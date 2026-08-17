/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.email.EmailService;
import com.shelfinity.reservations.Reservation;
import com.shelfinity.reservations.ReservationRepository;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.ws.rs.core.Response;

/**
 * Covers SPEC.md §6.1 (registration approval), §6.2 (borrow approval), and
 * §6.3 (return approval + reservation promotion) — the logic behind §10.2 and
 * §10.3's resolution.
 */
@ExtendWith(MockitoExtension.class)
class QueueApprovalServiceTest {

    private static final int LOAN_DAYS = 14;
    private static final int RESERVATION_EXPIRY_DAYS = 7;

    @Mock private BookRepository bookRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    private QueueApprovalService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new QueueApprovalService();
        setField(service, "bookRepository", bookRepository);
        setField(service, "reservationRepository", reservationRepository);
        setField(service, "userRepository", userRepository);
        setField(service, "emailService", emailService);
        setField(service, "reservationExpiryDays", RESERVATION_EXPIRY_DAYS);
        setField(service, "loanDays", LOAN_DAYS);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static QueueItem borrowItem(UUID bookId) {
        QueueItem item = new QueueItem(QueueType.BOOK_BORROW, "kc-user-1", bookId, "Borrow request");
        return item;
    }

    private static Book bookWith(int totalCopies, int availableCopies) {
        Book book = new Book("Clean Code", "Robert C. Martin");
        book.setId(UUID.randomUUID());
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);
        return book;
    }

    private static User user(String keycloakId, String email, String name) {
        User user = new User(keycloakId, email, name);
        return user;
    }

    // ---- BOOK_BORROW -------------------------------------------------

    @Test
    void borrowApproval_decrementsAvailabilityAndSetsDueDate() {
        Book book = bookWith(3, 2);
        QueueItem item = borrowItem(book.getId());
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findByKeycloakId("kc-user-1"))
                .thenReturn(Optional.of(user("kc-user-1", "a@b.com", "Alice")));

        service.applyApproval(item);

        assertThat(book.getAvailableCopies()).isEqualTo(1);
        assertThat(item.getDueDate()).isAfter(LocalDateTime.now().plusDays(LOAN_DAYS - 1));
        verify(bookRepository).update(book);
        verify(emailService).sendBorrowRequestApproval("a@b.com", "Alice", "Clean Code");
    }

    @Test
    void borrowApproval_noCopiesAvailable_throwsConflictAndDoesNotMutateBook() {
        Book book = bookWith(1, 0);
        QueueItem item = borrowItem(book.getId());
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> service.applyApproval(item))
                .isInstanceOf(QueueApprovalException.class)
                .satisfies(e -> assertThat(((QueueApprovalException) e).getStatus())
                        .isEqualTo(Response.Status.CONFLICT));

        assertThat(book.getAvailableCopies()).isZero();
        verify(bookRepository, never()).update(any());
    }

    @Test
    void borrowApproval_bookNotFound_throwsNotFound() {
        UUID missingBookId = UUID.randomUUID();
        QueueItem item = borrowItem(missingBookId);
        when(bookRepository.findById(missingBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyApproval(item))
                .isInstanceOf(QueueApprovalException.class)
                .satisfies(e -> assertThat(((QueueApprovalException) e).getStatus())
                        .isEqualTo(Response.Status.NOT_FOUND));
    }

    @Test
    void borrowApproval_missingBookId_throwsBadRequest() {
        QueueItem item = borrowItem(null);

        assertThatThrownBy(() -> service.applyApproval(item))
                .isInstanceOf(QueueApprovalException.class)
                .satisfies(e -> assertThat(((QueueApprovalException) e).getStatus())
                        .isEqualTo(Response.Status.BAD_REQUEST));
    }

    // ---- BOOK_RETURN ---------------------------------------------------

    @Test
    void returnApproval_incrementsAvailabilityCappedAtTotalCopies() {
        Book book = bookWith(2, 2); // already at max
        QueueItem item = new QueueItem(QueueType.BOOK_RETURN, "kc-user-1", book.getId(), "Return request");
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findByKeycloakId("kc-user-1"))
                .thenReturn(Optional.of(user("kc-user-1", "a@b.com", "Alice")));
        when(reservationRepository.findActiveByBookId(book.getId())).thenReturn(List.of());

        service.applyApproval(item);

        assertThat(book.getAvailableCopies()).isEqualTo(2); // capped, not 3
        verify(emailService).sendReturnConfirmation("a@b.com", "Alice", "Clean Code");
    }

    @Test
    void returnApproval_promotesOldestActiveReservationAndResetsExpiry() {
        Book book = bookWith(1, 0);
        QueueItem item = new QueueItem(QueueType.BOOK_RETURN, "kc-borrower", book.getId(), "Return request");
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findByKeycloakId("kc-borrower"))
                .thenReturn(Optional.of(user("kc-borrower", "b@b.com", "Bob")));

        Reservation oldest = new Reservation();
        oldest.setId(UUID.randomUUID());
        oldest.setUserKeycloakId("kc-waiting-user");
        oldest.setBookId(book.getId());
        when(reservationRepository.findActiveByBookId(book.getId())).thenReturn(List.of(oldest));
        when(userRepository.findByKeycloakId("kc-waiting-user"))
                .thenReturn(Optional.of(user("kc-waiting-user", "c@b.com", "Carol")));

        service.applyApproval(item);

        verify(reservationRepository).markAsNotified(eq(oldest.getId()), any(LocalDateTime.class));
        verify(emailService).sendBookAvailabilityNotification("c@b.com", "Carol", "Clean Code");
    }

    @Test
    void returnApproval_noActiveReservations_doesNotPromoteAnything() {
        Book book = bookWith(1, 0);
        QueueItem item = new QueueItem(QueueType.BOOK_RETURN, "kc-borrower", book.getId(), "Return request");
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findByKeycloakId("kc-borrower"))
                .thenReturn(Optional.of(user("kc-borrower", "b@b.com", "Bob")));
        when(reservationRepository.findActiveByBookId(book.getId())).thenReturn(List.of());

        service.applyApproval(item);

        verify(reservationRepository, never()).markAsNotified(any(), any());
        verify(emailService, never()).sendBookAvailabilityNotification(anyString(), anyString(), anyString());
    }

    // ---- USER_REGISTRATION ---------------------------------------------

    @Test
    void registrationApproval_activatesUserAndSendsConfirmation() {
        QueueItem item = new QueueItem(QueueType.USER_REGISTRATION, "kc-new-user", "Registration approval");
        User pendingUser = user("kc-new-user", "new@user.com", "New User");
        pendingUser.setActive(false);
        when(userRepository.findByKeycloakId("kc-new-user")).thenReturn(Optional.of(pendingUser));

        service.applyApproval(item);

        assertThat(pendingUser.isActive()).isTrue();
        verify(userRepository).update(pendingUser);
        verify(emailService).sendRegistrationConfirmation("new@user.com", "New User");
    }

    @Test
    void registrationApproval_userNotFound_throwsNotFound() {
        QueueItem item = new QueueItem(QueueType.USER_REGISTRATION, "kc-ghost", "Registration approval");
        when(userRepository.findByKeycloakId("kc-ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyApproval(item))
                .isInstanceOf(QueueApprovalException.class)
                .satisfies(e -> assertThat(((QueueApprovalException) e).getStatus())
                        .isEqualTo(Response.Status.NOT_FOUND));
    }

    // ---- Legacy/unused type ---------------------------------------------

    @Test
    void reservationType_isNoOp() {
        QueueItem item = new QueueItem(QueueType.BOOK_RESERVATION, "kc-user-1", UUID.randomUUID(), "n/a");

        service.applyApproval(item);

        verify(bookRepository, never()).findById(any());
        verify(userRepository, never()).findByKeycloakId(any());
    }

    // ---- Rejection notifications -----------------------------------------

    @Test
    void notifyRejection_borrowSendsDeclineEmailWithReason() {
        Book book = bookWith(1, 1);
        QueueItem item = borrowItem(book.getId());
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findByKeycloakId("kc-user-1"))
                .thenReturn(Optional.of(user("kc-user-1", "a@b.com", "Alice")));

        service.notifyRejection(item, "Book damaged");

        verify(emailService).sendBorrowRequestDecline("a@b.com", "Alice", "Clean Code", "Book damaged");
    }

    @Test
    void notifyRejection_registrationSendsDeclineEmail() {
        QueueItem item = new QueueItem(QueueType.USER_REGISTRATION, "kc-new-user", "Registration approval");
        when(userRepository.findByKeycloakId("kc-new-user"))
                .thenReturn(Optional.of(user("kc-new-user", "new@user.com", "New User")));

        service.notifyRejection(item, "Could not verify identity");

        verify(emailService).sendRegistrationDeclined("new@user.com", "New User", "Could not verify identity");
        verify(userRepository, times(0)).update(any());
    }
}
