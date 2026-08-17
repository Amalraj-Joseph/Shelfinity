# Shelfinity Backend - Local Development Guide

This guide explains how to run the Shelfinity backend locally without Docker Compose, using a containerized PostgreSQL database and Open Liberty running directly on your machine.

## Prerequisites

- Java 21+ (OpenJDK recommended)
- Maven 3.9+
- Docker (for PostgreSQL container)

## Step 1: Start PostgreSQL Database Container

Run PostgreSQL in a Docker container:

```bash
docker run --name shelfinity-postgres \
  -e POSTGRES_USER=shelfinity \
  -e POSTGRES_PASSWORD=shelfinity \
  -e POSTGRES_DB=shelfinity \
  -p 5432:5432 -d \
  public.ecr.aws/docker/library/postgres:16-alpine
```

**Container Details:**
- **Container Name:** `shelfinity-postgres`
- **Database:** `shelfinity`
- **Username:** `shelfinity`
- **Password:** `shelfinity`
- **Port:** `5432` (mapped to localhost:5432)

## Step 2: Start the Backend Application

Navigate to the backend directory and run Liberty in development mode:

```bash
cd backend
mvn liberty:dev
```

This will:
- Download and install Open Liberty server
- Build the application
- Deploy to Liberty
- Start the server in development mode with hot reload

## Step 3: Verify the Application

Once the server starts, verify these endpoints:

### Health Check
```bash
curl http://localhost:9080/api/health
```

### OpenAPI Documentation
- **Swagger UI:** http://localhost:9080/openapi/ui/
- **OpenAPI JSON:** http://localhost:9080/openapi

### Sample API Endpoints
```bash
# Get all books
curl http://localhost:9080/api/books

# Search books
curl "http://localhost:9080/api/books?search=gatsby"

# Get available books only
curl "http://localhost:9080/api/books?available=true"
```

## API Groups

The OpenAPI documentation organizes endpoints into logical groups:

### 🏥 Health
- System health and monitoring endpoints

### 📚 Books  
- Book management operations
- Create, read, update, delete books
- Search functionality

### 👥 Users
- User management and authentication operations

### 📋 Queue
- Queue management for processing user requests
- Registrations, book borrowing, and returns

## Development Features

### Hot Reload
Liberty dev mode automatically:
- Recompiles Java sources when changed
- Redeploys the application
- Updates server configuration

### Debugging
Debug port is available on **7777** when running in dev mode.

### Liberty Dev Mode Commands
While the server is running, press:
- **`h`** - Show help menu
- **`q`** - Stop server and quit dev mode
- **`Ctrl+C`** - Force quit

## Database Management

### Connecting to Database
```bash
docker exec -it shelfinity-postgres psql -U shelfinity -d shelfinity
```

### View Container Logs
```bash
docker logs shelfinity-postgres
```

### Stop Database Container
```bash
docker stop shelfinity-postgres
docker rm shelfinity-postgres
```

## Troubleshooting

### Port Conflicts
- **9080** - Liberty HTTP port
- **9443** - Liberty HTTPS port  
- **5432** - PostgreSQL port
- **7777** - Debug port

### Common Issues

1. **Database Connection Failed**
   - Ensure PostgreSQL container is running
   - Check if port 5432 is available

2. **Liberty Won't Start**
   - Check if port 9080 is available
   - Verify Java 21+ is installed

3. **Application Deployment Fails**
   - Check Maven build succeeds: `mvn clean compile`
   - Review Liberty logs in `target/liberty/wlp/usr/servers/defaultServer/logs/`

## Configuration Files

- **Server Config:** `backend/server.xml`
- **Database Config:** `backend/src/main/resources/META-INF/persistence.xml`
- **Maven Config:** `backend/pom.xml`

## Next Steps

After verifying local development:
1. Containerize the backend application
2. Set up Keycloak integration
3. Configure frontend to connect to local backend
4. Eventually migrate to Docker Compose for full stack
