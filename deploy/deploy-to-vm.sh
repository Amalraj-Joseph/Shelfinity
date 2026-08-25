#!/usr/bin/env bash
#
# Copyright (c) 2025 Amalraj Joseph
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.
#
# Run on the DEV MACHINE. One of exactly two deployment scripts (the other,
# vm-setup.sh, runs on the VM) — see deploy/JNDI_RACE_DEBUGGING_SUMMARY.md
# for why this pair replaced the previous package-for-vm.sh /
# install-liberty.sh / install-cloudflared.sh trio, and for the persistence
# architecture change that's the actual point of this deployment.
#
# Does everything that needs Maven, npm, or the source tree — none of which
# belong on the target VM (see install-liberty.sh's old reasoning, still
# true): builds the backend WAR, fetches the Db2 driver, builds the
# truststore, builds vault-bootstrap, builds the frontend, stages it all
# alongside vm-setup.sh and the server/nginx/systemd config, then SSHes the
# whole bundle over — wiping whatever was staged on the VM from a previous
# run first, so stale files never linger across deploys.
#
# It does NOT install or restart anything on the VM itself — that's
# vm-setup.sh's job, run separately (it needs secrets — the Cloudflare
# tunnel token and the Vault secret OCIDs — that have no reason to pass
# through this script or live in its shell history).
#
# Usage: ./deploy/deploy-to-vm.sh <path-to-ssh-private-key> <user@vm-host>
# Example: ./deploy/deploy-to-vm.sh ~/.ssh/shelfinity-vm.pem ubuntu@203.0.113.10

set -euo pipefail

if [ $# -ne 2 ]; then
    echo "Usage: $0 <path-to-ssh-private-key> <user@vm-host>" >&2
    exit 1
fi
SSH_KEY="$1"
VM_HOST="$2"
if [ ! -f "${SSH_KEY}" ]; then
    echo "FATAL: SSH key not found: ${SSH_KEY}" >&2
    exit 1
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STAGE_DIR="$(mktemp -d)"
trap 'rm -rf "${STAGE_DIR}"' EXIT
SSH_OPTS=(-i "${SSH_KEY}" -o StrictHostKeyChecking=accept-new)

echo "==> Building backend WAR"
(cd "${REPO_ROOT}/backend" && mvn -B -q clean package -DskipTests)
cp "${REPO_ROOT}/backend/target/shelfinity-backend.war" "${STAGE_DIR}/"

echo "==> Fetching Db2 JDBC driver (Db2 only — this deployment never talks to Postgres)"
mvn -q -DskipTests dependency:copy \
    -Dartifact=com.ibm.db2:jcc:12.1.5.0 \
    -DoutputDirectory="${STAGE_DIR}" \
    -f "${REPO_ROOT}/backend/pom.xml"

echo "==> Building the truststore (JDK cacerts + backend/certs/*)"
EFFECTIVE_JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/jdk-21}"
cp "${EFFECTIVE_JAVA_HOME}/lib/security/cacerts" "${STAGE_DIR}/app-truststore.jks"
for f in "${REPO_ROOT}"/backend/certs/*.pem "${REPO_ROOT}"/backend/certs/*.crt; do
    [ -f "$f" ] || continue
    "${EFFECTIVE_JAVA_HOME}/bin/keytool" -importcert -noprompt -trustcacerts \
        -alias "custom-$(basename "$f")" \
        -file "$f" \
        -keystore "${STAGE_DIR}/app-truststore.jks" \
        -storepass changeit
done

echo "==> Building vault-bootstrap"
(cd "${REPO_ROOT}/backend/vault-bootstrap" && mvn -B -q clean package)
cp "${REPO_ROOT}/backend/vault-bootstrap/target/vault-bootstrap.jar" "${STAGE_DIR}/"

echo "==> Building frontend (production React build — picks up frontend/.env.local for cloud/pkce config)"
(cd "${REPO_ROOT}/frontend" && npm ci --silent && npm run build --silent)
tar -czf "${STAGE_DIR}/frontend-build.tar.gz" -C "${REPO_ROOT}/frontend/build" .

echo "==> Staging server/nginx/systemd config and vm-setup.sh"
cp "${REPO_ROOT}/backend/server.xml" "${STAGE_DIR}/"
cp "${REPO_ROOT}/deploy/jvm.options" "${STAGE_DIR}/"
cp "${REPO_ROOT}/deploy/vm-setup.sh" "${STAGE_DIR}/"
mkdir -p "${STAGE_DIR}/nginx" "${STAGE_DIR}/systemd"
cp "${REPO_ROOT}/deploy/nginx/shelfinity.conf" "${STAGE_DIR}/nginx/"
cp "${REPO_ROOT}/deploy/systemd/shelfinity-backend.service" "${STAGE_DIR}/systemd/"

BUNDLE="${STAGE_DIR}.tar.gz"
tar -czf "${BUNDLE}" -C "${STAGE_DIR}" .
echo "==> Built deploy bundle ($(du -h "${BUNDLE}" | cut -f1))"

REMOTE_DIR="shelfinity-deploy"
echo "==> Removing any previous staged deploy on the VM and copying the new one"
ssh "${SSH_OPTS[@]}" "${VM_HOST}" "rm -rf ~/${REMOTE_DIR} && mkdir -p ~/${REMOTE_DIR}"
scp "${SSH_OPTS[@]}" "${BUNDLE}" "${VM_HOST}:~/${REMOTE_DIR}/bundle.tar.gz"
ssh "${SSH_OPTS[@]}" "${VM_HOST}" \
    "tar -xzf ~/${REMOTE_DIR}/bundle.tar.gz -C ~/${REMOTE_DIR} && rm -f ~/${REMOTE_DIR}/bundle.tar.gz && chmod +x ~/${REMOTE_DIR}/vm-setup.sh"

echo
echo "==> Done. Files are staged on the VM at ~/${REMOTE_DIR}/"
echo "Next: SSH in and run vm-setup.sh with the secrets it needs, e.g.:"
echo "  ssh -i ${SSH_KEY} ${VM_HOST}"
echo "  cd ~/${REMOTE_DIR}"
echo "  sudo ./vm-setup.sh --cloudflare-token <TOKEN> \\"
echo "      --db2-ocid <VAULT_DB2_CREDENTIALS_OCID> \\"
echo "      --appid-ocid <VAULT_APPID_CONFIG_OCID> \\"
echo "      --secrets-ocid <VAULT_APP_SECRETS_OCID> \\"
echo "      --hostname shelfinity-app.amalraj.dev"
echo "See ./vm-setup.sh --help for the full flag list, including what's safe to skip on a repeat run."
