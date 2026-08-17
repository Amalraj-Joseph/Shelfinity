---
title: Overview
hero: true
description: Shelfinity is a self-hosted library management system built on Jakarta EE, React, PostgreSQL, and Keycloak.
---

## What Shelfinity is

Shelfinity is a full-stack library management system for a single library or
small library network. Patrons browse the catalog and submit borrow, return,
and reservation requests; staff review a single approval queue, manage the
catalog, configure notification email, and pull reports. Identity is handled
entirely by [Keycloak](https://www.keycloak.org/) — the backend validates
Keycloak-issued JWTs and never stores a password.

<div class="grid-cards">
  <div class="card reveal">
    <span class="icon">📖</span>
    <h3>Catalog &amp; requests</h3>
    <p>Browse, search, and filter the catalog. Submit borrow, return, and reservation requests that route into a single admin approval queue.</p>
  </div>
  <div class="card reveal">
    <span class="icon">🗂️</span>
    <h3>One approval queue</h3>
    <p>Registration, borrowing, and returns share one review → approve/reject → notify workflow, so staff have a single place to work from.</p>
  </div>
  <div class="card reveal">
    <span class="icon">🔔</span>
    <h3>Reservations &amp; overdue tracking</h3>
    <p>Reserve a title that's checked out, get notified when it's available, and let a scheduled job track and remind on overdue loans automatically.</p>
  </div>
  <div class="card reveal">
    <span class="icon">📊</span>
    <h3>Admin reporting</h3>
    <p>Book popularity, borrowing trends, user activity, author distribution, and library-wide statistics — no spreadsheet exports required.</p>
  </div>
  <div class="card reveal">
    <span class="icon">🔐</span>
    <h3>Keycloak-backed identity</h3>
    <p>OIDC authentication via Keycloak. The app keeps a local profile cache for joins and role checks, but never sees a plaintext password.</p>
  </div>
  <div class="card reveal">
    <span class="icon">✉️</span>
    <h3>Configurable notifications</h3>
    <p>Admin-managed SMTP configuration drives async email for every state change — request outcomes, reservation readiness, overdue reminders.</p>
  </div>
</div>

## Why it's built this way

Every behavior documented on this site is verified against the running
codebase, not aspirational. The full specification — including the exact
business rules, entity fields, and the reasoning behind decisions like
"why Keycloak instead of a homegrown login" — lives in
[`docs/api/SPEC.md`](https://github.com/{{ site.repository }}/blob/main/docs/api/SPEC.md)
in the repository. This site is the readable front door to that spec, plus
a getting-started path and a browsable API reference.

## Where to go next

- **New to the project?** Start with [Getting Started]({{ '/getting-started/' | relative_url }}) to run the full stack locally with Docker Compose.
- **Want the system design?** [Architecture]({{ '/architecture/' | relative_url }}) covers the service topology and technology choices.
- **Building against the API?** [API Reference]({{ '/api-reference/' | relative_url }}) lists every resource, endpoint, and access rule.
- **Need the exact state machines?** [Business Rules]({{ '/business-rules/' | relative_url }}) walks through registration approval, borrowing, returns, and reservations end to end.
