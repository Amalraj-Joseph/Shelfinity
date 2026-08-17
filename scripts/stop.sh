#!/usr/bin/env bash
#
# Copyright (c) 2025 Amalraj Joseph
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.
#
# Stops the Shelfinity stack. Pass -v to also remove the Postgres volume
# (starts from a clean, freshly-seeded database next time).

set -euo pipefail
cd "$(dirname "$0")/.."

if docker compose version > /dev/null 2>&1; then
  compose=(docker compose)
else
  compose=(docker-compose)
fi

if [ "${1:-}" = "-v" ]; then
  "${compose[@]}" -f docker/docker-compose.yml down -v
  echo "Shelfinity stopped and volumes removed."
else
  "${compose[@]}" -f docker/docker-compose.yml down
  echo "Shelfinity stopped."
fi
