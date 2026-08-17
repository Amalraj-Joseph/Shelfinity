---
title: Getting Started
description: Run the full Shelfinity stack locally with Docker Compose.
permalink: /getting-started/
---

# Getting Started

Shelfinity ships as four containers — PostgreSQL, Keycloak, an Open Liberty
backend, and a React frontend — orchestrated by Docker Compose. Nothing else
is required to run the whole system locally.

## Prerequisites

- Docker and Docker Compose
- ~4 GB of free RAM for the stack (Keycloak and Postgres are the heaviest)

## 1. Clone and start the stack

```bash
git clone git@github.com:{{ site.repository }}.git
cd Shelfinity
docker compose -f docker/docker-compose.yml up -d --build
```

The first build compiles the backend with Maven and installs the frontend's
npm dependencies inside their respective images, so it takes a few minutes.
Subsequent starts are fast.

## 2. Wait for the backend health check

```bash
curl http://localhost:9080/api/health
```

A `{"status":"UP", ...}` response means Postgres, Keycloak, and the backend
are all up and the backend has successfully validated its connection to both.

## 3. Open the app

Visit **http://localhost:3000**. Sign in with one of the seeded accounts:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `john.doe` | `john123` | User |
| `jane.smith` | `jane123` | User |

New accounts go through Keycloak's own hosted registration page (linked from
the login screen) and start with `active=false` until an admin approves the
registration request from the **Requests** queue — see
[Business Rules → Registration]({{ '/business-rules/' | relative_url }}#registration) for why.

## Where things run

| Service | URL | Purpose |
|---|---|---|
| Frontend | http://localhost:3000 | React app |
| Backend API | http://localhost:9080/api | JAX-RS resources |
| Backend OpenAPI UI | http://localhost:9080/openapi/ui/ | Interactive API explorer |
| Keycloak | http://localhost:8080 | Identity provider, realm `shelfinity` |
| PostgreSQL | localhost:5432 | Application database |

## Running the tests

The backend and frontend each have their own test suites; nothing here
requires the Docker stack to be running unless noted.

```bash
# Backend unit tests + coverage gate
cd backend && mvn clean verify

# Backend repository tests (Testcontainers spins up its own Postgres)
cd backend && mvn test -Prepository-it

# Frontend unit/component tests
cd frontend && CI=true npm test -- --watchAll=false

# Frontend end-to-end tests (requires the Docker stack running)
cd frontend && npx playwright test
```

## Local backend/frontend development

Running the pieces outside Docker is also supported, for tighter edit/reload
loops:

```bash
# Backend: needs Java 21 and Maven; still talks to the Dockerized Postgres/Keycloak
cd backend && mvn liberty:dev

# Frontend: needs Node 18+; talks to whichever backend REACT_APP_API_BASE_URL points at
cd frontend && npm install && npm start
```

## Shutting down

```bash
docker compose -f docker/docker-compose.yml down
```

Add `-v` to also drop the Postgres volume and start from a clean seeded
database next time.
