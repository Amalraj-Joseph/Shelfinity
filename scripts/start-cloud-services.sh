#!/usr/bin/env bash
#
# Copyright (c) 2025 Amalraj Joseph
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.
#
# Builds and starts the backend and frontend locally via Docker Compose,
# same as start.sh, but pointed at real managed services on IBM Cloud
# (Db2 on Cloud, App ID) instead of the local Postgres/Keycloak containers.
#
# Not the full cloud deployment — that's deploy/deploy-to-vm.sh +
# deploy/vm-setup.sh, which also puts the app itself on a VM behind
# Cloudflare. This is for developing/testing locally against the real IBM
# Cloud services. See .env.cloud-services.example for what to configure
# first, including the App ID redirect URI you need to allow-list.

set -euo pipefail
cd "$(dirname "$0")/.."

ENV_FILE=".env.cloud-services"
if [ ! -f "${ENV_FILE}" ]; then
  cp .env.cloud-services.example "${ENV_FILE}"
  echo "Created ${ENV_FILE} from the example — fill in your Db2 and App ID" >&2
  echo "values (from the IBM Cloud console) and re-run this script." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

unfilled=()
for var in DB_JDBC_URL DB_USER DB_PASSWORD OIDC_ISSUER OIDC_JWKS_URI OIDC_TOKEN_ENDPOINT \
           OIDC_USERINFO_ENDPOINT OIDC_CLIENT_ID REACT_APP_CLIENT_ID REACT_APP_OIDC_AUTHORIZATION_ENDPOINT; do
  value="${!var:-}"
  if [ -z "${value}" ] || [[ "${value}" == *"CHANGEME"* ]]; then
    unfilled+=("${var}")
  fi
done
if [ "${#unfilled[@]}" -gt 0 ]; then
  echo "${ENV_FILE} still has placeholder values for: ${unfilled[*]}" >&2
  echo "Fill in the real Db2/App ID values from the IBM Cloud console and re-run." >&2
  exit 1
fi

if ! docker info > /dev/null 2>&1; then
  echo "Docker is not running. Start Docker and try again." >&2
  exit 1
fi

if docker compose version > /dev/null 2>&1; then
  compose=(docker compose)
else
  compose=(docker-compose)
fi

"${compose[@]}" -f docker/docker-compose.cloud-services.yml up -d --build

echo "Waiting for the backend to become healthy..."
for _ in $(seq 1 60); do
  if curl -sf http://localhost:9080/api/health | grep -q '"status":"UP"'; then
    echo ""
    echo "Shelfinity is up, running locally against IBM Cloud services:"
    echo "  Frontend         http://localhost:3000"
    echo "  Backend API      http://localhost:9080/api"
    echo "  OpenAPI UI       http://localhost:9080/openapi/ui/"
    echo "  Database         Db2 on Cloud (see DB_JDBC_URL in ${ENV_FILE})"
    echo "  Identity         App ID (see OIDC_ISSUER in ${ENV_FILE})"
    echo ""
    echo "This writes to the real Db2 instance — no local database involved."
    exit 0
  fi
  sleep 5
done

echo "Backend did not become healthy in time. Check the logs:" >&2
echo "  ${compose[*]} -f docker/docker-compose.cloud-services.yml logs backend" >&2
exit 1
