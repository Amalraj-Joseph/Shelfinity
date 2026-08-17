# Shelfinity - Library Management System

[![CI](https://github.com/Amalraj-Joseph/Shelfinity/actions/workflows/ci.yml/badge.svg)](https://github.com/Amalraj-Joseph/Shelfinity/actions/workflows/ci.yml)

A full-stack library management system built with Jakarta EE 10, React 18, PostgreSQL, and Keycloak.

📖 **[Documentation](https://shelfinity.amalraj.dev)** — getting started, architecture, business rules, and the full API reference.

## Quick start

```bash
git clone git@github.com:Amalraj-Joseph/Shelfinity.git
cd Shelfinity
./scripts/start.sh
```

This builds and starts Postgres, Keycloak, the backend, and the frontend, and waits for the backend to report healthy. Then open **http://localhost:3000**.

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `john.doe` | `john123` | User |

> Change these credentials before deploying anywhere beyond local development.

To stop everything: `./scripts/stop.sh` (add `-v` to also drop the database volume).

See **[shelfinity.amalraj.dev](https://shelfinity.amalraj.dev)** for architecture, the exact business rules (registration approval, borrow/return, reservations), the full API reference, and the authoritative [`docs/api/SPEC.md`](docs/api/SPEC.md).

## Running the tests

```bash
cd backend && mvn clean verify              # unit tests + coverage gate
cd backend && mvn test -Prepository-it      # repository tests (Testcontainers)
cd frontend && npm test -- --watchAll=false # unit/component tests
cd frontend && npx playwright test          # end-to-end (stack must be running)
```

## License

MIT — see [LICENSE.txt](LICENSE.txt).
