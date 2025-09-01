-- Initialize Shelfinity Databases and users

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
        CREATE USER keycloak WITH PASSWORD 'keycloak';
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'shelfinity') THEN
        CREATE USER shelfinity WITH PASSWORD 'shelfinity';
    END IF;
END
$$;

-- Create databases if they don't exist (using direct statements)
-- We'll use a different approach that works in PostgreSQL
CREATE DATABASE keycloak OWNER keycloak;
CREATE DATABASE shelfinity OWNER shelfinity;

-- Grant privileges on both DBs (connect)
GRANT CONNECT ON DATABASE keycloak TO keycloak;
GRANT CONNECT ON DATABASE shelfinity TO shelfinity;

-- Connect and grant schema privs
\c shelfinity
GRANT ALL ON SCHEMA public TO shelfinity;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO shelfinity;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO shelfinity;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c keycloak
GRANT ALL ON SCHEMA public TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;