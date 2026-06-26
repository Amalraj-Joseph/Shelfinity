#!/usr/bin/env bash
# Setup PostgreSQL and Keycloak locally on macOS
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    log_error "Homebrew not found. Please install it first:"
    log_error "  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
    exit 1
fi

log_info "Installing PostgreSQL 15..."
if brew list postgresql@15 &> /dev/null; then
    log_info "PostgreSQL 15 already installed"
else
    brew install postgresql@15
fi

log_info "Starting PostgreSQL service..."
brew services start postgresql@15

# Wait for PostgreSQL to start
sleep 3

log_info "Creating Shelfinity database..."
if psql postgres -lqt | cut -d \| -f 1 | grep -qw shelfinity; then
    log_warn "Database 'shelfinity' already exists"
else
    createdb shelfinity
    log_info "Database 'shelfinity' created"
fi

log_info "Creating database user..."
psql postgres -c "CREATE USER shelfinity WITH PASSWORD 'shelfinity123';" 2>/dev/null || log_warn "User may already exist"
psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE shelfinity TO shelfinity;" 2>/dev/null

log_info "PostgreSQL setup complete!"
log_info "  Host: localhost"
log_info "  Port: 5432"
log_info "  Database: shelfinity"
log_info "  User: shelfinity"
log_info "  Password: shelfinity123"
echo ""

log_info "Installing Keycloak..."
if brew list keycloak &> /dev/null; then
    log_info "Keycloak already installed"
else
    brew install keycloak
fi

log_info "Starting Keycloak..."
log_warn "Keycloak will start on http://localhost:8080"
log_warn "Default admin credentials: admin / admin"
log_warn "Run this command in a separate terminal:"
echo ""
echo "  kc.sh start-dev --http-port=8080"
echo ""

log_info "=== Setup Complete ==="
log_info "Next steps:"
log_info "1. Start Keycloak: kc.sh start-dev --http-port=8080"
log_info "2. Configure Keycloak realm (see docs/guides/KEYCLOAK_SETUP.md)"
log_info "3. Start backend: cd backend && mvn liberty:dev"
log_info "4. Start frontend: cd frontend && npm start"

# Made with Bob
