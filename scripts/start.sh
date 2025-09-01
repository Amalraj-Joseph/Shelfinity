#!/bin/bash

# Copyright (c) 2025 Shadow-Codex
#
# This source code is licensed under the MIT License.
# See the LICENSE file in the root directory for more information.

set -e

echo "🚀 Starting Shelfinity Library Management System..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose.yml exists
if [ ! -f "docker/docker-compose.yml" ]; then
    echo "❌ docker-compose.yml not found in docker/ directory"
    exit 1
fi

# Navigate to docker directory and start services
cd docker

echo "📦 Starting Docker services..."
docker-compose up -d

echo "⏳ Waiting for services to be ready..."

# Wait for PostgreSQL
echo "🔄 Waiting for PostgreSQL..."
until docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; do
    sleep 2
done
echo "✅ PostgreSQL is ready"

# Wait for Keycloak
echo "🔄 Waiting for Keycloak..."
until curl -f http://localhost:8080 > /dev/null 2>&1; do
    sleep 5
done
echo "✅ Keycloak is ready"

# Wait for Backend
echo "🔄 Waiting for Backend..."
until curl -f http://localhost:9080/health/ > /dev/null 2>&1; do
    sleep 5
done
echo "✅ Backend is ready"

# Wait for Frontend
echo "🔄 Waiting for Frontend..."
until curl -f http://localhost:3000 > /dev/null 2>&1; do
    sleep 5
done
echo "✅ Frontend is ready"

echo ""
echo "🎉 Shelfinity is now running!"
echo ""
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Backend API: http://localhost:9080/api"
echo "🔐 Keycloak Admin: http://localhost:8080 (admin/admin)"
echo "🗄️  PostgreSQL: localhost:5432"
echo ""
echo "📚 API Documentation: http://localhost:3000/docs"
echo ""
echo "To stop the services, run: docker-compose down"
