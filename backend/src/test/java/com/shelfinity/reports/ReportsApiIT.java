/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reports;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.shelfinity.testsupport.ApiSupport;

/**
 * Black-box coverage for {@code ReportResource} / {@code ReportService} —
 * each of these endpoints injects and reads from multiple repositories
 * ({@code BookRepository}, {@code QueueRepository}, {@code UserRepository})
 * in one call, the scenario {@link com.shelfinity.persistence.EntityManagerProducer}'s
 * shared-transaction-scoped persistence context exists for.
 */
class ReportsApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "statistics",
            "book-popularity",
            "borrowing-trends",
            "user-activity",
            "author-distribution"
    })
    void reportEndpoint_asAdmin_returnsOk(String path) {
        given()
                .header("Authorization", "Bearer " + ApiSupport.adminToken())
        .when()
            .get("/reports/{path}", path)
        .then()
            .statusCode(200);
    }

    @Test
    void reportEndpoint_asNonAdmin_isForbidden() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/reports/statistics")
        .then()
            .statusCode(403);
    }

    @Test
    void reportEndpoint_withoutToken_isRejected() {
        given()
        .when()
            .get("/reports/statistics")
        .then()
            .statusCode(401);
    }
}
