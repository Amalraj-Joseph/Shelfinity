#!/usr/bin/env bash
# Setup Local Storage for OpenShift Development
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
STORAGE_DIR="${PROJECT_ROOT}/openshift/storage"

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v oc &> /dev/null; then
        log_error "OpenShift CLI (oc) not found. Please install it first."
        exit 1
    fi
    
    if ! oc whoami &> /dev/null; then
        log_error "Not logged into OpenShift. Please run 'oc login' first."
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

setup_storage() {
    log_info "Setting up local storage..."
    
    # Check if StorageClass already exists
    if oc get storageclass local-storage &> /dev/null; then
        log_warn "StorageClass 'local-storage' already exists"
    else
        log_info "Creating StorageClass 'local-storage'..."
        oc apply -f "${STORAGE_DIR}/local-storage-class.yaml"
        log_info "StorageClass created successfully"
    fi
    
    # Verify StorageClass
    log_info "Verifying StorageClass..."
    oc get storageclass local-storage
    
    log_info "Storage setup complete!"
}

display_info() {
    echo ""
    log_info "=== Storage Configuration ==="
    log_info "StorageClass: local-storage (default)"
    log_info "Type: hostPath (local node storage)"
    log_info "Capacity: 5Gi per volume"
    log_info "Access Mode: ReadWriteOnce"
    log_info "Reclaim Policy: Retain"
    echo ""
    log_info "Storage is now ready for use!"
    log_info "You can now deploy with persistent storage enabled."
    echo ""
}

# Main execution
main() {
    log_info "Starting storage setup for OpenShift"
    echo ""
    
    check_prerequisites
    setup_storage
    display_info
}

main

# Made with Bob