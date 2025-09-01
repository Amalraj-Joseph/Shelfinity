#!/bin/bash

# Copyright (c) 2025 Shadow-Codex
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.

set -e

echo "🌐 Setting up local DNS for Shelfinity..."

# Check if running as root for DNS setup
if [ "$EUID" -ne 0 ]; then
    echo "⚠️  Not running as root. Local DNS setup will be skipped."
    echo "To enable shelfinity.local domain, run: sudo ./scripts/setup-local-dns.sh"
    exit 0
fi

# Setup local DNS entries
echo "🔧 Adding local DNS entries..."

# Check if entries already exist
if ! grep -q "shelfinity.local" /etc/hosts; then
    echo "127.0.0.1 shelfinity.local" >> /etc/hosts
    echo "127.0.0.1 www.shelfinity.local" >> /etc/hosts
    echo "✅ Added shelfinity.local to /etc/hosts"
else
    echo "✅ shelfinity.local already configured in /etc/hosts"
fi

if ! grep -q "keycloak.local" /etc/hosts; then
    echo "127.0.0.1 keycloak.local" >> /etc/hosts
    echo "✅ Added keycloak.local to /etc/hosts"
else
    echo "✅ keycloak.local already configured in /etc/hosts"
fi

echo "🎉 Local DNS setup complete!"
echo ""
echo "You can now access the services using:"
echo "📱 Frontend: http://shelfinity.local:3000"
echo "🔐 Keycloak: http://keycloak.local:8080"
echo ""
echo "To remove DNS entries later, run: sudo sed -i '/shelfinity.local\|keycloak.local/d' /etc/hosts"
