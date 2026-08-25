/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

import io.restassured.http.ContentType;

/**
 * Black-box coverage for {@code UsersResource} / {@code UserRepository}.
 * Business-rule edge cases (self-registration approval gating, role
 * escalation prevention) already have thorough coverage in the mocked
 * {@code UsersResourceTest} unit suite; this class exists to prove the same
 * flows actually commit through the real CDI/JTA-backed
 * {@code EntityManagerProducer}, not a reflection-injected field.
 */
class UsersApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @Test
    void getCurrentUserProfile_returnsAuthenticatedCallersOwnRecord() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/users/me")
        .then()
            .statusCode(200)
            .body("email", equalTo("john.doe@shelfinity.com"));
    }

    @Test
    void getCurrentUserProfile_withoutToken_isRejected() {
        given()
        .when()
            .get("/users/me")
        .then()
            .statusCode(401);
    }

    @Test
    void getAllUsers_asAdmin_includesSeedData() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.adminToken())
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("email", org.hamcrest.Matchers.hasItem("admin@shelfinity.com"));
    }

    @Test
    void getAllUsers_asNonAdmin_isForbidden() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/users")
        .then()
            .statusCode(403);
    }

    @Test
    void adminCreateUpdateDeleteUser_roundTripsThroughRealDatabase() {
        String token = ApiSupport.adminToken();
        String uniqueEmail = "api-it-" + UUID.randomUUID() + "@shelfinity.com";
        String createJson = String.format("""
            {"keycloakId":"%s","email":"%s","name":"API-IT Fixture User","role":"USER"}
            """, UUID.randomUUID(), uniqueEmail);

        String id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createJson)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("email", equalTo(uniqueEmail))
            // Admin-created users are active immediately (SPEC.md §6.1/§10.3).
            .body("active", equalTo(true))
            .extract().path("id");

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .get("/users/{id}", id)
        .then()
            .statusCode(200)
            .body("email", equalTo(uniqueEmail));

        String updateJson = String.format("""
            {"keycloakId":"%s","email":"%s","name":"API-IT Fixture User (updated)","role":"USER"}
            """, UUID.randomUUID(), uniqueEmail);
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateJson)
        .when()
            .put("/users/{id}", id)
        .then()
            .statusCode(200)
            .body("name", equalTo("API-IT Fixture User (updated)"));

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .delete("/users/{id}", id)
        .then()
            .statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .get("/users/{id}", id)
        .then()
            .statusCode(404);
    }
}
