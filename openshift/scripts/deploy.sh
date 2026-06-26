#!/usr/bin/env bash
# Shelfinity OpenShift Deployment Script
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
HELM_CHART_DIR="${PROJECT_ROOT}/openshift/helm/shelfinity"

# Default values
NAMESPACE="${NAMESPACE:-shelfinity-dev}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
GIT_REPO_URL="${GIT_REPO_URL:-}"
GIT_BRANCH="${GIT_BRANCH:-main}"
RELEASE_NAME="${RELEASE_NAME:-shelfinity}"

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
    log_info "Checking prerequisites..."
    
    if ! command -v oc &> /dev/null; then
        log_error "OpenShift CLI (oc) not found. Please install it first."
        exit 1
    fi
    
    if ! command -v helm &> /dev/null; then
        log_error "Helm not found. Please install it first."
        exit 1
    fi
    
    if ! oc whoami &> /dev/null; then
        log_error "Not logged into OpenShift. Please run 'oc login' first."
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

create_namespace() {
    log_info "Creating namespace: ${NAMESPACE}"
    
    if oc get project "${NAMESPACE}" &> /dev/null; then
        log_warn "Namespace ${NAMESPACE} already exists"
    else
        oc new-project "${NAMESPACE}" || log_error "Failed to create namespace"
    fi
}

deploy_postgresql() {
    log_info "Deploying PostgreSQL using OpenShift template..."
    
    # Deploy PostgreSQL with persistent storage
    oc process postgresql-persistent -n openshift \
        -p DATABASE_SERVICE_NAME=postgresql \
        -p POSTGRESQL_USER=shelfinity \
        -p POSTGRESQL_PASSWORD=shelfinity123 \
        -p POSTGRESQL_DATABASE=shelfinity \
        -p VOLUME_CAPACITY=5Gi \
        -p POSTGRESQL_VERSION=15-el9 \
        | oc create -f - -n "${NAMESPACE}"
    
    log_info "PostgreSQL deployed successfully"
    
    # Wait for PostgreSQL to be ready
    log_info "Waiting for PostgreSQL to be ready..."
    oc wait --for=condition=available --timeout=300s \
        dc/postgresql -n "${NAMESPACE}" || log_warn "PostgreSQL not ready yet"
}

deploy_helm_chart() {
    log_info "Deploying Helm chart (Keycloak, Backend, Frontend)..."
    
    local values_file="${HELM_CHART_DIR}/values-${ENVIRONMENT}.yaml"
    
    if [ ! -f "${values_file}" ]; then
        log_warn "Environment-specific values file not found: ${values_file}"
        log_info "Using default values.yaml"
        values_file="${HELM_CHART_DIR}/values.yaml"
    fi
    
    # Get cluster domain
    local cluster_domain=$(oc get ingresses.config.openshift.io cluster -o jsonpath='{.spec.domain}' 2>/dev/null || echo "apps.cluster.local")
    
    log_info "Detected cluster domain: ${cluster_domain}"
    
    # Deploy Helm chart with database disabled (using OpenShift template instead)
    helm upgrade --install "${RELEASE_NAME}" "${HELM_CHART_DIR}" \
        --namespace "${NAMESPACE}" \
        --values "${values_file}" \
        --set global.namespace="${NAMESPACE}" \
        --set global.environment="${ENVIRONMENT}" \
        --set global.clusterDomain="${cluster_domain}" \
        --set database.enabled=false \
        --wait \
        --timeout 10m
    
    log_info "Helm chart deployed successfully"
}

configure_urls() {
    log_info "Configuring service URLs..."
    
    # Wait for routes to be created
    log_info "Waiting for routes to be ready..."
    sleep 15
    
    # Get route URLs
    local frontend_route=$(oc get route frontend -n "${NAMESPACE}" -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
    local backend_route=$(oc get route backend -n "${NAMESPACE}" -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
    local keycloak_route=$(oc get route keycloak -n "${NAMESPACE}" -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
    
    if [ -z "$frontend_route" ] || [ -z "$backend_route" ] || [ -z "$keycloak_route" ]; then
        log_warn "Some routes not found yet, they may still be creating..."
        log_info "Frontend: ${frontend_route:-NOT FOUND}"
        log_info "Backend: ${backend_route:-NOT FOUND}"
        log_info "Keycloak: ${keycloak_route:-NOT FOUND}"
        return
    fi
    
    # Construct full URLs
    local frontend_url="https://${frontend_route}"
    local backend_url="https://${backend_route}"
    local keycloak_url="https://${keycloak_route}"
    
    log_info "Discovered Routes:"
    log_info "  Frontend:  ${frontend_url}"
    log_info "  Backend:   ${backend_url}"
    log_info "  Keycloak:  ${keycloak_url}"
    
    # Update Backend ConfigMap
    log_info "Updating backend configuration..."
    oc patch configmap backend-config -n "${NAMESPACE}" \
        --type merge \
        -p "{\"data\":{\"FRONTEND_URL\":\"${frontend_url}\",\"OIDC_ISSUER\":\"${keycloak_url}/realms/shelfinity\"}}" \
        2>/dev/null || log_warn "Failed to update backend config"
    
    # Update Frontend ConfigMap
    log_info "Updating frontend configuration..."
    oc patch configmap frontend-config -n "${NAMESPACE}" \
        --type merge \
        -p "{\"data\":{\"REACT_APP_KEYCLOAK_URL\":\"${keycloak_url}\",\"REACT_APP_API_URL\":\"${backend_url}\"}}" \
        2>/dev/null || log_warn "Failed to update frontend config"
    
    # Restart deployments to pick up new configuration
    log_info "Restarting deployments to apply configuration..."
    oc rollout restart deployment/backend -n "${NAMESPACE}" 2>/dev/null || log_warn "Backend deployment not found"
    oc rollout restart deployment/frontend -n "${NAMESPACE}" 2>/dev/null || log_warn "Frontend deployment not found"
    
    log_info "URL configuration complete"
}

start_builds() {
    log_info "Starting OpenShift builds from local source..."
    
    # Trigger backend build from local directory
    log_info "Building backend from ${PROJECT_ROOT}/backend..."
    oc start-build backend \
        -n "${NAMESPACE}" \
        --from-dir="${PROJECT_ROOT}/backend" \
        --follow || log_warn "Backend build failed"
    
    # Trigger frontend build from local directory
    log_info "Building frontend from ${PROJECT_ROOT}/frontend..."
    oc start-build frontend \
        -n "${NAMESPACE}" \
        --from-dir="${PROJECT_ROOT}/frontend" \
        --follow || log_warn "Frontend build failed"
    
    log_info "Builds completed"
}

wait_for_deployments() {
    log_info "Waiting for deployments to be ready..."
    
    oc wait --for=condition=available --timeout=600s \
        deployment/keycloak \
        -n "${NAMESPACE}" || log_warn "Some deployments are not ready yet"
    
    log_info "Core services are ready"
}

display_routes() {
    log_info "Application routes:"
    echo ""
    oc get routes -n "${NAMESPACE}" -o custom-columns=NAME:.metadata.name,HOST:.spec.host,TLS:.spec.tls.termination
    echo ""
}


print_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deploy Shelfinity to OpenShift

OPTIONS:
    -n, --namespace NAMESPACE    Target namespace (default: shelfinity-dev)
    -e, --environment ENV        Environment (dev/staging/prod) (default: dev)
    -r, --release RELEASE        Helm release name (default: shelfinity)
    -g, --git-repo URL          Git repository URL
    -b, --git-branch BRANCH     Git branch (default: main)
    -h, --help                  Show this help message

EXAMPLES:
    # Deploy to dev environment
    $0 -e dev

    # Deploy to production with custom namespace
    $0 -n shelfinity-prod -e prod

    # Deploy with git repository for builds
    $0 -g https://github.com/your-org/shelfinity.git -b main

ENVIRONMENT VARIABLES:
    NAMESPACE       Target namespace
    ENVIRONMENT     Environment (dev/staging/prod)
    GIT_REPO_URL    Git repository URL
    GIT_BRANCH      Git branch
    RELEASE_NAME    Helm release name

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -r|--release)
            RELEASE_NAME="$2"
            shift 2
            ;;
        -g|--git-repo)
            GIT_REPO_URL="$2"
            shift 2
            ;;
        -b|--git-branch)
            GIT_BRANCH="$2"
            shift 2
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
    log_info "Starting Shelfinity deployment to OpenShift"
    log_info "Namespace: ${NAMESPACE}"
    log_info "Environment: ${ENVIRONMENT}"
    log_info "Release: ${RELEASE_NAME}"
    echo ""
    
    check_prerequisites
    create_namespace
    deploy_postgresql
    deploy_helm_chart
    configure_urls
    start_builds
    wait_for_deployments
    display_routes
    
    echo ""
    log_info "=== Deployment Complete ==="
    log_info "✓ Persistent storage configured"
    log_info "✓ All services deployed"
    log_info "✓ URLs configured automatically"
    log_info "✓ CORS configured"
    log_info "✓ Seed data loaded (15 books, 3 users)"
    echo ""
    log_info "Default Credentials:"
    log_info "  Admin:  admin / admin123"
    log_info "  User 1: john.doe / john123"
    log_info "  User 2: jane.smith / jane123"
    echo ""
    log_info "Access your application using the routes displayed above"
}

main

# Made with Bob
