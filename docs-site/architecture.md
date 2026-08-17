---
title: Architecture
description: Service topology, technology stack, and the data model behind Shelfinity.
permalink: /architecture/
---

# Architecture

## Service topology

Four containers, one Docker Compose file, no message broker. Requests fan out
from the browser to the API and to Keycloak directly for token issuance.

```
┌──────────────┐        ┌──────────────────┐
│   Browser    │───────▶│  React frontend    │  :3000
│              │        │  (nginx-served)     │
└──────┬───────┘        └─────────┬──────────┘
       │  Bearer token                     │  REST calls
       ▼                                   ▼
┌──────────────┐        ┌──────────────────┐
│  Keycloak     │◀──────▶│  Open Liberty     │  :9080
│  (realm:      │  JWT   │  backend           │
│  shelfinity)  │ verify │  (Jakarta EE 10)   │
└──────────────┘  keys  └─────────┬──────────┘
     :8080                        │  JPA / EclipseLink
                                   ▼
                          ┌──────────────────┐
                          │   PostgreSQL       │  :5432
                          └──────────────────┘
```

The frontend never talks to Postgres or issues its own tokens — it exchanges
credentials with Keycloak's token endpoint directly (Resource Owner Password
Credentials grant against the public `shelfinity-frontend` client) and sends
the resulting JWT as a bearer token on every backend call. The backend
validates that JWT against Keycloak's realm keys via MicroProfile JWT; it
never sees a password.

## Technology choices

| Layer | Technology | Why |
|---|---|---|
| Backend runtime | Open Liberty 24, Jakarta EE 10 (JAX-RS, JPA/EclipseLink, CDI) | Portable Jakarta EE app; also deployable on Payara without code changes |
| Backend auth | MicroProfile JWT against Keycloak-issued tokens | No credential storage in the app; RBAC comes straight from Keycloak realm roles |
| Database | PostgreSQL 15 | One relational store for users, books, the approval queue, reservations, and email config |
| Async work | CDI `@Asynchronous` + scheduled jobs, in-process | No message broker needed at this scale — see the specification's decision log for when that would change |
| Frontend | React 18, MUI, React Router | Component-driven UI with real client-side routing for the growing set of admin views |
| Identity | Keycloak (realm `shelfinity`) | Full OIDC provider: registration, login, password reset, and role management, instead of reimplementing any of that |

## Domain model

The approval queue is the one modeling decision worth calling out: **registration, borrowing, and returns share a single `QueueItem` table**, reviewed through the same PENDING → APPROVED/REJECTED workflow, because they're the same admin work pattern — review, decide, notify. Reservations are *not* a queue item type in practice, despite a legacy enum value suggesting otherwise: their lifecycle (auto-expiry, fulfillment, promotion to the next person in line) doesn't fit a binary approve/reject shape, so they're modeled as their own resource.

| Entity | Table | Role |
|---|---|---|
| `User` | `users` | Profile cache keyed by Keycloak subject — no password column |
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
- **Frontend unit/component** (Jest + React Testing Library) — the API client, auth context, and every page.
- **Frontend end-to-end** (Playwright) — full flows against the real, running Docker Compose stack: sign-in, borrowing, admin approval, registration.
