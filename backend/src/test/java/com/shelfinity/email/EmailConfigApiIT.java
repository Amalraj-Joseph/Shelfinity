/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

import io.restassured.http.ContentType;

/**
 * Black-box coverage for {@code EmailConfigResource} / {@code EmailConfigRepository}
 * — one of the two repositories that moved from per-method {@code @Transactional}
 * to a single class-level {@code @Transactional} during the redesign (see
 * {@code EmailConfigRepository}'s class Javadoc). Exercises save, update,
 * activate, and delete — the methods that annotation used to sit on
 * individually — through a real container to confirm the class-level
 * annotation still wraps each of them in a transaction.
 */
class EmailConfigApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @Test
    void saveUpdateActivateDeleteConfig_asAdmin_roundTripsThroughRealDatabase() {
        String token = ApiSupport.adminToken();

        String createJson = """
            {"smtpHost":"smtp.api-it-test.invalid","smtpPort":587,
             "senderEmail":"noreply@api-it-test.invalid","senderName":"API-IT Smoke Test"}
            """;

        String id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createJson)
        .when()
            .post("/email/config")
        .then()
            .statusCode(200)
            .body("smtpHost", equalTo("smtp.api-it-test.invalid"))
            .extract().path("id");

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .get("/email/config")
        .then()
            .statusCode(200)
            .body("id", org.hamcrest.Matchers.hasItem(id));

        String updateJson = """
            {"smtpHost":"smtp.api-it-test.invalid","smtpPort":587,
             "senderEmail":"noreply@api-it-test.invalid","senderName":"API-IT Smoke Test (updated)"}
            """;
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateJson)
        .when()
            .put("/email/config/{id}", id)
        .then()
            .statusCode(200)
            .body("senderName", equalTo("API-IT Smoke Test (updated)"));

        given()
                .header("Authorization", "Bearer " + token)
                // No body, but the resource class declares @Consumes(JSON)
                // at class level and RESTEasy 415s a request with no
                // Content-Type at all, even for a bodyless POST.
                .contentType(ContentType.JSON)
        .when()
            .post("/email/config/{id}/activate", id)
        .then()
            // Returns a plain success message, not the updated config —
            // actual activation is verified via the /active GET below.
            .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .get("/email/config/active")
        .then()
            .statusCode(200)
            .body("id", equalTo(id));

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .delete("/email/config/{id}", id)
        .then()
            .statusCode(anyOf204Or200());
    }

    private static org.hamcrest.Matcher<Integer> anyOf204Or200() {
        return org.hamcrest.Matchers.anyOf(equalTo(200), equalTo(204));
    }

    @Test
    void getAllConfigs_asNonAdmin_isForbidden() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/email/config")
        .then()
            .statusCode(403);
    }

    @Test
    void saveConfig_withoutToken_isRejected() {
        // Same pattern as BooksResource — saveEmailConfig only checks
        // isCurrentUserAdmin(), so a missing token resolves to FORBIDDEN.
        given()
                .contentType(ContentType.JSON)
                .body("{\"smtpHost\":\"x\",\"senderEmail\":\"x@x.com\"}")
        .when()
            .post("/email/config")
        .then()
            .statusCode(403);
    }
}
