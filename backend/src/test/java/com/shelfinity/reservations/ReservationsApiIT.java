/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

import io.restassured.http.ContentType;

/**
 * Black-box coverage for {@code ReservationResource} / {@code ReservationRepository}
 * — the other repository that moved from per-method {@code @Transactional}
 * to class-level {@code @Transactional} during the redesign. Uses its own
 * admin-created, zero-availability fixture book so it never depends on (or
 * mutates) the shared seed-data reservation rows.
 */
class ReservationsApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    private static String createUnavailableFixtureBook(String adminToken) {
        String bookId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Reservation API-IT Fixture\",\"author\":\"Test Author\",\"totalCopies\":1}")
        .when()
            .post("/books")
        .then()
            .statusCode(201)
            .extract().path("id");

        // Borrow the only copy (as a different user) so availableCopies drops
        // to 0 — reservations are only allowed on unavailable books.
        String secondaryToken = ApiSupport.secondaryUserToken();
        String queueItemId = given()
                .header("Authorization", "Bearer " + secondaryToken)
                .contentType(ContentType.JSON)
                .body("{\"type\":\"BOOK_BORROW\",\"bookId\":\"" + bookId + "\",\"description\":\"make unavailable\"}")
        .when()
            .post("/queues")
        .then()
            .statusCode(201)
            .extract().path("id");
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"status\":\"APPROVED\"}")
        .when()
            .patch("/queues/{id}/status", queueItemId)
        .then()
            .statusCode(200);

        return bookId;
    }

    @Test
    void createAndCancelReservation_roundTripsThroughRealDatabase() {
        String adminToken = ApiSupport.adminToken();
        String userToken = ApiSupport.userToken();
        String bookId = createUnavailableFixtureBook(adminToken);

        String reservationId = given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body("{\"bookId\":\"" + bookId + "\",\"notes\":\"api-it smoke test\"}")
        .when()
            .post("/reservations")
        .then()
            .statusCode(201)
            .body("status", equalTo("ACTIVE"))
            .extract().path("id");

        given()
                .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/reservations/my")
        .then()
            .statusCode(200)
            .body("id", org.hamcrest.Matchers.hasItem(reservationId));

        given()
                .header("Authorization", "Bearer " + userToken)
        .when()
            .delete("/reservations/{id}", reservationId)
        .then()
            .statusCode(204);

        // No further cleanup: cancelling a reservation only soft-cancels it
        // (ReservationResource has no hard-delete endpoint — seed data's own
        // CANCELLED/EXPIRED rows are permanent too, by the same design), and
        // the fixture book still has that cancelled reservation and the
        // borrow queue item referencing it, so deleting it would risk an FK
        // violation rather than a clean 204. Fixture books from this class
        // accumulate in a local dev stack across repeated -Pe2e runs;
        // harmless there, and not a concern for a real deployment, which
        // never runs this suite against its own database.
    }

    @Test
    void createReservation_forAvailableBook_isRejected() {
        // Any book with copies available (e.g. seed data's "The Great Gatsby")
        // should reject a reservation attempt outright — proves the write
        // path is reached (not short-circuited by an auth failure) without
        // actually persisting anything.
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
                .contentType(ContentType.JSON)
                .body("{\"bookId\":\"20000000-0000-4000-8000-000000000001\"}")
        .when()
            .post("/reservations")
        .then()
            .statusCode(400);
    }

    @Test
    void createReservation_withoutToken_isRejected() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"bookId\":\"20000000-0000-4000-8000-000000000001\"}")
        .when()
            .post("/reservations")
        .then()
            .statusCode(401);
    }

    @Test
    void getAllReservations_asNonAdmin_isForbidden() {
        given()
                .header("Authorization", "Bearer " + ApiSupport.userToken())
        .when()
            .get("/reservations")
        .then()
            .statusCode(403);
    }
}
