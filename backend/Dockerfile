# --- Build stage ---
FROM registry.access.redhat.com/ubi9/openjdk-21:latest AS build
WORKDIR /build

# Install Maven
USER root
RUN microdnf install -y maven && microdnf clean all

COPY pom.xml .
# Cache deps first
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
# Package WAR and copy the PostgreSQL JDBC driver into target/lib
RUN mvn -B -DskipTests package && \
    mkdir -p target/lib && \
    mvn -q -DskipTests dependency:copy -Dartifact=org.postgresql:postgresql:42.7.5 -DoutputDirectory=target/lib

# --- Runtime stage ---
FROM icr.io/appcafe/open-liberty:full-java21-openj9-ubi-minimal

# Copy server configuration
COPY server.xml /opt/ol/wlp/usr/servers/defaultServer/

# Copy the application
COPY --from=build /build/target/shelfinity-backend.war /opt/ol/wlp/usr/servers/defaultServer/apps/

# Copy JDBC driver to shared library dir
RUN mkdir -p /opt/ol/wlp/usr/shared/resources/lib
COPY --from=build /build/target/lib/postgresql-*.jar /opt/ol/wlp/usr/shared/resources/lib/

EXPOSE 9080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9080/health || exit 1

CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
