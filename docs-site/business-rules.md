---
title: Business Rules
description: The exact state machines behind registration, borrowing, returns, and reservations.
permalink: /business-rules/
---

# Business rules

These are the state machines that actually run in the codebase, not an
aspirational design. Each section links the endpoints involved — full request
and response shapes are in the [API Reference]({{ '/api-reference/' | relative_url }}).

## Registration

1. A user self-registers through Keycloak's hosted registration page (linked from the login screen). The backend is not involved in this step at all — Keycloak owns the identity.
2. On the user's first authenticated call, the backend creates a local profile via `POST /users` with `active=false` and a `USER_REGISTRATION` queue item.
3. Until an admin approves that queue item, the user can log in and view their own profile, but is blocked from borrow, return, and reservation endpoints with `403 {"error": "Account pending approval"}`.
4. An admin approves or rejects it via `PATCH /queues/{id}/status`, the same endpoint used for borrow/return decisions. Approval sets `active=true`. Rejection leaves the account disabled — it is not deleted, so an admin can revisit the decision later.

An account created directly by an admin (rather than through self-registration) skips this queue entirely and is `active=true` immediately, since an admin creating the account has already vetted it.

## Borrowing

`PENDING → APPROVED` (with inventory effects) or `PENDING → REJECTED` (no effect).

1. User: `POST /queues` with `type=BOOK_BORROW` and a `bookId`. Blocked with `409` if the user already has a pending borrow request for that same book.
2. Admin: `PATCH /queues/{id}/status` to `APPROVED` or `REJECTED`.
3. On approval: `availableCopies` decrements by one (rejected with `409` if no copies are available at decision time), and `dueDate` is set from a configurable loan period (default 14 days).
4. On rejection: no inventory change. The requester is emailed either way.

## Returning

Same queue, `type=BOOK_RETURN`, same `PENDING → APPROVED/REJECTED` shape — returns are **admin-approval-gated, not automatic**, so a return only actually frees up inventory once staff confirm the book is back.

1. User: `POST /queues` with `type=BOOK_RETURN` and the `bookId`.
2. Admin: `PATCH /queues/{id}/status` to `APPROVED` increments `availableCopies` by one (capped at `totalCopies`) and, if anyone holds an `ACTIVE` reservation on that title, promotes the oldest one to `NOTIFIED` and emails them that it's ready.

## Reservations

`ACTIVE → NOTIFIED → FULFILLED`, or `ACTIVE → CANCELLED`, or `ACTIVE`/`NOTIFIED → EXPIRED`.

1. User: `POST /reservations`. Rejected with `400` if the book currently has copies available (reservations are for unavailable titles only) or the user already holds an active reservation on it.
2. When a copy becomes available (a return is approved, or an admin acts directly): the oldest `ACTIVE` reservation becomes `NOTIFIED`, a confirmation email goes out, and a fresh claim window starts (`expiresAt` reset to notify-time + the configured expiry, default 7 days).
3. Admin: `POST /reservations/{id}/fulfill` marks it `FULFILLED` once the patron actually checks the book out — paired with the patron separately submitting a normal `BOOK_BORROW` request.
4. User or admin: `DELETE /reservations/{id}` cancels it at any point.
5. A scheduled job expires any reservation past its claim window without being fulfilled, and promotes the next person in line, if any.

## Overdue tracking

A scheduled, in-process job periodically scans approved borrow requests whose `dueDate` has passed, flags them, and sends reminder emails. `GET /overdue`, `GET /overdue/my`, and `GET /overdue/stats` expose the results to admins and users respectively — no separate "overdue" entity exists; it's a computed view over the same queue data.

## Roles

| Role | Can do |
|---|---|
| **User** | Browse/search the catalog, submit borrow/return/reservation requests, view their own queue items, overdue items, and reservations |
| **Admin** | Everything a User can, plus: approve/reject queue items, manage the catalog (including CSV bulk upload), configure SMTP, fulfill reservations, view all reports, and reassign other users' roles |

Every endpoint except health, book browsing, and book search requires an authenticated, Keycloak-issued bearer token — there is no unauthenticated "guest" role.
