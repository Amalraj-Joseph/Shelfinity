#!/usr/bin/env bash
# Install NFS Provisioner for OpenShift
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

log_info "Installing NFS Provisioner..."

# Add Helm repo for NFS provisioner
log_info "Adding NFS provisioner Helm repository..."
helm repo add nfs-subdir-external-provisioner https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner/
helm repo update

# Install NFS provisioner
log_info "Installing NFS provisioner..."
helm upgrade --install nfs-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
    --namespace kube-system \
    --set nfs.server=YOUR_NFS_SERVER_IP \
    --set nfs.path=/exported/path \
    --set storageClass.name=nfs-client \
    --set storageClass.defaultClass=true

log_info "NFS Provisioner installed successfully"
log_info "Storage class 'nfs-client' is now available"

# Made with Bob
