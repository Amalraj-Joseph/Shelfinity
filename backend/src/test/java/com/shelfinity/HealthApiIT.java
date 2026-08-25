/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

/**
 * Sanity check that the stack under test actually came up — the same
 * "expect 401/200, not 500 or a hang" check used manually after every VM
 * deploy, now checked-in rather than run by hand each time. If this fails,
 * every other *ApiIT class's failures are noise; fix this first.
 */
class HealthApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @Test
    void health_reportsUp() {
        given()
        .when()
            // /health lives outside the /api base path (server.xml root vs.
            // ShelfinityApplication's @ApplicationPath).
            .baseUri(ApiSupport.BACKEND_ROOT)
            .get("/health")
        .then()
            .statusCode(200)
            .body("status", org.hamcrest.Matchers.equalTo("UP"));
    }
}
