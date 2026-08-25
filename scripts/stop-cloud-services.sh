#!/usr/bin/env bash
#
# Copyright (c) 2025 Amalraj Joseph
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.
#
# Stops the stack started by start-cloud-services.sh. No -v/volume option —
# unlike start.sh's local stack, there's no local database volume here; the
# data lives in the real Db2 instance and isn't touched by this script.

set -euo pipefail
cd "$(dirname "$0")/.."

if docker compose version > /dev/null 2>&1; then
  compose=(docker compose)
else
  compose=(docker-compose)
fi

"${compose[@]}" -f docker/docker-compose.cloud-services.yml down
echo "Shelfinity (cloud services) stopped."
