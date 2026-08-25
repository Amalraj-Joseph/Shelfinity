/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.ApiSupport;

import io.restassured.http.ContentType;

/**
 * Black-box coverage for {@code BooksResource} / {@code BookRepository}
 * against a real running stack — see {@link ApiSupport}'s Javadoc for why
 * this tier exists (it's the only one that exercises the real CDI/JTA
 * wiring the persistence redesign changed).
 */
class BooksApiIT {

    @BeforeAll
    static void configureApi() {
        ApiSupport.init();
    }

    @Test
    void listBooks_isPublicAndReturnsSeedData() {
        given()
        .when()
            .get("/books")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    void getAvailableBooks_isPublic() {
        given()
        .when()
            .get("/books/available")
        .then()
            .statusCode(200);
    }

    @Test
    void searchBooks_findsByTitle() {
        given()
            .queryParam("q", "Gatsby")
        .when()
            .get("/books/search")
        .then()
            .statusCode(200)
            .body("title", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Gatsby")));
    }

    @Test
    void createUpdateDeleteBook_asAdmin_roundTripsThroughRealDatabase() {
        String token = ApiSupport.adminToken();

        String bookJson = """
            {"title":"API-IT Smoke Test Book","author":"Test Author","totalCopies":2}
            """;

        String id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(bookJson)
            .when()
                .post("/books")
            .then()
                .statusCode(201)
                .body("title", equalTo("API-IT Smoke Test Book"))
                .body("availableCopies", equalTo(2))
                .extract().path("id");

        // Read-your-own-write against the real DB, not the response body cache.
        given()
        .when()
            .get("/books/{id}", id)
        .then()
            .statusCode(200)
            .body("title", equalTo("API-IT Smoke Test Book"));

        String updateJson = """
            {"title":"API-IT Smoke Test Book (updated)","author":"Test Author","totalCopies":2}
            """;
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateJson)
        .when()
            .put("/books/{id}", id)
        .then()
            .statusCode(200)
            .body("title", equalTo("API-IT Smoke Test Book (updated)"));

        given()
                .header("Authorization", "Bearer " + token)
        .when()
            .delete("/books/{id}", id)
        .then()
            .statusCode(204);

        given()
        .when()
            .get("/books/{id}", id)
        .then()
            .statusCode(404);
    }

    @Test
    void createBook_withoutToken_isRejected() {
        // BooksResource.createBook only checks isCurrentUserAdmin() — no
        // isAuthenticated() guard first — so a missing token also resolves
        // to FORBIDDEN, not UNAUTHORIZED (unlike QueueResource/
        // ReservationResource, which check identity presence explicitly).
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Should Not Persist\",\"author\":\"Nobody\",\"totalCopies\":1}")
        .when()
            .post("/books")
        .then()
            .statusCode(403);
    }

    @Test
    void createBook_asNonAdmin_isForbidden() {
        // Proves JwtUtil.isCurrentUserAdmin()'s DB-driven role lookup (via the
        // same EntityManagerProducer chain) correctly denies a real,
        // non-admin, DB-backed user — not just an absent token.
        String token = ApiSupport.userToken();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Should Not Persist\",\"author\":\"Nobody\",\"totalCopies\":1}")
        .when()
            .post("/books")
        .then()
            .statusCode(403);
    }

    @Test
    void getBookById_notFound_returns404NotError() {
        given()
        .when()
            .get("/books/{id}", "00000000-0000-4000-8000-000000000000")
        .then()
            .statusCode(404);
    }
}
