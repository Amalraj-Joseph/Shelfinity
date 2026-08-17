/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueRepository;
import com.shelfinity.queues.QueueStatus;
import com.shelfinity.queues.QueueType;
import com.shelfinity.testsupport.RepositoryTestBase;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

/**
 * ReportService issues raw JPQL directly against the EntityManager rather
 * than going through repositories for most queries — mocking that query
 * chain would be brittle and low-value, so this runs against a real
 * Postgres instead (repository tier).
 */
class ReportServiceIT extends RepositoryTestBase {

    private ReportService reportService;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private QueueRepository queueRepository;

    @BeforeEach
    void wireService() throws Exception {
        bookRepository = new BookRepository();
        setField(bookRepository, "entityManager", em);

        userRepository = new UserRepository();
        setField(userRepository, "entityManager", em);

        queueRepository = new QueueRepository();
        setField(queueRepository, "entityManager", em);

        reportService = new ReportService();
        setField(reportService, "entityManager", em);
        setField(reportService, "bookRepository", bookRepository);
        setField(reportService, "userRepository", userRepository);
        setField(reportService, "queueRepository", queueRepository);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Book book(String title, String author) {
        Book book = new Book(title, author);
        book.setIsbn(UUID.randomUUID().toString());
        book.setTotalCopies(2);
        book.setAvailableCopies(2);
        return book;
    }

    private User user(String name) {
        String suffix = UUID.randomUUID().toString();
        return new User("kc-" + suffix, "user-" + suffix + "@shelfinity.com", name);
    }

    @Test
    void getBookPopularityReport_countsOnlyApprovedBorrows() {
        Book popular = book("Clean Code", "Robert C. Martin");
        User borrower = user("Alice");
        inTransaction(() -> {
            bookRepository.save(popular);
            userRepository.save(borrower);
        });

        QueueItem approvedBorrow = new QueueItem(QueueType.BOOK_BORROW, borrower.getKeycloakId(), popular.getId(), "desc");
        approvedBorrow.setStatus(QueueStatus.APPROVED);
        QueueItem pendingBorrow = new QueueItem(QueueType.BOOK_BORROW, borrower.getKeycloakId(), popular.getId(), "desc");
        pendingBorrow.setStatus(QueueStatus.PENDING);
        inTransaction(() -> {
            queueRepository.save(approvedBorrow);
            queueRepository.save(pendingBorrow);
        });

        var report = reportService.getBookPopularityReport(10);

        assertThat(report).anySatisfy(entry -> {
            assertThat(entry.getBookId()).isEqualTo(popular.getId());
            assertThat(entry.getBorrowCount()).isEqualTo(1); // only the approved one counts
        });
    }

    @Test
    void getBorrowingTrends_countsWithinWindow() {
        Book book = book("Some Book", "Some Author");
        User user = user("Bob");
        inTransaction(() -> {
            bookRepository.save(book);
            userRepository.save(user);
        });

        QueueItem borrow = new QueueItem(QueueType.BOOK_BORROW, user.getKeycloakId(), book.getId(), "desc");
        borrow.setStatus(QueueStatus.APPROVED);
        inTransaction(() -> queueRepository.save(borrow));

        var trends = reportService.getBorrowingTrends(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        assertThat(trends.getTotalBorrows()).isGreaterThanOrEqualTo(1);
        assertThat(trends.getCurrentlyBorrowed()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getUserActivityReport_groupsByUser() {
        Book book = book("Activity Book", "Author");
        User activeUser = user("Carol");
        inTransaction(() -> {
            bookRepository.save(book);
            userRepository.save(activeUser);
        });

        QueueItem approved = new QueueItem(QueueType.BOOK_BORROW, activeUser.getKeycloakId(), book.getId(), "desc");
        approved.setStatus(QueueStatus.APPROVED);
        inTransaction(() -> queueRepository.save(approved));

        var report = reportService.getUserActivityReport(10);

        assertThat(report).anySatisfy(entry -> assertThat(entry.getUserId()).isEqualTo(activeUser.getId()));
    }

    @Test
    void getLibraryStatistics_reflectsSeededData() {
        Book available = book("Available", "Author");
        inTransaction(() -> bookRepository.save(available));

        var stats = reportService.getLibraryStatistics();

        assertThat(stats.getTotalBooks()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getAvailableBooks()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getAuthorDistribution_countsBooksPerAuthor() {
        inTransaction(() -> {
            bookRepository.save(book("Book One", "Shared Author"));
            bookRepository.save(book("Book Two", "Shared Author"));
        });

        var distribution = reportService.getAuthorDistribution();

        assertThat(distribution).anySatisfy(entry -> {
            if (entry.getAuthor().equals("Shared Author")) {
                assertThat(entry.getCount()).isGreaterThanOrEqualTo(2);
            }
        });
    }
}
