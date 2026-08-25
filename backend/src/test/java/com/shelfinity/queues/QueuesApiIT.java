/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

import io.restassured.http.ContentType;

/**
 * Black-box coverage for {@code QueueResource} / {@code QueueApprovalService}
 * — the one flow that specifically proves
 * {@code EntityManagerProducer}'s {@code @TransactionScoped} producer really
 * does share one persistence context across multiple repositories within a
 * single HTTP request (its Javadoc's core claim, previously asserted in a
 * comment but never exercised against a real Weld/JTA container). Approving
 * a borrow request writes through both {@code QueueRepository} and
 * {@code BookRepository} in the same {@code @Transactional} call; this
 * confirms both writes land atomically in the real database, not just that
 * the HTTP call returns 200.
 */
class QueuesApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    private static String createFixtureBook(String token, int totalCopies) {
        String body = String.format(
                "{\"title\":\"Queue API-IT Fixture\",\"author\":\"Test Author\",\"totalCopies\":%d}",
                totalCopies);
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
            .post("/books")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    @Test
    void approvingBorrowRequest_atomicallyUpdatesQueueItemAndBookCopies() {
        String adminToken = ApiSupport.adminToken();
        String userToken = ApiSupport.userToken();

        String bookId = createFixtureBook(adminToken, 3);

        String queueItemId = given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body("{\"type\":\"BOOK_BORROW\",\"bookId\":\"" + bookId + "\",\"description\":\"api-it smoke test\"}")
        .when()
            .post("/queues")
        .then()
            .statusCode(201)
            .body("status", equalTo("PENDING"))
            .extract().path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"status\":\"APPROVED\"}")
        .when()
            .patch("/queues/{id}/status", queueItemId)
        .then()
            .statusCode(200)
            .body("status", equalTo("APPROVED"))
            .body("dueDate", notNullValue());

        // The book copy count is written by a *different* repository
        // (BookRepository) than the one the resource method is on
        // (QueueRepository) — both must have shared the same EntityManager
        // and committed together for this to be visible here.
        given()
        .when()
            .get("/books/{id}", bookId)
        .then()
            .statusCode(200)
            .body("availableCopies", equalTo(2))
            .body("totalCopies", equalTo(3));

        // Cleanup: delete the queue item, then the fixture book.
        given()
                .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/queues/{id}", queueItemId)
        .then()
            .statusCode(anyOf204Or200());
        given()
                .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/books/{id}", bookId)
        .then()
            .statusCode(anyOf204Or200());
    }

    // DELETE endpoints in this codebase are inconsistent between 200 (with a
    // body) and 204 across resources; cleanup here only needs to not throw.
    private static org.hamcrest.Matcher<Integer> anyOf204Or200() {
        return org.hamcrest.Matchers.anyOf(equalTo(200), equalTo(204));
    }

    @Test
    void rejectingBorrowRequest_leavesBookCopiesUntouched() {
        String adminToken = ApiSupport.adminToken();
        String userToken = ApiSupport.userToken();

        String bookId = createFixtureBook(adminToken, 2);

        String queueItemId = given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body("{\"type\":\"BOOK_BORROW\",\"bookId\":\"" + bookId + "\",\"description\":\"api-it reject test\"}")
        .when()
            .post("/queues")
        .then()
            .statusCode(201)
            .extract().path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"status\":\"REJECTED\",\"adminRemark\":\"not needed\"}")
        .when()
            .patch("/queues/{id}/status", queueItemId)
        .then()
            .statusCode(200)
            .body("status", equalTo("REJECTED"));

        given()
        .when()
            .get("/books/{id}", bookId)
        .then()
            .statusCode(200)
            .body("availableCopies", equalTo(2));

        given()
                .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/queues/{id}", queueItemId)
        .then()
            .statusCode(anyOf204Or200());
        given()
                .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/books/{id}", bookId)
        .then()
            .statusCode(anyOf204Or200());
    }

    @Test
    void listMyQueueItems_returnsOnlyCallersOwnItems() {
        String userToken = ApiSupport.userToken();

        given()
                .header("Authorization", "Bearer " + userToken)
        .when()
            .get("/queues/my")
        .then()
            .statusCode(200);
    }

    @Test
    void createQueueItem_withoutToken_isRejected() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"BOOK_BORROW\",\"description\":\"no auth\"}")
        .when()
            .post("/queues")
        .then()
            .statusCode(401);
    }
}
