---
title: Architecture
description: Deployment models, service topology, technology stack, and the data model behind Shelfinity.
permalink: /architecture/
---

# Architecture

Shelfinity is one codebase — one backend WAR, one React build — deployable
three different ways. Which database and identity provider it talks to is
selected entirely through environment variables and build args; nothing in
the source changes between them.

## Deployment models

| | Runs on | Database | Identity | Script |
|---|---|---|---|---|
| **Local** | your machine, Docker Compose | PostgreSQL (container) | Keycloak (container) | `scripts/start.sh` |
| **Hybrid** | your machine, Docker Compose | Db2 on Cloud | IBM Cloud App ID | `scripts/start-cloud-services.sh` |
| **Multi-cloud** | one Oracle Cloud (OCI) VM, systemd | Db2 on Cloud | IBM Cloud App ID | `deploy/deploy-to-vm.sh` + `deploy/vm-setup.sh` |

Two environment variable pairs are what actually switch models:
`DB_JDBC_URL`/`DB_DRIVER_CLASS` select PostgreSQL vs. Db2 (`server.xml`'s
`dataSource` targets the plain `java.sql.Driver` SPI rather than a
vendor-specific bean, specifically so this works), and `REACT_APP_AUTH_FLOW`
selects Keycloak's Resource Owner Password Credentials (ROPC) flow vs. App
ID's Authorization Code + PKCE flow.

### Local

Four containers, one Docker Compose file, no message broker. Requests fan
out from the browser to the API and to Keycloak directly for token
issuance.

![Local deployment topology]({{ '/assets/images/local-deployment-topology.svg' | relative_url }})

The frontend never talks to Postgres or issues its own tokens — it
exchanges credentials with Keycloak's token endpoint directly (ROPC grant
against the public `shelfinity-frontend` client) and sends the resulting
JWT as a bearer token on every backend call. The backend validates that JWT
against Keycloak's realm keys via MicroProfile JWT; it never sees a
password.

### Hybrid

The app itself — frontend and backend — still runs as local containers,
but the database and identity provider are the real managed IBM Cloud
services instead of local containers. Useful for developing or testing
against Db2 and App ID directly without deploying anywhere.

![Hybrid deployment topology]({{ '/assets/images/hybrid-deployment-topology.svg' | relative_url }})

App ID requires the Authorization Code + PKCE flow rather than ROPC: its
password-grant flow needs a client secret, which a static single-page app
has nowhere safe to keep. The frontend only ever redirects the browser to
App ID's hosted authorization page (a page navigation, never subject to
CORS); the actual code-for-token exchange and the UserInfo lookup happen
server-side, via the backend's `POST /auth/oidc-exchange` — App ID's token
endpoint sends no CORS headers at all, so a direct browser call to it isn't
possible regardless of client type.

### Multi-cloud

The full deployment: the app itself also moves off your machine, onto a
single free-tier Oracle Cloud Infrastructure (OCI) Compute VM, fronted by
Cloudflare.

![Multi-cloud deployment topology]({{ '/assets/images/multicloud-deployment-topology.svg' | relative_url }})

- **Cloudflare** — DNS, Universal SSL, and a Cloudflare Tunnel (`cloudflared`)
  carry all public traffic to the VM without opening an inbound port on it.
- **Oracle Cloud Infrastructure** — the VM runs `nginx` (serving the React
  production build and proxying `/api`) and Open Liberty natively under
  systemd, not in Docker on this one path — the free-tier Always Free VM's
  1GB of RAM doesn't leave room for a Docker daemon and duplicated image
  layers on top of the app itself. At boot, a small standalone
  module (`backend/vault-bootstrap`) authenticates as the VM itself via
  Instance Principals and pulls every secret — the Db2 password, App ID
  config, the email encryption key — from **OCI Vault**, so nothing
  sensitive is ever stored on disk or in the systemd unit.
- **IBM Cloud** — Db2 on Cloud (reached over SSL, verified against a CA
  certificate bundled into the backend's own truststore at build time) and
  App ID for identity, same PKCE flow as the hybrid model.

## Technology choices

| Layer | Technology | Why |
|---|---|---|
| Backend runtime | Open Liberty, Jakarta EE 10 (JAX-RS, JPA/EclipseLink, CDI) | Portable Jakarta EE app; a `kernel-slim` base with an explicit feature list keeps the cloud image lean |
| Backend auth | MicroProfile JWT against Keycloak- or App ID-issued tokens | No credential storage in the app; RBAC comes from the token's own claims regardless of provider |
| Database | PostgreSQL (local/hybrid dev) or Db2 on Cloud (hybrid/multi-cloud), selected via `DB_JDBC_URL`/`DB_DRIVER_CLASS` | One vendor-neutral persistence layer; swapping databases is a config change, not a code change |
| Persistence | `EntityManagerProducer` hands out a `@TransactionScoped` `EntityManager` built from a `@Resource`-injected `DataSource`, rather than a container-managed persistence unit | Sidesteps a Liberty JNDI bootstrap race hit during cloud validation — see `deploy/JNDI_RACE_DEBUGGING_SUMMARY.md` |
| Async work | CDI `@Asynchronous` + scheduled jobs, in-process | No message broker needed at this scale — see the specification's decision log for when that would change |
| Frontend | React 18, MUI, React Router | Component-driven UI with real client-side routing for the growing set of admin views |
| Identity | Keycloak (local/hybrid dev, realm `shelfinity`) or IBM Cloud App ID (hybrid/multi-cloud), selected via `REACT_APP_AUTH_FLOW` | Full OIDC either way; App ID is the managed option for the cloud deployment models |
| Secrets (multi-cloud only) | OCI Vault, fetched by `backend/vault-bootstrap` via Instance Principals | No credential file ever lives on the VM's disk |

## Domain model

The approval queue is the one modeling decision worth calling out: **registration, borrowing, and returns share a single `QueueItem` table**, reviewed through the same PENDING → APPROVED/REJECTED workflow, because they're the same admin work pattern — review, decide, notify. Reservations are *not* a queue item type in practice, despite a legacy enum value suggesting otherwise: their lifecycle (auto-expiry, fulfillment, promotion to the next person in line) doesn't fit a binary approve/reject shape, so they're modeled as their own resource.

| Entity | Table | Role |
|---|---|---|
| `User` | `users` | Profile cache keyed by the identity provider's subject claim — no password column |
| `Book` | `books` | Catalog entry with derived availability (`available && availableCopies > 0`) |
| `QueueItem` | `queue_items` | The one approval queue: registration, borrow, and return requests |
| `Reservation` | `reservations` | Hold on an unavailable book, with expiry and fulfillment tracking |
| `EmailConfig` | `email_config` | Admin-managed SMTP settings; password AES/GCM-encrypted at rest |

See [Business Rules]({{ '/business-rules/' | relative_url }}) for the state machines each of these drives, and the repository's
[`docs/api/SPEC.md`](https://github.com/{{ site.repository }}/blob/main/docs/api/SPEC.md#5-domain-model)
for the exact field list.

## Testing strategy

Three backend tiers plus two frontend tiers, chosen to get real coverage
without needing a full application-server-in-Maven integration harness:

- **Backend unit** (JUnit 5 + Mockito) — services, validation, resource-layer logic, no I/O.
- **Backend repository** (Testcontainers, real PostgreSQL) — named queries and JPA mappings, run via the `repository-it` Maven profile.
- **Backend black-box** (REST Assured, real HTTP against an already-running stack) — the `*ApiIT` suite, run via the `e2e` Maven profile, exercises the full CDI container end to end — `EntityManagerProducer`'s transaction-scoped persistence and every `@Transactional` interceptor — against either the local Docker stack or a deployed instance via `-Dapi.base.url`.
- **Frontend unit/component** (Jest + React Testing Library) — the API client, auth context (both the ROPC and PKCE flows), and every page.
- **Frontend end-to-end** (Playwright) — full flows against the real, running Docker Compose stack: sign-in, borrowing, admin approval, registration.
