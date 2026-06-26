#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../docker"
docker-compose -f docker-compose.yml down -v
echo "Shelfinity has been stopped and volumes removed."
