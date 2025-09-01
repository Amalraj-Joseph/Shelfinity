# Patch Notes (v4.1 - API Fixes & Cleanup)

This patch addresses API endpoint issues and cleans up the project structure. It fixes:
- API endpoints now accessible at correct URLs
- Removed unnecessary backup files
- Updated documentation to reflect current API structure
- Known OpenAPI path prefix issue documented

## Changes

1. **docker/docker-compose.yml & docker-compose-shelfinity.yml**
   - Mount `init-db.sql` and added Postgres healthcheck
   - Configure Keycloak to use Postgres (`KC_DB*`) and import realm
   - Backend now receives DB and OIDC env vars and exposes `/health`
   - Start order: Postgres → Keycloak → Backend → Frontend

2. **docker/init-db.sql**
   - Idempotent creation of roles, DBs (`keycloak`, `shelfinity`) and schema grants

3. **backend/server.xml**
   - Enabled features: `mpOpenAPI`, `mpHealth`, `mpJwt`, `jdbc`, `persistence`
   - Configured `mpJwt` with `issuer`, `jwksUri`, `audiences`, `groupsPath=realm_access.roles`, `userNameAttribute=email`
   - Added PostgreSQL dataSource `jdbc/shelfinityDS`

4. **backend/microprofile-config.properties**
   - Ensures OpenAPI scanning is on

5. **scripts/dev-up.sh**
   - Convenience script to launch the stack

> If you previously started containers, run `docker compose -f docker/docker-compose.yml down -v` once to reset volumes.