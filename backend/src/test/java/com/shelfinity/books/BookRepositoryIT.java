/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.RepositoryTestBase;

/**
 * SPEC.md §10.4 (resolved) — verifies findByGenre and the other named queries
 * against a real database, not a mock.
 */
class BookRepositoryIT extends RepositoryTestBase {

    private BookRepository bookRepository;

    @BeforeEach
    void wireRepository() throws Exception {
        bookRepository = new BookRepository();
        Field field = BookRepository.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(bookRepository, em);
    }

    private Book book(String title, String author, String genre, int availableCopies) {
        Book book = new Book(title, author);
        book.setIsbn(UUID.randomUUID().toString());
        book.setGenre(genre);
        book.setTotalCopies(Math.max(availableCopies, 1));
        book.setAvailableCopies(availableCopies);
        return book;
    }

    @Test
    void saveAndFindById_persistsGenreAndPublicationYear() {
        Book book = book("Clean Code", "Robert C. Martin", "Software Engineering", 3);
        book.setPublicationYear(2008);

        inTransaction(() -> bookRepository.save(book));

        Book found = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(found.getGenre()).isEqualTo("Software Engineering");
        assertThat(found.getPublicationYear()).isEqualTo(2008);
    }

    @Test
    void findByGenre_isCaseInsensitiveExactMatch() {
        Book fiction = book("Dune", "Frank Herbert", "Science Fiction", 2);
        Book nonFiction = book("Sapiens", "Yuval Noah Harari", "History", 1);
        inTransaction(() -> {
            bookRepository.save(fiction);
            bookRepository.save(nonFiction);
        });

        assertThat(bookRepository.findByGenre("science fiction"))
                .extracting(Book::getId)
                .contains(fiction.getId())
                .doesNotContain(nonFiction.getId());
    }

    @Test
    void findAvailable_excludesBooksWithZeroCopies() {
        Book available = book("Available Book", "Author", "Fiction", 1);
        Book unavailable = book("Unavailable Book", "Author", "Fiction", 0);
        inTransaction(() -> {
            bookRepository.save(available);
            bookRepository.save(unavailable);
        });

        assertThat(bookRepository.findAvailable())
                .extracting(Book::getId)
                .contains(available.getId())
                .doesNotContain(unavailable.getId());
    }

    @Test
    void findByIsbn_enforcesUniqueConstraint() {
        Book book = book("Unique ISBN Book", "Author", "Fiction", 1);
        inTransaction(() -> bookRepository.save(book));

        assertThat(bookRepository.findByIsbn(book.getIsbn())).isPresent();
        assertThat(bookRepository.existsByIsbn(book.getIsbn())).isTrue();
    }

    @Test
    void search_matchesTitleOrAuthorCaseInsensitively() {
        Book book = book("The Pragmatic Programmer", "David Thomas", "Software Engineering", 1);
        inTransaction(() -> bookRepository.save(book));

        assertThat(bookRepository.search("pragmatic")).extracting(Book::getId).contains(book.getId());
        assertThat(bookRepository.search("thomas")).extracting(Book::getId).contains(book.getId());
    }

    @Test
    void update_persistsAvailableCopiesChange() {
        Book book = book("Mutable Book", "Author", "Fiction", 2);
        inTransaction(() -> bookRepository.save(book));

        inTransaction(() -> {
            Book managed = bookRepository.findById(book.getId()).orElseThrow();
            managed.setAvailableCopies(1);
            bookRepository.update(managed);
        });

        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);
    }
}
