#!/usr/bin/env bash
#
# Copyright (c) 2025 Amalraj Joseph
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.
#
# Run ON THE VM, as root (sudo), from the directory deploy-to-vm.sh staged
# (~/shelfinity-deploy by default). One of exactly two deployment scripts —
# see deploy-to-vm.sh's header and deploy/JNDI_RACE_DEBUGGING_SUMMARY.md.
#
# Installs whatever isn't already installed (Semeru JDK, Open Liberty
# kernel, nginx, cloudflared), always redeploys the app itself (WAR, driver,
# truststore, jvm.options, frontend static files, systemd unit), and
# restarts only what actually needs restarting — re-running this after every
# deploy-to-vm.sh is meant to be fast and safe, not a fresh VM bring-up each
# time. Pass --reinstall-liberty or --reinstall-cloudflared to force a full
# reinstall of either piece (e.g. after bumping LIBERTY_VERSION below, or to
# rotate the Cloudflare tunnel token — cloudflared is intentionally left
# alone otherwise, since re-registering it on every deploy would mean an
# unnecessary tunnel reconnect each time).
#
# Usage:
#   sudo ./vm-setup.sh --cloudflare-token <TOKEN> \
#       --db2-ocid <VAULT_DB2_CREDENTIALS_OCID> \
#       --appid-ocid <VAULT_APPID_CONFIG_OCID> \
#       --secrets-ocid <VAULT_APP_SECRETS_OCID> \
#       --hostname shelfinity-app.amalraj.dev \
#       [--reinstall-liberty] [--reinstall-cloudflared]

set -euo pipefail

LIBERTY_VERSION="26.0.0.8"
LIBERTY_INSTALL_DIR="/opt/liberty"
SEMERU_HOME="/opt/semeru-jdk-21"
APP_DIR="/opt/shelfinity"
RUN_USER="${SHELFINITY_USER:-ubuntu}"

CLOUDFLARE_TOKEN=""
DB2_OCID=""
APPID_OCID=""
SECRETS_OCID=""
PUBLIC_HOSTNAME=""
REINSTALL_LIBERTY=false
REINSTALL_CLOUDFLARED=false

usage() {
    cat <<EOF
Usage: sudo $0 --cloudflare-token <TOKEN> --db2-ocid <OCID> --appid-ocid <OCID> --secrets-ocid <OCID> --hostname <host> [--reinstall-liberty] [--reinstall-cloudflared]

  --cloudflare-token   Tunnel token from Cloudflare dashboard > Networking >
                        Tunnels > this tunnel > install command (copy just
                        the token argument, not the whole command). Ignored
                        if cloudflared is already running — see
                        --reinstall-cloudflared.
  --db2-ocid           VAULT_DB2_CREDENTIALS_OCID
  --appid-ocid         VAULT_APPID_CONFIG_OCID
  --secrets-ocid       VAULT_APP_SECRETS_OCID
  --hostname           Public hostname this instance is reachable at behind
                        the Cloudflare Tunnel (e.g. shelfinity-app.amalraj.dev)
                        — single-level subdomain, see server.xml's comment on
                        why. No scheme, no trailing slash.
  --reinstall-liberty     Force a full Open Liberty kernel reinstall even if
                        one is already present.
  --reinstall-cloudflared  Force cloudflared to be reinstalled/reconnected
                        with the given token even if it's already running.
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --cloudflare-token) CLOUDFLARE_TOKEN="$2"; shift 2 ;;
        --db2-ocid) DB2_OCID="$2"; shift 2 ;;
        --appid-ocid) APPID_OCID="$2"; shift 2 ;;
        --secrets-ocid) SECRETS_OCID="$2"; shift 2 ;;
        --hostname) PUBLIC_HOSTNAME="$2"; shift 2 ;;
        --reinstall-liberty) REINSTALL_LIBERTY=true; shift ;;
        --reinstall-cloudflared) REINSTALL_CLOUDFLARED=true; shift ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
    esac
done

if [ "$(id -u)" -ne 0 ]; then
    echo "Run as root (sudo)." >&2
    exit 1
fi
for pair in "CLOUDFLARE_TOKEN:--cloudflare-token" "DB2_OCID:--db2-ocid" "APPID_OCID:--appid-ocid" "SECRETS_OCID:--secrets-ocid" "PUBLIC_HOSTNAME:--hostname"; do
    var="${pair%%:*}"; flag="${pair##*:}"
    if [ -z "${!var}" ]; then
        echo "FATAL: ${flag} is required." >&2
        usage
        exit 1
    fi
done

STAGE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
for required in shelfinity-backend.war jcc-12.1.5.0.jar app-truststore.jks vault-bootstrap.jar server.xml jvm.options frontend-build.tar.gz nginx/shelfinity.conf systemd/shelfinity-backend.service; do
    if [ ! -f "${STAGE_DIR}/${required}" ]; then
        echo "FATAL: ${required} missing from ${STAGE_DIR} — re-run deploy-to-vm.sh from the dev machine first." >&2
        exit 1
    fi
done

echo "==> [1/7] Semeru (OpenJ9) JDK 21"
# Matches icr.io/appcafe/open-liberty:kernel-slim-java21-openj9-ubi-minimal —
# the exact image local development validates against (see backend/Dockerfile
# and deploy/JNDI_RACE_DEBUGGING_SUMMARY.md). Ubuntu's own
# openjdk-21-jre-headless package is HotSpot, not OpenJ9 — a real JVM-vendor
# mismatch from what's actually been tested, and the proximate cause of a
# same-day surprise (WELD-001524) during the first cloud deployment attempt.
if [ -x "${SEMERU_HOME}/bin/java" ]; then
    echo "    already installed at ${SEMERU_HOME}, skipping"
else
    ASSET_URL="$(curl -fsSL https://api.github.com/repos/ibmruntimes/semeru21-binaries/releases/latest \
        | grep -oE '"browser_download_url": *"[^"]*jre_x64_linux[^"]*\.tar\.gz"' \
        | head -1 | sed -E 's/.*"(https[^"]+)".*/\1/')"
    if [ -z "${ASSET_URL}" ]; then
        echo "FATAL: could not find a Semeru 21 linux x64 JRE asset — check https://github.com/ibmruntimes/semeru21-binaries/releases manually." >&2
        exit 1
    fi
    echo "    downloading ${ASSET_URL}"
    TMP_TAR="$(mktemp)"
    curl -fsSL -o "${TMP_TAR}" "${ASSET_URL}"
    mkdir -p "${SEMERU_HOME}"
    tar -xzf "${TMP_TAR}" -C "${SEMERU_HOME}" --strip-components=1
    rm -f "${TMP_TAR}"
fi
export JAVA_HOME="${SEMERU_HOME}"
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "==> [2/7] nginx"
if ! dpkg -s nginx >/dev/null 2>&1; then
    export DEBIAN_FRONTEND=noninteractive
    apt-get update -qq
    apt-get install -y -qq nginx
    NGINX_JUST_INSTALLED=true
else
    NGINX_JUST_INSTALLED=false
fi
# Ubuntu's nginx package ships a default site pre-enabled on port 80 with
# default_server, which wins over shelfinity.conf for any request that
# doesn't match on Host (e.g. a bare curl localhost) — so this has to run
# every time, not just on a fresh install, or a re-run against a VM where
# nginx pre-existed leaves the stock welcome page serving :80 instead.
rm -f /etc/nginx/sites-enabled/default
cp "${STAGE_DIR}/nginx/shelfinity.conf" /etc/nginx/conf.d/shelfinity.conf
if [ "${NGINX_JUST_INSTALLED}" = true ]; then
    systemctl enable --now nginx
elif systemctl is-active --quiet nginx; then
    echo "    already running, reloading config"
    systemctl reload nginx
else
    systemctl enable --now nginx
fi

echo "==> [3/7] cloudflared"
if systemctl is-active --quiet cloudflared 2>/dev/null && [ "${REINSTALL_CLOUDFLARED}" != true ]; then
    echo "    already running, leaving it alone (pass --reinstall-cloudflared to force a reconnect)"
else
    if ! command -v cloudflared >/dev/null 2>&1; then
        mkdir -p --mode=0755 /usr/share/keyrings
        curl -fsSL https://pkg.cloudflare.com/cloudflare-main.gpg -o /usr/share/keyrings/cloudflare-main.gpg
        echo "deb [signed-by=/usr/share/keyrings/cloudflare-main.gpg] https://pkg.cloudflare.com/cloudflared any main" \
            > /etc/apt/sources.list.d/cloudflared.list
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -qq
        apt-get install -y -qq cloudflared
    fi
    # Both writes the systemd unit and enables+starts it; safe to re-run.
    cloudflared service install "${CLOUDFLARE_TOKEN}"
fi

echo "==> [4/7] Open Liberty kernel"
WLP="${LIBERTY_INSTALL_DIR}/wlp"
if [ -x "${WLP}/bin/server" ] && [ "${REINSTALL_LIBERTY}" != true ]; then
    echo "    already installed at ${WLP}, skipping kernel download (pass --reinstall-liberty to force)"
else
    echo "    downloading Open Liberty kernel ${LIBERTY_VERSION}"
    TMP_ZIP="$(mktemp)"
    curl -sL -o "${TMP_ZIP}" \
        "https://repo1.maven.org/maven2/io/openliberty/openliberty-kernel/${LIBERTY_VERSION}/openliberty-kernel-${LIBERTY_VERSION}.zip"
    mkdir -p "${LIBERTY_INSTALL_DIR}"
    # Not present on the base Ubuntu cloud image.
    if ! command -v unzip >/dev/null 2>&1; then
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -qq
        apt-get install -y -qq unzip
    fi
    unzip -q -o "${TMP_ZIP}" -d "${LIBERTY_INSTALL_DIR}"
    rm -f "${TMP_ZIP}"
    "${WLP}/bin/server" create defaultServer
fi

echo "==> [5/7] Deploying app (WAR, driver, truststore, config, features)"
cp "${STAGE_DIR}/server.xml" "${WLP}/usr/servers/defaultServer/server.xml"
cp "${STAGE_DIR}/jvm.options" "${WLP}/usr/servers/defaultServer/jvm.options"
"${WLP}/bin/featureUtility" installServerFeatures defaultServer
cp "${STAGE_DIR}/shelfinity-backend.war" "${WLP}/usr/servers/defaultServer/apps/"
mkdir -p "${WLP}/usr/shared/resources/lib"
cp "${STAGE_DIR}/jcc-12.1.5.0.jar" "${WLP}/usr/shared/resources/lib/"
cp "${STAGE_DIR}/app-truststore.jks" "${WLP}/usr/shared/resources/lib/"

mkdir -p "${APP_DIR}"
cp "${STAGE_DIR}/vault-bootstrap.jar" "${APP_DIR}/"

echo "    replacing deployed frontend static files"
rm -rf /usr/share/nginx/html/*
mkdir -p /usr/share/nginx/html
tar -xzf "${STAGE_DIR}/frontend-build.tar.gz" -C /usr/share/nginx/html

# nginx was already started back in step [2/7], before this content existed
# on disk — a bare content swap on a running nginx doesn't get picked up
# reliably (confirmed directly: curl kept serving the pre-deploy content,
# byte-for-byte, until nginx was restarted — reload was never even tried
# since restart is free here and removes any doubt). Without this, every
# deploy silently keeps serving whatever nginx had at first start instead
# of the just-deployed build.
systemctl restart nginx

id -u "${RUN_USER}" >/dev/null 2>&1 || useradd -r -s /usr/sbin/nologin "${RUN_USER}"
chown -R "${RUN_USER}:${RUN_USER}" "${LIBERTY_INSTALL_DIR}" "${APP_DIR}"

echo "==> [6/7] Writing shelfinity-backend.service from the template"
sed -e "s|__VAULT_DB2_CREDENTIALS_OCID__|${DB2_OCID}|" \
    -e "s|__VAULT_APPID_CONFIG_OCID__|${APPID_OCID}|" \
    -e "s|__VAULT_APP_SECRETS_OCID__|${SECRETS_OCID}|" \
    -e "s|__PUBLIC_HOSTNAME__|${PUBLIC_HOSTNAME}|g" \
    "${STAGE_DIR}/systemd/shelfinity-backend.service" > /etc/systemd/system/shelfinity-backend.service
systemctl daemon-reload

echo "==> [7/7] Restarting shelfinity-backend"
systemctl stop shelfinity-backend 2>/dev/null || true
systemctl enable --now shelfinity-backend

echo
echo "==> Status:"
systemctl --no-pager status nginx cloudflared shelfinity-backend | grep -E "●|Active:" || true
echo
echo "Check the app actually came up healthy with:"
echo "  journalctl -u shelfinity-backend -n 60 --no-pager"
echo "  curl -s http://localhost:9080/api/books   # expect 401, not 500 or a hang"
