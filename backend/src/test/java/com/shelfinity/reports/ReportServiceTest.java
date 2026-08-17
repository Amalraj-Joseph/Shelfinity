/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueRepository;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private QueueRepository queueRepository;

    private EntityManager entityManager;

    @InjectMocks
    private ReportService reportService;

    private void injectEntityManager() throws Exception {
        entityManager = mock(EntityManager.class);
        var field = ReportService.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(reportService, entityManager);
    }

    @Test
    void getBookPopularityReport_joinsCountsWithBookDetails() throws Exception {
        injectEntityManager();
        UUID bookId = UUID.randomUUID();
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[]{bookId, 5L}));

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        List<ReportService.BookPopularityReport> result = reportService.getBookPopularityReport(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
        assertThat(result.get(0).getBorrowCount()).isEqualTo(5);
    }

    @Test
    void getBookPopularityReport_skipsEntriesForDeletedBooks() throws Exception {
        injectEntityManager();
        UUID bookId = UUID.randomUUID();
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[]{bookId, 3L}));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        List<ReportService.BookPopularityReport> result = reportService.getBookPopularityReport(10);

        assertThat(result).isEmpty();
    }

    @Test
    void getBorrowingTrends_aggregatesBorrowsReturnsAndActive() throws Exception {
        injectEntityManager();
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(7L, 4L, 12L);

        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        ReportService.BorrowingTrendsReport report = reportService.getBorrowingTrends(start, end);

        assertThat(report.getTotalBorrows()).isEqualTo(7);
        assertThat(report.getTotalReturns()).isEqualTo(4);
        assertThat(report.getCurrentlyBorrowed()).isEqualTo(12);
        assertThat(report.getStartDate()).isEqualTo(start);
        assertThat(report.getEndDate()).isEqualTo(end);
    }

    @Test
    void getUserActivityReport_joinsCountsWithUserDetails() throws Exception {
        injectEntityManager();
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[]{"kc-1", 9L}));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setKeycloakId("kc-1");
        user.setName("Jane Smith");
        user.setEmail("jane.smith@shelfinity.com");
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(user));

        List<ReportService.UserActivityReport> result = reportService.getUserActivityReport(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("Jane Smith");
        assertThat(result.get(0).getActivityCount()).isEqualTo(9);
    }

    @Test
    void getUserActivityReport_skipsEntriesForUnknownUsers() throws Exception {
        injectEntityManager();
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[]{"kc-ghost", 2L}));
        when(userRepository.findByKeycloakId("kc-ghost")).thenReturn(Optional.empty());

        List<ReportService.UserActivityReport> result = reportService.getUserActivityReport(10);

        assertThat(result).isEmpty();
    }

    @Test
    void getLibraryStatistics_aggregatesAcrossRepositoriesAndQueries() throws Exception {
        injectEntityManager();
        when(bookRepository.findAll()).thenReturn(List.of(new Book(), new Book(), new Book()));
        when(bookRepository.findAvailable()).thenReturn(List.of(new Book()));
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(6L);

        when(queueRepository.countPending()).thenReturn(3L);
        when(queueRepository.findOverdueItems()).thenReturn(List.of(new QueueItem()));

        ReportService.LibraryStatistics stats = reportService.getLibraryStatistics();

        assertThat(stats.getTotalBooks()).isEqualTo(3);
        assertThat(stats.getAvailableBooks()).isEqualTo(1);
        assertThat(stats.getTotalUsers()).isEqualTo(2);
        assertThat(stats.getActiveBorrows()).isEqualTo(6);
        assertThat(stats.getPendingRequests()).isEqualTo(3);
        assertThat(stats.getOverdueItems()).isEqualTo(1);
    }

    @Test
    void getAuthorDistribution_countsBooksPerAuthorAndSortsDescending() {
        Book a1 = new Book(); a1.setAuthor("Author A");
        Book a2 = new Book(); a2.setAuthor("Author A");
        Book b1 = new Book(); b1.setAuthor("Author B");
        Book noAuthor = new Book(); noAuthor.setAuthor(null);
        when(bookRepository.findAll()).thenReturn(List.of(a1, a2, b1, noAuthor));

        List<ReportService.AuthorDistribution> result = reportService.getAuthorDistribution();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAuthor()).isEqualTo("Author A");
        assertThat(result.get(0).getCount()).isEqualTo(2);
        assertThat(result).anySatisfy(d -> {
            assertThat(d.getAuthor()).isEqualTo("Unknown");
            assertThat(d.getCount()).isEqualTo(1);
        });
    }
}
