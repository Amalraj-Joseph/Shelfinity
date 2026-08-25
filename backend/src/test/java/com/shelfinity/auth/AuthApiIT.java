/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

/**
 * Black-box coverage for {@code AuthResource}'s Keycloak/ropc-mode
 * endpoints (/validate, /me) — both resolve the caller's DB user record via
 * {@code UserRepository.findByKeycloakId}, the same CDI-injected repository
 * path exercised throughout this suite. The App ID/pkce-mode
 * {@code /oidc-exchange} endpoint is intentionally not covered here: it's
 * only reachable against a real App ID tenant, not local Keycloak — see its
 * own Javadoc in AuthResource.
 */
class AuthApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @Test
    void validateToken_withValidToken_resolvesDbUser() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/auth/validate")
        .then()
            .statusCode(200)
            .body("email", equalTo("john.doe@shelfinity.com"));
    }

    @Test
    void validateToken_withoutToken_isRejected() {
        given()
        .when()
            .get("/auth/validate")
        .then()
            .statusCode(401);
    }

    @Test
    void getCurrentUser_withValidToken_resolvesDbUser() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.adminToken())
        .when()
            .get("/auth/me")
        .then()
            .statusCode(200)
            .body("email", equalTo("admin@shelfinity.com"))
            .body("role", equalTo("ADMIN"));
    }

    @Test
    void getCurrentUser_withoutToken_isRejected() {
        given()
        .when()
            .get("/auth/me")
        .then()
            .statusCode(401);
    }
}
