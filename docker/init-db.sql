-- Initialize Shelfinity Database
-- This script creates the necessary databases and users

-- Create Keycloak user
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
        CREATE USER keycloak WITH PASSWORD 'keycloak';
    END IF;
END
$$;

-- Create Shelfinity user
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'shelfinity') THEN
        CREATE USER shelfinity WITH PASSWORD 'shelfinity';
    END IF;
END
$$;

-- Create Shelfinity database
SELECT 'CREATE DATABASE shelfinity'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'shelfinity')\gexec

-- Grant privileges to keycloak user on postgres database
GRANT ALL PRIVILEGES ON DATABASE postgres TO keycloak;
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;

-- Grant privileges to shelfinity user on shelfinity database
GRANT ALL PRIVILEGES ON DATABASE shelfinity TO shelfinity;

-- Connect to Shelfinity database
\c shelfinity;

-- Grant schema privileges to shelfinity user
GRANT ALL ON SCHEMA public TO shelfinity;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO shelfinity;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO shelfinity;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO shelfinity;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO shelfinity;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- The actual tables will be created by JPA/Hibernate
-- This is just for initial setup
