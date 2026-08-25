# Multi-stage build for React app
# Node 20, matching CI (.github/workflows/ci.yml) — serialize-javascript's
# patched line (fixing GHSA-5c6j-r48x-rmvq, an RCE) requires Node >=20 as of
# every 7.x release, so this can't stay on 18 once that override is applied
# (see package.json's "overrides").
FROM node:20-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install --only=production

# Copy source code
COPY . .

# Create React App bakes REACT_APP_* variables into the bundle at build time —
# setting them in docker-compose's `environment:` block has no effect on this
# already-built, nginx-served static image. Accept them as build ARGs instead
# so docker-compose's `build.args` can actually reach the bundle.
ARG REACT_APP_KEYCLOAK_URL=http://localhost:8080
ARG REACT_APP_REALM=shelfinity
ARG REACT_APP_CLIENT_ID=shelfinity-frontend
ARG REACT_APP_API_BASE_URL=/api
# Defaults to 'ropc' (Keycloak, today's behavior) with an empty endpoint, so
# a build with no cloud-specific args is unaffected — see AuthContext.js's
# AUTH_FLOW comment and .env.example for the App ID / 'pkce' values. Only the
# authorization endpoint is needed client-side (a page redirect, not a
# fetch); the token exchange itself is backend-side — see backend/.env.example.
ARG REACT_APP_AUTH_FLOW=ropc
ARG REACT_APP_OIDC_AUTHORIZATION_ENDPOINT=
ENV REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} \
    REACT_APP_REALM=${REACT_APP_REALM} \
    REACT_APP_CLIENT_ID=${REACT_APP_CLIENT_ID} \
    REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL} \
    REACT_APP_AUTH_FLOW=${REACT_APP_AUTH_FLOW} \
    REACT_APP_OIDC_AUTHORIZATION_ENDPOINT=${REACT_APP_OIDC_AUTHORIZATION_ENDPOINT}

# Build the app
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built app from build stage
COPY --from=build /app/build /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
