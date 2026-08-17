/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.overdue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import com.shelfinity.email.EmailService;
import com.shelfinity.overdue.OverdueService.OverdueStats;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueRepository;
import com.shelfinity.queues.QueueStatus;
import com.shelfinity.queues.QueueType;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

@ExtendWith(MockitoExtension.class)
class OverdueServiceTest {

    @Mock private QueueRepository queueRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private OverdueService overdueService;

    private static QueueItem borrowItem(QueueStatus status, LocalDateTime dueDate) {
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-1", UUID.randomUUID(), "desc");
        item.setStatus(status);
        item.setDueDate(dueDate);
        return item;
    }

    @Test
    void isOverdue_pastDueApprovedBorrow_true() {
        QueueItem item = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().minusDays(3));

        assertThat(overdueService.isOverdue(item)).isTrue();
    }

    @Test
    void isOverdue_futureDueDate_false() {
        QueueItem item = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().plusDays(3));

        assertThat(overdueService.isOverdue(item)).isFalse();
    }

    @Test
    void isOverdue_pendingNotApproved_false() {
        QueueItem item = borrowItem(QueueStatus.PENDING, LocalDateTime.now().minusDays(3));

        assertThat(overdueService.isOverdue(item)).isFalse();
    }

    @Test
    void isOverdue_returnTypeNotBorrow_false() {
        QueueItem item = new QueueItem(QueueType.BOOK_RETURN, "kc-1", UUID.randomUUID(), "desc");
        item.setStatus(QueueStatus.APPROVED);
        item.setDueDate(LocalDateTime.now().minusDays(3));

        assertThat(overdueService.isOverdue(item)).isFalse();
    }

    @Test
    void isOverdue_noDueDate_false() {
        QueueItem item = borrowItem(QueueStatus.APPROVED, null);

        assertThat(overdueService.isOverdue(item)).isFalse();
    }

    @Test
    void getDaysOverdue_notOverdue_returnsZero() {
        QueueItem item = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().plusDays(1));

        assertThat(overdueService.getDaysOverdue(item)).isZero();
    }

    @Test
    void getDaysOverdue_overdueByThreeDays_returnsThree() {
        QueueItem item = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().minusDays(3).minusHours(1));

        assertThat(overdueService.getDaysOverdue(item)).isEqualTo(3);
    }

    @Test
    void getOverdueStats_computesAverage() {
        QueueItem twoDaysOverdue = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().minusDays(2).minusHours(1));
        QueueItem fourDaysOverdue = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().minusDays(4).minusHours(1));
        when(queueRepository.findOverdueItems()).thenReturn(List.of(twoDaysOverdue, fourDaysOverdue));

        OverdueStats stats = overdueService.getOverdueStats();

        assertThat(stats.getTotalOverdueItems()).isEqualTo(2);
        assertThat(stats.getTotalDaysOverdue()).isEqualTo(6);
        assertThat(stats.getAverageDaysOverdue()).isEqualTo(3.0);
    }

    @Test
    void getOverdueStats_noOverdueItems_averageIsZero() {
        when(queueRepository.findOverdueItems()).thenReturn(List.of());

        OverdueStats stats = overdueService.getOverdueStats();

        assertThat(stats.getTotalOverdueItems()).isZero();
        assertThat(stats.getAverageDaysOverdue()).isZero();
    }

    @Test
    void checkOverdueBooks_sendsNotificationForEachOverdueItem() {
        UUID bookId = UUID.randomUUID();
        QueueItem item = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().minusDays(2));
        item.setBookId(bookId);
        when(queueRepository.findOverdueItems()).thenReturn(List.of(item));
        when(userRepository.findByKeycloakId("kc-1"))
                .thenReturn(Optional.of(new User("kc-1", "a@b.com", "Alice")));
        Book book = new Book("Clean Code", "Author");
        book.setId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        overdueService.checkOverdueBooks();

        verify(emailService, times(1)).sendOverdueNotification(
                eq("a@b.com"), eq("Alice"), eq("Clean Code"), any(LocalDateTime.class), anyInt());
    }

    @Test
    void checkOverdueBooks_userNotFound_skipsWithoutThrowing() {
        QueueItem item = borrowItem(QueueStatus.APPROVED, LocalDateTime.now().minusDays(2));
        when(queueRepository.findOverdueItems()).thenReturn(List.of(item));
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        overdueService.checkOverdueBooks(); // must not throw

        verify(emailService, times(0)).sendOverdueNotification(any(), any(), any(), any(), anyInt());
    }

    @Test
    void getOverdueItemsForUser_delegatesToRepository() {
        when(queueRepository.findOverdueItemsByUser("kc-1")).thenReturn(List.of());

        overdueService.getOverdueItemsForUser("kc-1");

        verify(queueRepository).findOverdueItemsByUser("kc-1");
    }
}
