/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.testsupport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Shared setup for the {@code *ApiIT} black-box suite (mvn verify -Pe2e):
 * real HTTP against an already-running stack (docker-compose up
 * postgres/keycloak/backend), no mocks, no reflection-injected fields. This
 * is deliberately the one tier that exercises the actual CDI container —
 * {@link com.shelfinity.persistence.EntityManagerProducer}'s
 * {@code @TransactionScoped} producer, {@code @Inject}-based repository
 * wiring, and the {@code @Transactional} interceptor — none of which the
 * unit tests (mocked/reflection-injected repositories) or the *RepositoryIT
 * tier (plain JPA, no app server) ever touch. That gap is exactly what let
 * WELD-000053 and the JNDI race both reach a real VM before being caught;
 * see deploy/JNDI_RACE_DEBUGGING_SUMMARY.md.
 *
 * Base URLs are overridable system properties so this suite can point at
 * any running stack — local docker-compose (the defaults, matching
 * docker/docker-compose.yml) or a deployed VM — without code changes:
 *   mvn verify -Pe2e -Dapi.base.url=https://shelfinity-app.amalraj.dev/api
 *                     -Dkeycloak.base.url=https://shelfinity-app.amalraj.dev
 */
public final class ApiSupport {

    public static final String KEYCLOAK_BASE_URL =
            System.getProperty("keycloak.base.url", "http://localhost:8080");
    private static final String REALM = "shelfinity";
    private static final String CLIENT_ID = "shelfinity-frontend";

    // realm-shelfinity.json / seed-data.sql fixture accounts.
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String USER_USERNAME = "john.doe";
    public static final String USER_PASSWORD = "john123";
    public static final String SECONDARY_USER_USERNAME = "jane.smith";
    public static final String SECONDARY_USER_PASSWORD = "jane123";

    /** The API base URL with its trailing "/api" stripped — for endpoints outside that path, e.g. /health. */
    public static final String BACKEND_ROOT;

    private static final Map<String, String> TOKEN_CACHE = new ConcurrentHashMap<>();

    static {
        String apiBaseUrl = System.getProperty("api.base.url", "http://localhost:9080/api");
        RestAssured.baseURI = apiBaseUrl;
        BACKEND_ROOT = apiBaseUrl.endsWith("/api")
                ? apiBaseUrl.substring(0, apiBaseUrl.length() - "/api".length())
                : apiBaseUrl;
    }

    private ApiSupport() {
    }

    /** Forces static init (baseURI assignment) to run before the first request. */
    public static void init() {
        // no-op; triggers the static block above
    }

    public static String adminToken() {
        return token(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public static String userToken() {
        return token(USER_USERNAME, USER_PASSWORD);
    }

    public static String secondaryUserToken() {
        return token(SECONDARY_USER_USERNAME, SECONDARY_USER_PASSWORD);
    }

    /** Password-grant token from the real Keycloak instance; cached per (username) for the JVM's lifetime. */
    public static String token(String username, String password) {
        return TOKEN_CACHE.computeIfAbsent(username, u -> fetchToken(u, password));
    }

    private static String fetchToken(String username, String password) {
        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "password");
        form.put("client_id", CLIENT_ID);
        form.put("username", username);
        form.put("password", password);

        return io.restassured.RestAssured.given()
                .baseUri(KEYCLOAK_BASE_URL)
                .contentType(ContentType.URLENC)
                .formParams(form)
                .post("/realms/" + REALM + "/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }
}
