#!/usr/bin/env bash
#
# Copyright (c) 2025 Amalraj Joseph
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.
#
# Builds and starts the full Shelfinity stack (Postgres, Keycloak, backend,
# frontend) via Docker Compose, and waits for the backend to report healthy.

set -euo pipefail
cd "$(dirname "$0")/.."

if ! docker info > /dev/null 2>&1; then
  echo "Docker is not running. Start Docker and try again." >&2
  exit 1
fi

if docker compose version > /dev/null 2>&1; then
  compose=(docker compose)
else
  compose=(docker-compose)
fi

"${compose[@]}" -f docker/docker-compose.yml up -d --build

echo "Waiting for the backend to become healthy..."
for _ in $(seq 1 60); do
  if curl -sf http://localhost:9080/api/health | grep -q '"status":"UP"'; then
    echo ""
    echo "Shelfinity is up:"
    echo "  Frontend         http://localhost:3000"
    echo "  Backend API      http://localhost:9080/api"
    echo "  OpenAPI UI       http://localhost:9080/openapi/ui/"
    echo "  Keycloak admin   http://localhost:8080 (admin/admin)"
    exit 0
  fi
  sleep 5
done

echo "Backend did not become healthy in time. Check the logs:" >&2
echo "  ${compose[*]} -f docker/docker-compose.yml logs backend" >&2
exit 1
