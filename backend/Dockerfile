# --- Build stage ---
FROM registry.access.redhat.com/ubi9/openjdk-21:latest AS build
WORKDIR /build

# Whether to bundle the Postgres JDBC driver. Local dev (docker-compose,
# unset — defaults true) needs it; a cloud image built against Db2 only
# doesn't, and skipping it keeps the final image a few hundred KB leaner.
ARG INCLUDE_POSTGRES_DRIVER=true

# Install Maven
USER root
RUN microdnf install -y maven && microdnf clean all

COPY pom.xml .
# Cache deps first
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
COPY certs ./certs
# Package WAR and copy the JDBC driver jar(s) into target/lib. Both ship by
# default so the vendor-neutral server.xml dataSource (DB_JDBC_URL-driven)
# can bind to whichever one matches at runtime — see server.xml's comment —
# unless INCLUDE_POSTGRES_DRIVER=false trims it for a Db2-only cloud image.
#
# Also build a truststore: start from the JDK's own cacerts, then import any
# CA certs dropped in backend/certs/ on top. Used two ways (see server.xml):
#   1. DB_JDBC_URL's sslTrustStoreLocation, for managed databases that issue
#      a private/self-signed CA rather than a publicly trusted one.
#   2. Liberty's own default outbound TLS trust (mpJwt's JWKS fetch, etc).
#      This one is non-obvious: the *runtime* image's own bundled JDK cacerts
#      turned out to be missing the CA chain for a real external host
#      (us-south.appid.cloud.ibm.com, DigiCert-issued — a perfectly ordinary
#      public cert), which surfaced as CWWKS6051E "PKIX path building
#      failed" fetching App ID's JWKS. Building the truststore from *this*
#      stage's JDK (a full, non-minimal image) sidesteps whatever's
#      incomplete in the runtime image's own cacerts, regardless of the
#      actual root cause there.
# backend/certs/ is empty by default, making the per-cert loop a no-op.
RUN mvn -B -DskipTests package && \
    mkdir -p target/lib && \
    if [ "$INCLUDE_POSTGRES_DRIVER" = "true" ]; then \
        mvn -q -DskipTests dependency:copy -Dartifact=org.postgresql:postgresql:42.7.5 -DoutputDirectory=target/lib; \
    fi && \
    mvn -q -DskipTests dependency:copy -Dartifact=com.ibm.db2:jcc:12.1.5.0 -DoutputDirectory=target/lib && \
    cp "${JAVA_HOME}/lib/security/cacerts" target/lib/app-truststore.jks && \
    for f in certs/*.pem certs/*.crt; do \
        [ -f "$f" ] || continue; \
        keytool -importcert -noprompt -trustcacerts \
            -alias "custom-$(basename "$f")" \
            -file "$f" \
            -keystore target/lib/app-truststore.jks \
            -storepass changeit; \
    done

# --- Runtime stage ---
# kernel-slim, not full: full bundles Jakarta EE features this app never uses
# (JSF/Faces, WebSocket, Enterprise Beans Lite among them) and costs ~330MB
# more for it. kernel-slim needs its exact feature set resolved and installed
# at build time instead (via features.sh below, Open Liberty's own documented
# pattern — openliberty.io/guides/containerize.html) since it has no bundled
# umbrella profile to fall back on.
FROM icr.io/appcafe/open-liberty:kernel-slim-java21-openj9-ubi-minimal

# server.xml first: features.sh reads its <featureManager> to know what to
# install. /config is this image's own symlink to the default server's
# config root (verified: -> /opt/ol/wlp/usr/servers/defaultServer) — using
# it instead of the raw path is the documented convention for this pattern.
# --chown matches the image's own non-root default user (1001:0).
COPY --chown=1001:0 server.xml /config/

RUN features.sh

# The WAR must be copied after features.sh, not before — features.sh installs
# from server.xml's declared feature list, not by inspecting the app.
COPY --chown=1001:0 --from=build /build/target/shelfinity-backend.war /config/apps/

# JDBC drivers and the truststore (see build stage) go to the shared library
# dir server.xml's JdbcLib fileset reads from — unrelated to feature
# installation, so ordering relative to features.sh doesn't matter here.
RUN mkdir -p /opt/ol/wlp/usr/shared/resources/lib
COPY --chown=1001:0 --from=build /build/target/lib/*.jar /opt/ol/wlp/usr/shared/resources/lib/
COPY --chown=1001:0 --from=build /build/target/lib/app-truststore.jks /opt/ol/wlp/usr/shared/resources/lib/

# configure.sh's default behavior bakes an OpenJ9 shared class cache into the
# image (~70MB) purely to shave cold-start time — a tradeoff that makes sense
# for scale-to-zero/serverless, not for one long-running server container
# that isn't restarting on every request. Disabled for image size.
ENV OPENJ9_SCC=false
RUN configure.sh

EXPOSE 9080

# kernel-slim-minimal doesn't include wget, unlike the full image — curl is
# present instead.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:9080/health || exit 1

CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
