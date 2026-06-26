#!/usr/bin/env bash
# Shelfinity OpenShift Cleanup Script
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
NAMESPACE="${NAMESPACE:-shelfinity-dev}"
RELEASE_NAME="${RELEASE_NAME:-shelfinity}"
DELETE_NAMESPACE="${DELETE_NAMESPACE:-false}"

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

check_prerequisites() {
    if ! command -v oc &> /dev/null; then
        log_error "OpenShift CLI (oc) not found"
        exit 1
    fi
    
    if ! command -v helm &> /dev/null; then
        log_error "Helm not found"
        exit 1
    fi
    
    if ! oc whoami &> /dev/null; then
        log_error "Not logged into OpenShift"
        exit 1
    fi
}

confirm_deletion() {
    log_warn "This will delete the Shelfinity deployment from namespace: ${NAMESPACE}"
    
    if [ "${DELETE_NAMESPACE}" = "true" ]; then
        log_warn "The entire namespace will be DELETED!"
    fi
    
    read -p "Are you sure you want to continue? (yes/no): " -r
    echo
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        log_info "Cleanup cancelled"
        exit 0
    fi
}

cleanup_helm_release() {
    log_info "Uninstalling Helm release: ${RELEASE_NAME}"
    
    if helm list -n "${NAMESPACE}" | grep -q "${RELEASE_NAME}"; then
        helm uninstall "${RELEASE_NAME}" -n "${NAMESPACE}" || log_warn "Failed to uninstall Helm release"
        log_info "Helm release uninstalled"
    else
        log_warn "Helm release ${RELEASE_NAME} not found"
    fi
}

cleanup_builds() {
    log_info "Cleaning up builds and image streams..."
    
    oc delete buildconfig --all -n "${NAMESPACE}" 2>/dev/null || log_warn "No build configs to delete"
    oc delete imagestream --all -n "${NAMESPACE}" 2>/dev/null || log_warn "No image streams to delete"
    oc delete build --all -n "${NAMESPACE}" 2>/dev/null || log_warn "No builds to delete"
}

cleanup_pvcs() {
    log_info "Cleaning up persistent volume claims..."
    
    oc delete pvc --all -n "${NAMESPACE}" 2>/dev/null || log_warn "No PVCs to delete"
}

delete_namespace() {
    if [ "${DELETE_NAMESPACE}" = "true" ]; then
        log_info "Deleting namespace: ${NAMESPACE}"
        oc delete project "${NAMESPACE}" || log_error "Failed to delete namespace"
        log_info "Namespace deleted"
    else
        log_info "Namespace ${NAMESPACE} preserved (use --delete-namespace to remove it)"
    fi
}

print_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Cleanup Shelfinity deployment from OpenShift

OPTIONS:
    -n, --namespace NAMESPACE       Target namespace (default: shelfinity-dev)
    -r, --release RELEASE          Helm release name (default: shelfinity)
    -d, --delete-namespace         Delete the entire namespace
    -y, --yes                      Skip confirmation prompt
    -h, --help                     Show this help message

EXAMPLES:
    # Cleanup deployment but keep namespace
    $0 -n shelfinity-dev

    # Cleanup and delete namespace
    $0 -n shelfinity-dev --delete-namespace

    # Cleanup without confirmation
    $0 -n shelfinity-dev -y

EOF
}

# Parse command line arguments
SKIP_CONFIRM=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--release)
            RELEASE_NAME="$2"
            shift 2
            ;;
        -d|--delete-namespace)
            DELETE_NAMESPACE="true"
            shift
            ;;
        -y|--yes)
            SKIP_CONFIRM=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

# Main execution
main() {
    log_info "Starting Shelfinity cleanup"
    log_info "Namespace: ${NAMESPACE}"
    log_info "Release: ${RELEASE_NAME}"
    echo ""
    
    check_prerequisites
    
    if [ "${SKIP_CONFIRM}" = "false" ]; then
        confirm_deletion
    fi
    
    cleanup_helm_release
    cleanup_builds
    cleanup_pvcs
    delete_namespace
    
    log_info "Cleanup completed successfully!"
}

main

# Made with Bob
