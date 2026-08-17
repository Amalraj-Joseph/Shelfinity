# Shelfinity — Authoritative Specification (v1)

**Status: AUTHORITATIVE.** This document is the single source of truth for Shelfinity's requirements, domain model, and API surface. It supersedes `SRS.md`, `Architecture.md`, `User_stories.md`, and `Flow_chart.md`, which are kept for historical context only and are marked accordingly. Where anything in this document conflicts with those files, this document wins. `api.yaml` is the machine-readable companion to this document and must stay in sync with it.

This spec was built by reconciling the five previously-independent documents against each other *and* against the actual backend implementation already running in `backend/src/main/java/com/shelfinity` — every entity, endpoint, DTO, and enum described below is verified against real code, not re-derived from scratch. Section 10 lists the handful of places where the code still needs to change to match this spec.

---

## 1. Why this document exists

A prior analysis of `SRS.md`, `Architecture.md`, `User_stories.md`, `Flow_chart.md`, and `api.yaml` found that all five described materially different systems for the same product: three incompatible API contracts, a mandatory-Kafka architecture with no basis in the running stack, a payment/fines feature that appeared in exactly one document, and a registration-approval workflow that three documents required and two silently skipped. Building against any single one of those documents risked implementing a system none of the others agreed with.

This document fixes that by being the *only* place scope decisions are recorded, and by treating the real, already-written backend as ground truth wherever a legacy document and the code disagreed. Section 11 records every decision that changed as a result, with the reasoning, so nobody re-litigates a settled question by accident.

---

## 2. Scope (v1)

**In scope:**
- Book catalog: browse, search, admin CRUD, CSV bulk upload.
- User accounts backed by Keycloak (OIDC), with a local profile/role cache.
- Borrow and return requests, both admin-approval-gated, via a single request queue.
- Book reservations for unavailable titles, with expiry and admin fulfillment.
- Overdue tracking (scheduled, in-process).
- Admin-configurable SMTP settings, with async email notifications.
- Admin reporting: book popularity, borrowing trends, user activity, library statistics, author distribution.
- Role-based access control: `user` and `admin`.

**Explicitly out of scope for v1** (see §12 for the full future roadmap):
- Payment processing and overdue fines/fees.
- Apache Kafka or any external message broker — v1 uses in-process async (CDI `@Asynchronous` / scheduled jobs) inside the single Liberty deployment.
- API versioning (`/api/v1/...`) — v1 ships unversioned under `/api`, matching the deployed `@ApplicationPath`. A version prefix is a breaking-change decision to make later, not a default.
- PDF report export (CSV/JSON only).
- A "guest" (unauthenticated browsing) role.
- Refresh-token rotation, HttpOnly session cookies, or any auth machinery beyond what Keycloak's standard OIDC token flow already provides — Keycloak is the identity provider; the backend does not reimplement session management.
- Multi-branch / multi-library support, i18n, and formal multi-jurisdiction compliance programs (GDPR *and* India's DPDP Act as parallel obligations) — see §9.6 for the interim compliance posture.

---

## 3. Actors and roles

| Role | Description | Enum value |
|---|---|---|
| **User** | A library patron. Can browse/search books, submit borrow/return/reservation requests, view their own queue items, overdue items, and reservations. | `UserRole.USER` |
| **Admin** | Library staff. Everything a User can do, plus: approve/reject queue items, manage the book catalog (including bulk upload), manage SMTP configuration, fulfill reservations, view all reports, and manage other users' roles. | `UserRole.ADMIN` |

There is no "guest" role in v1. Every endpoint except `GET /health`, `GET /books`, `GET /books/{id}`, `GET /books/search`, and `GET /books/available` requires an authenticated Keycloak-issued bearer token.

---

## 4. Identity and authentication model

**Keycloak is the system of record for credentials.** This is a deliberate, evidence-based departure from the original SRS's "app stores bcrypt-hashed passwords" model (§4, item 1) — the backend never sees, stores, or validates a plaintext password. It only validates Keycloak-issued JWTs.

- **Realm:** `shelfinity`. Registration is self-service at the Keycloak level (`registrationAllowed: true`), with `loginWithEmailAllowed: true` so users may authenticate with either username or email.
- **Token flow:** Frontend obtains a token from Keycloak's `/realms/shelfinity/protocol/openid-connect/token` endpoint (Resource Owner Password Credentials grant, per the public `shelfinity-frontend` client) and sends it as `Authorization: Bearer <token>` on every backend call.
- **Backend validation:** MicroProfile JWT (`mpJwt-2.1`) validates the token signature and claims against Keycloak's realm. `userNameAttribute="preferred_username"` (server.xml) — Keycloak's default `upn` claim is not populated, so this must stay pinned.
- **Roles:** Keycloak emits realm roles under `realm_access.roles`. A protocol mapper (`oidc-usermodel-realm-role-mapper`) flattens this into a `groups` claim so that both manual checks (`JwtUtil`) and container-managed `@RolesAllowed` work off the same data. Both mechanisms must be kept in sync if the role model changes — this cost real debugging time once already.
- **Local `users` table:** a profile cache, not a credential store. It exists to let SQL joins (queue items, reservations, reports) reference a stable `UUID` instead of the Keycloak subject string everywhere, and to hold app-specific fields Keycloak doesn't (local `role` cache, `active` flag — see §10.3, resolved). It has **no password column** and never will.
- **Password reset / change:** delegated entirely to Keycloak's own account console / required actions (`resetPasswordAllowed: true` in the realm). The backend does not expose `/password/change` — that was an SRS carry-over from the pre-Keycloak design and has been dropped as redundant. Do not re-add it.

---

## 5. Domain model

These are the actual JPA entities, verified against `backend/src/main/java/com/shelfinity`. Field names are exactly as coded.

### 5.1 `User` (table: `users`)
| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `keycloakId` | String, unique, not null | Keycloak subject (`sub` claim) |
| `email` | String, unique, not null | |
| `name` | String, not null | |
| `role` | enum `USER`/`ADMIN` | Cached from/synced with Keycloak realm roles |
| `active` | boolean, default `false` | Fail-closed; `UsersResource` sets it `true` immediately for admin-created accounts (§10.3, resolved) |
| `createdAt`, `updatedAt` | timestamps | |

### 5.2 `Book` (table: `books`)
| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `title`, `author` | String, not null | |
| `isbn` | String, unique | |
| `description` | String (1000) | |
| `available` | boolean, default `true` | Derived: `available && availableCopies > 0` |
| `totalCopies` | int, positive, default 1 | |
| `availableCopies` | int, default 1 | |
| `createdAt`, `updatedAt` | timestamps | |
| `genre` | String, nullable | Free-text, no fixed vocabulary (§10.4, resolved) |
| `publicationYear` | Integer, nullable | §10.4, resolved |

### 5.3 `QueueItem` (table: `queue_items`) — the admin approval queue
| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `type` | enum `USER_REGISTRATION`, `BOOK_BORROW`, `BOOK_RETURN`, `BOOK_RESERVATION` | See §6.2 — `USER_REGISTRATION` exists in code but is not yet produced by any endpoint |
| `userKeycloakId` | String, not null | |
| `bookId` | UUID, nullable | Null for `USER_REGISTRATION` |
| `status` | enum `PENDING`, `APPROVED`, `REJECTED` | |
| `description` | String (1000) | |
| `adminRemark` | String (1000) | |
| `dueDate` | timestamp, nullable | Set on borrow approval |
| `createdAt`, `updatedAt`, `processedAt`, `processedBy` | | |

One queue model handles registration approval, borrow, and return — this is deliberate: it's the same admin work pattern (review → approve/reject → notify) for all three, and the code already generalizes it this way. Reservations are **not** a `QueueItem` type in practice despite `BOOK_RESERVATION` existing in the enum — they're modeled as their own resource (§5.4) because their lifecycle (auto-expiry, fulfillment) doesn't fit the binary approve/reject pattern. Treat the `BOOK_RESERVATION` enum value as legacy/unused; do not build new logic against it.

### 5.4 `Reservation` (table: `reservations`)
| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `userKeycloakId`, `bookId` | not null | |
| `status` | enum `ACTIVE`, `NOTIFIED`, `FULFILLED`, `CANCELLED`, `EXPIRED` | |
| `expiresAt` | timestamp | Set by `ReservationResource` via `reservation.expiry.days` (default 7), not hardcoded (§10.5, resolved) |
| `notifiedAt` | timestamp, nullable | |
| `notes` | String, nullable | |
| `createdAt`, `updatedAt` | | |

### 5.5 `EmailConfig` (table: `email_config`)
| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `smtpHost`, `smtpPort` (default 587), `senderEmail` | not null | |
| `senderName`, `username`, `password` | nullable | `password` is AES/GCM-encrypted at rest and never returned by any GET (§10.6, resolved) |
| `useTls` (default true), `useSsl` (default false), `requireAuth` (default true) | | |
| `active` | boolean, default true | Only one config should be active at a time |
| `createdAt`, `updatedAt` | | |

Multiple configs can be stored (history/rollback); only the `active` one is used to send mail.

---

## 6. Business rules and state machines

### 6.1 Registration
1. User self-registers through Keycloak (or is invited). This creates the Keycloak identity — the backend is not involved and does not gate this step.
2. On first authenticated call, if no local `User` row exists for the Keycloak subject, the backend creates one via `POST /users` (implemented — §10.1 and §10.3, both resolved) with `active = false` and a `QueueItem(type=USER_REGISTRATION, status=PENDING)`.
3. Until an admin approves the queue item (`active` flips to `true`), the user can authenticate (`/auth/login`, `/auth/me` succeed) but is blocked from borrow/return/reservation endpoints with `403 {"error": "Account pending approval"}`.
4. Admin approves/rejects via the same `PATCH /queues/{id}/status` used for borrow/return. Approval sets `active=true`; rejection leaves the account disabled (does not delete it — an admin can still review it later).

This reverses what the running Keycloak realm currently does (immediate, ungated self-service) in favor of what three of the five legacy documents and the code's own `QueueType.USER_REGISTRATION` / `User.active` fields were clearly designed for. It is the only place this spec asks the *existing* data model to be driven differently than it currently is at the API layer — everything else in this section describes behavior the code already implements correctly.

### 6.2 Borrow request lifecycle
`PENDING → APPROVED → RETURNED` (via the return sub-flow), or `PENDING → REJECTED`.
1. User: `POST /queues` with `type=BOOK_BORROW`, `bookId`. Status starts `PENDING`.
2. Admin: `PATCH /queues/{id}/status` → `APPROVED` or `REJECTED`.
3. On `APPROVED`: `availableCopies` must decrement by 1; `dueDate` must be set (default loan period: 14 days from approval — externalize via config, do not hardcode).
4. On `REJECTED`: no inventory change.

Step 3 is implemented — see §10.2 (resolved).

### 6.3 Return request lifecycle
Same queue, `type=BOOK_RETURN`, `PENDING → APPROVED/REJECTED`.
1. User: `POST /queues` with `type=BOOK_RETURN`, `bookId`.
2. Admin: `PATCH /queues/{id}/status` → `APPROVED` must increment `availableCopies` by 1 and, if any `Reservation` for that book is `ACTIVE`, transition the oldest one to `NOTIFIED` and send the reservation-ready email.

Returns are **admin-approval-gated**, not automatic. This resolves the direct contradiction between `Flow_chart.md` (auto-completing returns) and `SRS.md`/`Architecture.md` (admin-verified returns) in favor of the latter, because the code's own `QueueType.BOOK_RETURN` + `QueueStatus` model only makes sense under an approval-gated design — an auto-completing return would never need a `PENDING` state at all.

Step 2's side effects are implemented — see §10.2 (resolved).

### 6.4 Reservation lifecycle
`ACTIVE → NOTIFIED → FULFILLED`, or `ACTIVE → CANCELLED` (by user or admin), or `ACTIVE/NOTIFIED → EXPIRED` (by scheduled job after `expiresAt`).
1. User: `POST /reservations` — rejected with `400` if the book currently has `availableCopies > 0` (reserve is only for unavailable books) or the user already holds an active reservation on that title.
2. On book return (§6.3) or admin action: oldest `ACTIVE` reservation → `NOTIFIED`, confirmation email sent, `expiresAt` reset to `NOTIFIED time + 7 days` (claim window).
3. Admin: `POST /reservations/{id}/fulfill` → `FULFILLED` (paired with the user separately submitting a `BOOK_BORROW` queue request to actually take the book).
4. User or admin: `DELETE /reservations/{id}` → `CANCELLED`.
5. Scheduled job: any reservation past `expiresAt` without being fulfilled → `EXPIRED`; the next `ACTIVE` reservation in line (if any) is promoted per step 2.

### 6.5 Overdue tracking
Scheduled job (in-process, no Kafka — see §12) periodically scans `QueueItem` rows with `type=BOOK_BORROW`, `status=APPROVED`, `dueDate < now`, flags them overdue, and sends reminder emails. `GET /overdue`, `GET /overdue/my`, `GET /overdue/stats` expose this to admins/users respectively.

---

## 7. API surface

`api.yaml` (this directory) is the authoritative machine-readable contract and has been rewritten to match this section exactly — it previously covered roughly a third of what's below. Base path: `/api` (unversioned — see §2). All endpoints require `Authorization: Bearer <Keycloak JWT>` unless marked public.

| Resource | Endpoints | Auth |
|---|---|---|
| Health | `GET /health` | Public |
| Auth | `POST /auth/login`, `GET /auth/validate`, `GET /auth/me` | Authenticated (validates the token, does not issue one — Keycloak issues tokens) |
| Users | `POST /users`, `GET /users`, `GET /users/{id}`, `GET /users/me`, `PUT /users/{id}`, `DELETE /users/{id}` | `POST` = self-service (own `keycloakId`, forced `role=USER`) or admin (any user, any role — §10.1); `GET all`/`PUT`/`DELETE` = admin only; `GET /me` = self |
| Books | `GET /books`, `GET /books/{id}`, `GET /books/search?q=`, `GET /books/available`, `POST /books`, `PUT /books/{id}`, `DELETE /books/{id}`, `POST /books/bulk-upload`, `GET /books/bulk-upload/template` | Reads public; writes/upload = admin |
| Queue | `POST /queues`, `GET /queues?status=&type=`, `GET /queues/{id}`, `GET /queues/my`, `PATCH /queues/{id}/status`, `DELETE /queues/{id}` | Create/read-own = user; list-all/update-status/delete = admin |
| Reservations | `POST /reservations`, `GET /reservations`, `GET /reservations/my`, `DELETE /reservations/{id}`, `POST /reservations/{id}/fulfill` | Create/read-own/cancel-own = user; list-all/fulfill = admin |
| Overdue | `GET /overdue`, `GET /overdue/my`, `GET /overdue/stats` | `/my` = user; others = admin |
| Email config | `POST /email/config`, `GET /email/config`, `GET /email/config/active`, `PUT /email/config/{id}`, `DELETE /email/config/{id}`, `POST /email/config/{id}/activate`, `POST /email/config/test` | Admin only |
| Reports | `GET /reports/book-popularity?limit=`, `GET /reports/borrowing-trends?days=`, `GET /reports/user-activity?limit=`, `GET /reports/statistics`, `GET /reports/author-distribution` | Admin only |

Full request/response schemas live in `api.yaml`.

---

## 8. Notifications

In-process, asynchronous email dispatch (CDI `@Asynchronous` methods on `EmailService`) — no message broker. Templates (subject/body copy) from the original `SRS.md` Appendix A2 are still good and are retained as-is; only the transport mechanism changed (direct async call instead of a Kafka event + separate consumer service).

| Event | Recipient | Trigger |
|---|---|---|
| Registration confirmation | User | Keycloak registration (sent by Keycloak itself, not this backend) |
| Registration pending / approved / rejected | User, Admin | §6.1 queue transitions |
| Borrow request acknowledgment / approved / rejected | User | §6.2 queue transitions |
| Return confirmation | User | §6.3 approval |
| Reservation confirmation / ready-for-pickup | User | §6.4 create / notify |
| Overdue reminder | User | §6.5 scheduled job |
| Admin queue alert | Admin | New `PENDING` queue item of any type |

---

## 9. Non-functional requirements (right-sized)

The original SRS's NFRs (1,000 concurrent users, 99.9% monthly uptime, geo-distributed encrypted backups, 2-hour RTO) were sized for an enterprise system with no connection to `As_is.md`'s description of a single library digitizing paper records. These are revised to be realistic defaults for a v1 production deployment; revisit if actual usage data justifies scaling them up.

| Area | Target |
|---|---|
| **9.1 Concurrency** | 200 concurrent users without degradation; API responses < 3s under normal load |
| **9.2 Availability** | 99.5% monthly uptime target; scheduled maintenance outside business hours |
| **9.3 Backup** | Daily automated PostgreSQL backup, encrypted at rest, retained 30 days |
| **9.4 Disaster recovery** | RTO: 4 hours. RPO: 24 hours |
| **9.5 Security** | HTTPS everywhere; RBAC via Keycloak roles; input validation (Bean Validation, already used on DTOs); no plaintext secrets (§10.6, resolved) |
| **9.6 Compliance** | Deployment-specific — this spec does not commit to GDPR or India's DPDP Act by default. Whoever operates a given deployment must state which regulation(s) apply to their user base and add the corresponding functional requirements (e.g., data export, right-to-deletion endpoints) at that time. This section intentionally stops short of promising compliance work with no matching functional requirement, which is what the original SRS did. |
| **9.7 Logging** | Structured application logs, 90-day retention, no PII/secrets in logs |
| **9.8 Portability** | Deployable on both Open Liberty and Payara; packaged as a Docker image |

---

## 10. Known gaps between this spec and the current code

These are the concrete changes needed before this system can be called production-ready. Nothing else in this document requires a code change — everything else already matches. Ranked by severity.

### 10.1 RESOLVED — privilege escalation via `POST /users`
Was: **no authorization check at all**, and `CreateUserRequest.role` was a free-text field the caller controlled, defaulting to `"USER"` but accepting `"ADMIN"` — any caller, including an unauthenticated one, could create a user record with `role=ADMIN`.
Fixed in `UsersResource.createUser()`: the endpoint now requires an authenticated caller; a non-admin caller may only create a record for their own `keycloakId` (self-service profile sync still works); and `role=ADMIN` is only honored when the caller is already an admin — any other caller's requested role is forced to `USER` regardless of what the request body asks for. This started as option (a) from the original write-up of this gap (endpoint stays directly callable, rather than becoming internal-only per option (b)); §10.3's later resolution then layered the substance of (b) on top — a self-service caller now gets `active=false` plus a registration queue item, so the endpoint is still public but its output is gated the way (b) intended.

### 10.2 RESOLVED — approving a borrow/return request has no effect on inventory
Was: `QueueResource.updateQueueItemStatus()` only set `status` and `processedAt`. It never decremented/incremented `Book.availableCopies`, never set `QueueItem.dueDate`, and never promoted a `Reservation` — approving a borrow did not make the book unavailable, and approving a return did not make it available again.
Fixed: `updateQueueItemStatus()` now applies §6.2 step 3 and §6.3 step 2 on the `PENDING → APPROVED` transition only (re-processing an already-decided item, or acting on any other transition, does not double-apply side effects). The logic was extracted into an injectable `QueueApprovalService` (unit-testable with mocked repositories) rather than living as private methods on the JAX-RS resource. `BOOK_BORROW` approval decrements `availableCopies`, rejects with `409` if none are available, and sets `dueDate` via the now-configurable `borrow.loan.days` (default 14). `BOOK_RETURN` approval increments `availableCopies` (capped at `totalCopies`) and promotes the oldest `ACTIVE` reservation for that book to `NOTIFIED`, resetting its claim-window expiry, sending the corresponding email in both the approve and promote cases. Both approval and rejection now send the borrower a status email via the existing `EmailService` methods, which is a small addition beyond the original scope of this gap but was a one-line, low-risk pairing with the state change.
Residual note: each side effect (`Book` update, `QueueItem` update, `Reservation` update) still runs as a separate CDI-managed transaction rather than one shared transaction — consistent with how the rest of the codebase already does multi-repository writes (e.g. `ReservationResource`), not a new risk introduced by this fix, but worth addressing if this code is rebuilt rather than patched.

### 10.3 RESOLVED (for §6.1) — registration approval not wired up
Was: `User.active` defaulted to `true` and nothing created a `QueueItem(type=USER_REGISTRATION)`.
Fixed: `User.active` now defaults to `false` (fail-closed). `UsersResource.createUser()` sets `active=true` immediately only when the caller is an admin creating the account; a self-service caller creating their own record gets `active=false` plus a `USER_REGISTRATION` queue item. `QueueApprovalService` gained a registration branch: approval sets `active=true` and sends a confirmation email; rejection sends a decline email and leaves the account inactive (not deleted). `QueueResource.createQueueItem` and `ReservationResource.createReservation` both now check the caller's local `active` flag and return `403 {"error": "Account pending approval"}` if it's false or the local record doesn't exist yet.

### 10.4 RESOLVED — Book catalog was missing `genre` and `publicationYear`
Fixed: both fields added to `Book`, `CreateBookRequest`, and `BookResponse`; `GET /books?genre=` filters case-insensitively (composable with `availableOnly`).

### 10.5 RESOLVED — Reservation expiry window was hardcoded
Fixed: `Reservation.prePersist()` no longer sets `expiresAt` (JPA lifecycle callbacks have no CDI/Config access). `ReservationResource.createReservation` sets it explicitly via `reservation.expiry.days` (default 7, env override `RESERVATION_EXPIRY_DAYS`). The same config value drives the claim-window reset in `QueueApprovalService` when a reservation is promoted to `NOTIFIED` (§6.4 step 2), which `ReservationRepository.markAsNotified` now takes as an explicit parameter instead of leaving `expiresAt` untouched.

### 10.6 RESOLVED — SMTP credentials were stored in plaintext
Fixed: `EmailConfig.password` is now AES/GCM-encrypted at rest via a JPA `AttributeConverter` (`EncryptedStringConverter`), keyed by `email.config.encryption.key` (env override `EMAIL_CONFIG_ENCRYPTION_KEY` — the shipped default is dev-only and explicitly insecure). `EmailConfigResource` now returns a new `EmailConfigResponse` DTO on every read path instead of the raw entity, so the password is never echoed back (matches `api.yaml`'s `writeOnly: true`). `PUT /email/config/{id}` preserves the existing password when the request omits one, rather than blanking it — a real bug this fix would otherwise have introduced, since no GET response can pre-fill a password field for a typical edit-then-submit UI flow.

### 10.7 No account-deletion endpoint
No `DELETE /users/me` (self-service) exists. Given §9.6 no longer commits to a specific compliance regime by default, this is not currently a blocking gap — but if a deployment opts into GDPR/DPDP-style obligations, this endpoint is a prerequisite and should be built then, not assumed to already exist.

### 10.8 RESOLVED — `UUID` columns didn't bind correctly against real Postgres
Not part of the original gap audit — found by the repository test tier (Testcontainers, real Postgres), which is exactly the class of bug mocks can't catch. Every entity's `@Id private UUID id` had no explicit type hint. EclipseLink bound `UPDATE`/`DELETE ... WHERE id = ?` parameters as `VARCHAR` against a native Postgres `uuid` column, which Postgres rejects (`operator does not exist: character varying = uuid`) — `SELECT`-by-id worked fine, only the merge/remove path was affected. This means **every "delete a user/book/reservation" and "update an entity" operation in the running application was silently broken**, independent of anything in the original §10 list.

The fix went through two iterations, both instructive:
1. First attempt: `@Column(columnDefinition = "uuid")` on the `@Id` fields. This fixed `@Id`-based UPDATE/DELETE, but (a) turned out to also be *required*, not optional, on the non-`@Id` `bookId` columns (`QueueItem`, `Reservation`) for plain `WHERE bookId = ?` equality filters — reverting it there to "fix" a different problem re-broke those — and (b) broke persisting a **null** `bookId` (the real case for `USER_REGISTRATION` queue items), because `columnDefinition` only affects DDL generation, not the JDBC parameter-binding path EclipseLink chose for null vs. non-null values inconsistently.
2. Final fix: a proper EclipseLink-native converter, `com.shelfinity.persistence.PostgresUuidConverter`, that sets the underlying `DatabaseField`'s SQL type to `Types.OTHER` directly (what the PostgreSQL JDBC driver expects for `uuid`) — the actual root cause, handled uniformly for null and non-null, comparisons, inserts, and updates. Applied via `@Convert("postgresUuid")` to all five `@Id` fields and to `QueueItem.bookId`/`Reservation.bookId`.

### 10.9 RESOLVED — `GET /books/available` (and `availableOnly=true`) could return books with zero copies left
Also found by `BookRepositoryIT`. `Book.findAvailable` (the named query behind both endpoints) filtered on the `available` boolean column alone. Nothing in the borrow/return flow ever sets that column — `QueueApprovalService` only changes `availableCopies` — so once a book's last copy was borrowed, it kept appearing in availability list results even though `Book.isAvailable()` (used for the per-book JSON field) correctly derived `false` from `availableCopies`. Fixed by changing the named query to `WHERE b.available = true AND b.availableCopies > 0`, matching `isAvailable()`'s logic.

### 10.10 RESOLVED — reservation promotion on book return never actually worked
Found by `ReservationRepositoryIT`, not by the mocked `QueueApprovalServiceTest` — mocks don't execute real JPQL, which is the whole point of having a repository tier. `Reservation.findActiveByBookId` — the exact query `QueueApprovalService.applyReturnApproval()` calls to find the next reservation to promote (§6.3 step 2 / §6.4 step 2) — compared `r.status` against a bare `'ACTIVE'` string literal instead of a bound parameter. EclipseLink can't compare a string literal against an enum-mapped column and throws at query-prepare time, so **every return approval on a book with an active reservation would have thrown at runtime**, well before §10.2's fix ever got a chance to promote anyone. Fixed by switching to a bound `:status` parameter, same pattern as the original `QueueRepository.countPending` fix from earlier in this project. A sweep for the same anti-pattern elsewhere also caught `QueueItem.findPending` (bare `'PENDING'` literal) — currently dead code with no caller, but fixed anyway rather than left as a landmine.

---

## 11. Decisions log

Recorded so future readers understand *why*, not just *what*, and don't reopen settled questions without new information.

1. **Auth model: Keycloak/OIDC, not local bcrypt+JWT.** The original SRS assumed the app stores and validates passwords itself. The actual system already runs a fully configured Keycloak realm (protocol mappers, pinned user IDs, MP-JWT validation wired into Liberty). Rebuilding local credential storage to match a paper spec, when a working, more secure IdP integration already exists, would be a pure regression. Decided by evidence, not asked.
2. **Fees/payments: cut from v1.** Asked the product owner directly — appeared in `Flow_chart.md` only, nowhere else, with no compliance/PCI groundwork elsewhere in the docs. Answer: cut, park as future scope (§12).
3. **Async architecture: in-process, not Kafka.** Asked the product owner directly — `Architecture.md` made Kafka a hard requirement; nothing in the real `docker-compose` stack runs it, and `As_is.md`'s single-library scale doesn't obviously need it. Answer: in-process for v1, Kafka documented as a future scaling option only (§12).
4. **Registration approval: gated, not self-service.** Reversed from an earlier draft of this analysis, which initially favored self-service because the live Keycloak realm allows immediate registration. Upgraded to gated once the code's own `QueueType.USER_REGISTRATION` enum value and `User.active` flag were found — both are meaningless under a self-service model, and only make sense if registration was always intended to flow through the same approval queue as borrow/return. Decided by evidence from the data model, not asked, since it reconciles code intent with 3 of the 5 legacy documents.
5. **Return flow: admin-approval-gated, not automatic.** `Flow_chart.md` showed an automatic return; `SRS.md`/`Architecture.md` required admin sign-off. The code's `QueueType.BOOK_RETURN` + `QueueStatus.PENDING/APPROVED/REJECTED` model only makes sense under the gated interpretation — an auto-completing return would never need a pending state. Decided by evidence.
6. **API base path: unversioned `/api`, not `/api/v1`.** `Architecture.md` wanted versioning; the deployed `@ApplicationPath` and every existing resource class use unversioned paths. Changing this now is a bigger breaking change than the value it adds at this stage. Decided by evidence; can be revisited at a genuine v2 boundary.
7. **Dynamic admin role reassignment: kept.** Low cost — `PUT /users/{id}` already lets an admin set a user's role, so no new work is required. Decided without asking; reversible at zero cost since it's already implemented.
8. **Report export formats: CSV/JSON, not CSV/PDF.** `SRS.md` §5.2 said CSV/PDF; `Architecture.md` §6.6 said CSV/JSON. No PDF-generation library is in the codebase and none of the five real report endpoints return anything but JSON today. Decided by evidence — JSON is what exists; CSV is a cheap addition; PDF is dropped.
9. **NFRs right-sized** per §9 — decided by evidence from `As_is.md`'s stated scale, not asked, since these are defaults explicitly marked as revisable rather than hard commitments.

---

## 12. Out of scope / future roadmap

Not committed to for v1; listed so they aren't lost, and so nobody mistakes "not now" for "never":
- Payment processing and fine collection, if the business decides overdue fines are worth the compliance surface.
- Apache Kafka (or another broker) if/when notification volume or the need to decouple email delivery from API latency actually materializes — the in-process model in §8 should be the trigger point to revisit this, not a target date.
- API versioning (`/api/v2`) at the point a genuinely breaking change is needed.
- PDF report export, if stakeholders need offline/printable reports beyond CSV/JSON.
- Multi-branch/multi-library support, i18n.
- A formal compliance program (GDPR, India's DPDP Act, or both) once a specific deployment's jurisdiction is known — see §9.6.
- Rate limiting / CAPTCHA on auth-adjacent endpoints, called out as a nice-to-have in the legacy Architecture doc's own security checklist but never required.

---

## 13. Traceability

| Legacy requirement source | Status here |
|---|---|
| `SRS.md` §3.1–3.13 (functional requirements) | Covered by §5–8, with deviations recorded in §11 |
| `SRS.md` §6 (NFRs) | Superseded by §9 |
| `SRS.md` Appendix A2 (email templates) | Retained as-is, referenced in §8 |
| `Architecture.md` §6 (module responsibilities) | Covered by §5–6, Kafka-specific parts superseded by §8/§11.3 |
| `Architecture.md` §8 (DB design) | Superseded by §5 (matches real entities) |
| `Architecture.md` §9 (security design) | Superseded by §9.5 and §10 (only what's actually true/needed kept) |
| `User_stories.md` (all stories) | Covered by §6; every story now has a concrete state machine and endpoint, closing the earlier gap where reservation/inventory stories had no matching use case |
| `Flow_chart.md` (all flows) | Covered by §6, with #17 (payments) and #15 (role reassignment, trivially — §11.7) resolved |
