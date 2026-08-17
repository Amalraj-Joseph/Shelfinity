/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.dto.requests.CreateBookRequest;
import com.shelfinity.security.JwtUtil;

import jakarta.ws.rs.core.Response;

/**
 * SPEC.md §10.4 (resolved) — genre/publicationYear round-trip, and the
 * pre-existing admin gates on write operations.
 */
@ExtendWith(MockitoExtension.class)
class BooksResourceTest {

    @Mock private BookRepository bookRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private BulkUploadService bulkUploadService;

    @InjectMocks
    private BooksResource booksResource;

    private static CreateBookRequest request(String isbn) {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert C. Martin");
        req.setIsbn(isbn);
        req.setGenre("Software Engineering");
        req.setPublicationYear(2008);
        req.setTotalCopies(3);
        return req;
    }

    @Test
    void createBook_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = booksResource.createBook(request("978-1"));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void createBook_admin_persistsGenreAndPublicationYear() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(bookRepository.findByIsbn("978-1")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = booksResource.createBook(request("978-1"));

        assertThat(response.getStatus()).isEqualTo(201);
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getGenre()).isEqualTo("Software Engineering");
        assertThat(captor.getValue().getPublicationYear()).isEqualTo(2008);
        assertThat(captor.getValue().getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void createBook_duplicateIsbn_returns409() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(bookRepository.findByIsbn("978-1"))
                .thenReturn(Optional.of(new Book("Existing", "Author")));

        Response response = booksResource.createBook(request("978-1"));

        assertThat(response.getStatus()).isEqualTo(409);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void getAllBooks_noFilters_returnsAll() {
        when(bookRepository.findAll()).thenReturn(List.of(new Book("A", "B")));

        Response response = booksResource.getAllBooks(null, null);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(bookRepository).findAll();
        verify(bookRepository, never()).findByGenre(anyString());
    }

    @Test
    void getAllBooks_availableOnly_usesFindAvailable() {
        when(bookRepository.findAvailable()).thenReturn(List.of());

        booksResource.getAllBooks(true, null);

        verify(bookRepository).findAvailable();
    }

    @Test
    void getAllBooks_genreFilter_usesFindByGenre() {
        when(bookRepository.findByGenre("Fiction")).thenReturn(List.of());

        booksResource.getAllBooks(null, "Fiction");

        verify(bookRepository).findByGenre("Fiction");
        verify(bookRepository, never()).findAll();
    }

    @Test
    void getAllBooks_genreAndAvailableOnly_composesBothFilters() {
        Book available = new Book("Available", "Author");
        available.setGenre("Fiction");
        available.setAvailableCopies(1);
        Book unavailable = new Book("Unavailable", "Author");
        unavailable.setGenre("Fiction");
        unavailable.setAvailableCopies(0);
        when(bookRepository.findByGenre("Fiction")).thenReturn(List.of(available, unavailable));

        Response response = booksResource.getAllBooks(true, "Fiction");

        @SuppressWarnings("unchecked")
        List<Object> body = (List<Object>) response.getEntity();
        assertThat(body).hasSize(1);
    }

    @Test
    void getBookById_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Response response = booksResource.getBookById(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void getBookById_invalidUuid_returns400() {
        Response response = booksResource.getBookById("not-a-uuid");

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void updateBook_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = booksResource.updateBook(UUID.randomUUID().toString(), request("978-1"));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(bookRepository, never()).update(any());
    }

    @Test
    void updateBook_admin_updatesGenreAndPublicationYear() {
        UUID id = UUID.randomUUID();
        Book existing = new Book("Old Title", "Old Author");
        existing.setId(id);
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(bookRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookRepository.update(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = booksResource.updateBook(id.toString(), request("978-1"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(existing.getGenre()).isEqualTo("Software Engineering");
        assertThat(existing.getPublicationYear()).isEqualTo(2008);
    }

    @Test
    void deleteBook_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = booksResource.deleteBook(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(403);
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void deleteBook_admin_deletesAndReturns204() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(bookRepository.findById(id)).thenReturn(Optional.of(new Book("A", "B")));

        Response response = booksResource.deleteBook(id.toString());

        assertThat(response.getStatus()).isEqualTo(204);
        verify(bookRepository).deleteById(id);
    }

    @Test
    void searchBooks_blankQuery_returns400() {
        Response response = booksResource.searchBooks("   ");

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void searchBooks_delegatesToRepository() {
        when(bookRepository.search("clean")).thenReturn(List.of(new Book("Clean Code", "Martin")));

        Response response = booksResource.searchBooks("clean");

        assertThat(response.getStatus()).isEqualTo(200);
        verify(bookRepository).search("clean");
    }

    @Test
    void bulkUploadBooks_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = booksResource.bulkUploadBooks(null);

        assertThat(response.getStatus()).isEqualTo(403);
    }
}
