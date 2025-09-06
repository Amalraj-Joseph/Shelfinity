# ---- Build stage -------------------------------------------------------------
# FROM maven:3.9-eclipse-temurin-21 AS build

ARG MAVEN_IMAGE=public.ecr.aws/docker/library/maven:3.9-eclipse-temurin-21
FROM ${MAVEN_IMAGE} AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src

# Build app, package Liberty server ZIP, and prepare wlp/ for copying
RUN mvn -B -DskipTests clean package liberty:package

# ---- Runtime stage (Open Liberty base) --------------------------------------
FROM icr.io/appcafe/open-liberty:kernel-slim-java21-openj9-ubi-minimal

# Copy server config from packaged server into /config (overlay for defaultServer)
# Also copy shared libs (JDBC driver) and the freshly built WAR
COPY --chown=1001:0 --from=build /build/target/liberty/wlp/usr/servers/defaultServer/ /config/
COPY --chown=1001:0 --from=build /build/target/classes/META-INF/microprofile-config.properties /config/microprofile-config.properties
COPY --chown=1001:0 --from=build /build/target/liberty/wlp/usr/shared/resources/lib/ /opt/ol/wlp/usr/shared/resources/lib/
COPY --chown=1001:0 --from=build /build/target/shelfinity-backend.war /config/apps/

# Install features now (runs featureUtility under the hood)
RUN features.sh
RUN configure.sh
# (If you prefer explicitly:)  # RUN /opt/ol/wlp/bin/featureUtility installServerFeatures defaultServer

EXPOSE 9080
# Base image entrypoint runs the server automatically (no CMD needed)
