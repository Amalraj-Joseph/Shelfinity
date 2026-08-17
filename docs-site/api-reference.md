---
title: API Reference
description: Every Shelfinity resource, endpoint, and access rule. Base path is unversioned /api.
---

# API Reference

Base path: **`/api`** (unversioned — see the specification's decision log for why).
Every endpoint except the ones tagged <span class="badge badge-public">public</span> requires
`Authorization: Bearer <Keycloak JWT>`. For live, try-it-out request/response
schemas, run the stack and open **http://localhost:9080/openapi/ui/**.

<span class="badge badge-public">public</span> no auth required &nbsp;
<span class="badge badge-user">user</span> any authenticated user &nbsp;
<span class="badge badge-admin">admin</span> admin role required

## Health

| Method &amp; path | Access | Notes |
|---|---|---|
| `GET /health` | <span class="badge badge-public">public</span> | Liveness check |

## Auth

| Method &amp; path | Access | Notes |
|---|---|---|
| `POST /auth/login` | <span class="badge badge-user">user</span> | Validates the caller's token and returns their local profile |
| `GET /auth/validate` | <span class="badge badge-user">user</span> | Token validity check |
| `GET /auth/me` | <span class="badge badge-user">user</span> | Current user's profile |

Tokens themselves are issued by Keycloak, not this API — see [Architecture]({{ '/architecture/' | relative_url }}).

## Users

| Method &amp; path | Access | Notes |
|---|---|---|
| `POST /users` | <span class="badge badge-user">user</span> | Self-service profile sync (own `keycloakId` only, `role` forced to `USER`) or admin-created account (any role) |
| `GET /users` | <span class="badge badge-admin">admin</span> | List all users |
| `GET /users/{id}` | <span class="badge badge-admin">admin</span> | Look up a user |
| `GET /users/me` | <span class="badge badge-user">user</span> | Current user's own record |
| `PUT /users/{id}` | <span class="badge badge-admin">admin</span> | Update a user, including role reassignment |
| `DELETE /users/{id}` | <span class="badge badge-admin">admin</span> | Remove a user |

## Books

| Method &amp; path | Access | Notes |
|---|---|---|
| `GET /books` | <span class="badge badge-public">public</span> | List/filter (`genre`, `availableOnly`) |
| `GET /books/{id}` | <span class="badge badge-public">public</span> | Single book |
| `GET /books/search?q=` | <span class="badge badge-public">public</span> | Title/author search |
| `GET /books/available` | <span class="badge badge-public">public</span> | Books with copies currently available |
| `POST /books` | <span class="badge badge-admin">admin</span> | Create a book |
| `PUT /books/{id}` | <span class="badge badge-admin">admin</span> | Update a book |
| `DELETE /books/{id}` | <span class="badge badge-admin">admin</span> | Remove a book |
| `POST /books/bulk-upload` | <span class="badge badge-admin">admin</span> | CSV bulk import |
| `GET /books/bulk-upload/template` | <span class="badge badge-admin">admin</span> | Download the expected CSV shape |

## Queue (registration, borrow, return)

| Method &amp; path | Access | Notes |
|---|---|---|
| `POST /queues` | <span class="badge badge-user">user</span> | Submit a `BOOK_BORROW` or `BOOK_RETURN` request; blocked with `403` if the caller's account is not yet approved |
| `GET /queues?status=&type=` | <span class="badge badge-admin">admin</span> | List/filter all queue items |
| `GET /queues/{id}` | <span class="badge badge-admin">admin</span> | Look up a queue item |
| `GET /queues/my` | <span class="badge badge-user">user</span> | Current user's own queue items |
| `PATCH /queues/{id}/status` | <span class="badge badge-admin">admin</span> | Approve or reject — drives the inventory/registration side effects in [Business Rules]({{ '/business-rules/' | relative_url }}) |
| `DELETE /queues/{id}` | <span class="badge badge-admin">admin</span> | Remove a queue item |

## Reservations

| Method &amp; path | Access | Notes |
|---|---|---|
| `POST /reservations` | <span class="badge badge-user">user</span> | Reserve an unavailable book |
| `GET /reservations` | <span class="badge badge-admin">admin</span> | List all reservations |
| `GET /reservations/my` | <span class="badge badge-user">user</span> | Current user's own reservations |
| `DELETE /reservations/{id}` | <span class="badge badge-user">user</span> | Cancel — own reservation, or any reservation as admin |
| `POST /reservations/{id}/fulfill` | <span class="badge badge-admin">admin</span> | Mark fulfilled once the patron checks the book out |

## Overdue tracking

| Method &amp; path | Access | Notes |
|---|---|---|
| `GET /overdue` | <span class="badge badge-admin">admin</span> | All overdue items |
| `GET /overdue/my` | <span class="badge badge-user">user</span> | Current user's overdue items |
| `GET /overdue/stats` | <span class="badge badge-admin">admin</span> | Aggregate overdue statistics |

## Email configuration

| Method &amp; path | Access | Notes |
|---|---|---|
| `POST /email/config` | <span class="badge badge-admin">admin</span> | Create an SMTP configuration |
| `GET /email/config` | <span class="badge badge-admin">admin</span> | List configurations (password never included) |
| `GET /email/config/active` | <span class="badge badge-admin">admin</span> | Currently active configuration |
| `PUT /email/config/{id}` | <span class="badge badge-admin">admin</span> | Update — omitting `password` preserves the existing one |
| `DELETE /email/config/{id}` | <span class="badge badge-admin">admin</span> | Remove a configuration |
| `POST /email/config/{id}/activate` | <span class="badge badge-admin">admin</span> | Make this the active configuration |
| `POST /email/config/test` | <span class="badge badge-admin">admin</span> | Send a test email |

## Reports

| Method &amp; path | Access | Notes |
|---|---|---|
| `GET /reports/book-popularity?limit=` | <span class="badge badge-admin">admin</span> | Most-borrowed books |
| `GET /reports/borrowing-trends?days=` | <span class="badge badge-admin">admin</span> | Borrow/return volume over time |
| `GET /reports/user-activity?limit=` | <span class="badge badge-admin">admin</span> | Most active users |
| `GET /reports/statistics` | <span class="badge badge-admin">admin</span> | Library-wide counters |
| `GET /reports/author-distribution` | <span class="badge badge-admin">admin</span> | Catalog breakdown by author |

Reports return JSON only (no PDF export) — see the specification's decision log for why.
