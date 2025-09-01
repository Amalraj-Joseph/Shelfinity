# Shelfinity: Fixes & Changes (Sep 1, 2025)

This patch makes the **Cursor-generated** backend + Docker setup actually run end‑to‑end with Postgres, Keycloak and the React UI.

## What changed

1. **Backend Docker build**: Switched to a multi‑stage Dockerfile that builds the WAR and bundles the PostgreSQL JDBC driver. The previous Dockerfile expected a pre‑built WAR (which didn’t exist in `docker compose build`) and missed the JDBC driver.
2. **Database wiring**: `server.xml` now reads DB host/port/name/user/password from environment variables. It no longer hardcodes `localhost`, which fails inside containers.
3. **Schema generation**: `persistence.xml` now sets `jakarta.persistence.schema-generation.database.action=update`, so JPA can create tables automatically for local/dev.
4. **Keycloak**: Added a ready‑to‑import realm (`docker/keycloak/realm-shelfinity.json`) and told Keycloak to `--import-realm` at startup. Also created demo users: `admin/admin`, `user/user`.
5. **Healthchecks**: Normalized the backend healthcheck to `/health`.
6. **Helper scripts**: `scripts/dev-up.sh` and `scripts/dev-down.sh` for quick start/stop.

## How to run

```bash
./scripts/dev-up.sh
# When done:
./scripts/dev-down.sh
```

- Frontend:  http://localhost:3000  
- Backend (OpenAPI UI): http://localhost:9080/openapi/ui  
- Keycloak:  http://localhost:8080  (admin/admin)

## Notes

- For production, consider reverting schema generation to `none` and managing DDL separately.
- If you later enforce backend JWT, add `mpJwt` to `server.xml` and configure `mp.jwt.verify.publickey.location` against the realm’s OIDC well‑known JWKS URL.
