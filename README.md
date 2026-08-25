# Shelfinity - Library Management System

[![CI](https://github.com/Amalraj-Joseph/Shelfinity/actions/workflows/ci.yml/badge.svg)](https://github.com/Amalraj-Joseph/Shelfinity/actions/workflows/ci.yml)

A full-stack library management system built with Jakarta EE 10, React 18, PostgreSQL, and Keycloak — deployable locally with Docker Compose or to a free-tier multi-cloud stack with only configuration changes (see [Multi-cloud deployment architecture](#multi-cloud-deployment-architecture) below).

📖 **[Documentation](https://shelfinity.amalraj.dev)** — getting started, architecture, business rules, and the full API reference.

### Built with

![React](https://img.shields.io/badge/React-20232a?style=flat-square&logo=react&logoColor=61DAFB)
![MUI](https://img.shields.io/badge/MUI-007FFF?style=flat-square&logo=mui&logoColor=white)
![Jakarta EE](https://img.shields.io/badge/Jakarta_EE-ED6C00?style=flat-square)
![Open Liberty](https://img.shields.io/badge/Open_Liberty-6929C4?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![IBM Db2](https://img.shields.io/badge/IBM_Db2-0F62FE?style=flat-square)
![Keycloak](https://img.shields.io/badge/Keycloak-4D4D4D?style=flat-square&logo=keycloak&logoColor=white)
![IBM App ID](https://img.shields.io/badge/IBM_App_ID-0F62FE?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white)
![Cloudflare](https://img.shields.io/badge/Cloudflare-F38020?style=flat-square&logo=cloudflare&logoColor=white)
![Oracle Cloud Infrastructure](https://img.shields.io/badge/Oracle_Cloud_Infrastructure-C74634?style=flat-square)
![Apache Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![Jest](https://img.shields.io/badge/Jest-C21325?style=flat-square&logo=jest&logoColor=white)
![Playwright](https://img.shields.io/badge/Playwright-2EAD33?style=flat-square)

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

## Multi-cloud deployment architecture

Shelfinity runs unmodified on either stack below — switching between them is a matter of environment variables and build args, never a code change. Locally, `docker-compose.yml` wires up Postgres, Keycloak, and the backend/frontend containers on one machine. In the cloud, the same backend WAR and React build run against IBM Cloud's managed services, fronted by Cloudflare, on a single free-tier Oracle Cloud VM:

![Shelfinity multi-cloud deployment topology](docs/images/multicloud-architecture.svg)

- **Cloudflare** — DNS, Universal SSL, and a [Cloudflare Tunnel](https://www.cloudflare.com/products/tunnel/) (`cloudflared`) carry all public traffic to the VM without opening an inbound port on it.
- **Oracle Cloud Infrastructure (OCI)** — the one Always Free Compute VM that actually runs the app: `nginx` serves the React production build and proxies `/api` to Open Liberty, which runs natively under systemd (no Docker in this path — see `deploy/JNDI_RACE_DEBUGGING_SUMMARY.md` for why). At boot, a small standalone module (`backend/vault-bootstrap`) authenticates as the VM itself via Instance Principals and pulls every secret — the Db2 password, App ID config, the email encryption key — from **OCI Vault**, so nothing sensitive is ever stored on disk or in the systemd unit.
- **IBM Cloud** — the two managed services the backend talks to: **Db2 on Cloud** for the database (over SSL, via the CA cert in `backend/certs/`) and **App ID** for OIDC identity, using an Authorization Code + PKCE flow suited to a static SPA (see `frontend/src/auth/pkce.js` and `AuthContext.js`).

Swapping stacks is entirely config-driven: `DB_JDBC_URL`/`DB_DRIVER_CLASS` select Postgres vs. Db2, `REACT_APP_AUTH_FLOW` selects Keycloak's ROPC flow vs. App ID's PKCE flow, and `.env.example` documents every variable for both. See `deploy/deploy-to-vm.sh` and `deploy/vm-setup.sh` for the two scripts that build and install the cloud stack.

## License

MIT — see [LICENSE.txt](LICENSE.txt).

---

### Thanks

Shelfinity's cloud deployment runs entirely on generous free tiers. Thank you to:

<p>
  <a href="https://www.oracle.com/cloud/free/"><img src="docs/images/oracle-logo.svg" alt="Oracle Cloud Infrastructure" height="40"></a>
  &nbsp;&nbsp;&nbsp;
  <a href="https://www.ibm.com/cloud/free"><img src="docs/images/ibm-logo.svg" alt="IBM Cloud" height="40"></a>
  &nbsp;&nbsp;&nbsp;
  <a href="https://www.cloudflare.com/plans/free/"><img src="docs/images/cloudflare-logo.svg" alt="Cloudflare" height="40"></a>
</p>

**Oracle Cloud Infrastructure** for the Always Free compute instance this app runs on and its Vault service, **IBM Cloud** for the Lite Db2 and App ID plans backing its data and identity, and **Cloudflare** for the free Tunnel and DNS/TLS that put it on the internet safely — without any of the three, this project's cloud deployment wouldn't exist.
