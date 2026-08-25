/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.overdue;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

/**
 * Black-box coverage for {@code OverdueResource} — injects both
 * {@code BookRepository} and {@code UserRepository} to resolve each overdue
 * item's human-readable relations.
 */
class OverdueApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @Test
    void getAllOverdueItems_asAdmin_returnsOk() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.adminToken())
        .when()
            .get("/overdue")
        .then()
            .statusCode(200);
    }

    @Test
    void getAllOverdueItems_asNonAdmin_isForbidden() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/overdue")
        .then()
            .statusCode(403);
    }

    @Test
    void getMyOverdueItems_asAuthenticatedUser_returnsOk() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/overdue/my")
        .then()
            .statusCode(200);
    }

    @Test
    void getOverdueStats_asAdmin_returnsOk() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.adminToken())
        .when()
            .get("/overdue/stats")
        .then()
            .statusCode(200);
    }

    @Test
    void getAllOverdueItems_withoutToken_isRejected() {
        given()
        .when()
            .get("/overdue")
        .then()
            .statusCode(401);
    }
}
