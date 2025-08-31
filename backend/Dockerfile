# Liberty base image for Shelfinity Backend
FROM icr.io/appcafe/open-liberty:full-java21-openj9-ubi-minimal

# Copy the pre-built WAR file (built locally)
COPY target/shelfinity-backend.war /opt/ol/wlp/usr/servers/defaultServer/apps/

# Copy server configuration
COPY server.xml /opt/ol/wlp/usr/servers/defaultServer/

# Expose port
EXPOSE 9080

# Health check - simple port check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9080/health/ || exit 1

# Start the server
CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
