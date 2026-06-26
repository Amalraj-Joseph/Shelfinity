#!/usr/bin/env bash
# Fix storage permissions for PostgreSQL
set -euo pipefail

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_info "Fixing storage permissions for PostgreSQL..."

# Get the node where the PV is located
NODE=$(oc get pv postgresql-pv -o jsonpath='{.spec.nodeAffinity.required.nodeSelectorTerms[0].matchExpressions[0].values[0]}' 2>/dev/null || echo "")

if [ -z "$NODE" ]; then
    log_info "No specific node affinity found, using any node"
    NODE=$(oc get nodes -o jsonpath='{.items[0].metadata.name}')
fi

log_info "Target node: $NODE"

# Create a privileged pod to fix permissions
log_info "Creating privileged pod to fix directory permissions..."

cat <<EOF | oc apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: fix-permissions
  namespace: shelfinity-dev
spec:
  nodeSelector:
    kubernetes.io/hostname: $NODE
  containers:
  - name: fix-perms
    image: registry.access.redhat.com/ubi9/ubi-minimal:latest
    command: ["/bin/sh", "-c"]
    args:
      - |
        mkdir -p /mnt/data/postgresql
        chmod 777 /mnt/data/postgresql
        chown -R 26:26 /mnt/data/postgresql
        echo "Permissions fixed successfully"
        sleep 10
    volumeMounts:
    - name: data
      mountPath: /mnt/data
    securityContext:
      privileged: true
      runAsUser: 0
  volumes:
  - name: data
    hostPath:
      path: /mnt/data
      type: DirectoryOrCreate
  restartPolicy: Never
  hostNetwork: true
  hostPID: true
EOF

log_info "Waiting for permissions fix to complete..."
sleep 15

# Check if pod completed successfully
if oc logs fix-permissions -n shelfinity-dev 2>/dev/null | grep -q "Permissions fixed successfully"; then
    log_info "✓ Permissions fixed successfully!"
else
    log_warn "Could not verify permissions fix. Check pod logs: oc logs fix-permissions -n shelfinity-dev"
fi

# Cleanup
log_info "Cleaning up..."
oc delete pod fix-permissions -n shelfinity-dev --ignore-not-found=true

log_info "Done! You can now restart the PostgreSQL pod:"
log_info "  oc delete pod -l app.kubernetes.io/name=postgresql -n shelfinity-dev"

# Made with Bob
