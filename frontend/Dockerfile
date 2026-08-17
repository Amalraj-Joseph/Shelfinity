# Multi-stage build for React app
FROM node:18-alpine AS build

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
ENV REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} \
    REACT_APP_REALM=${REACT_APP_REALM} \
    REACT_APP_CLIENT_ID=${REACT_APP_CLIENT_ID} \
    REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL}

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
