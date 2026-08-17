/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.email.EmailService;
import com.shelfinity.reservations.Reservation;
import com.shelfinity.reservations.ReservationRepository;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * Applies the business-rule side effects of approving or rejecting a
 * {@link QueueItem}, per SPEC.md §6.1 (registration), §6.2 (borrow) and §6.3
 * (return). Pulled out of {@link QueueResource} so it can be unit-tested with
 * mocked repositories instead of only being reachable through the JAX-RS layer
 * (SPEC.md §10.2 residual note).
 */
@ApplicationScoped
public class QueueApprovalService {

    @Inject
    private BookRepository bookRepository;

    @Inject
    private ReservationRepository reservationRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private EmailService emailService;

    @Inject
    @ConfigProperty(name = "reservation.expiry.days", defaultValue = "7")
    private int reservationExpiryDays;

    @Inject
    @ConfigProperty(name = "borrow.loan.days", defaultValue = "14")
    private int loanDays;

    /**
     * Applies the effects of a PENDING -&gt; APPROVED transition. Mutates
     * {@code item} in place (e.g. sets a due date) but does not persist it —
     * the caller (QueueResource) still owns that write. Throws
     * {@link QueueApprovalException} if the approval can't be applied; the
     * caller must not persist the status change in that case.
     */
    public void applyApproval(QueueItem item) {
        switch (item.getType()) {
            case BOOK_BORROW:
                applyBorrowApproval(item);
                break;
            case BOOK_RETURN:
                applyReturnApproval(item);
                break;
            case USER_REGISTRATION:
                applyRegistrationApproval(item);
                break;
            default:
                // BOOK_RESERVATION is legacy/unused (SPEC.md §5.3) — no side effect.
        }
    }

    /**
     * Sends the appropriate decline notification for a PENDING -&gt; REJECTED
     * transition. No inventory/account state changes on rejection.
     */
    public void notifyRejection(QueueItem item, String reason) {
        if (item.getType() == QueueType.BOOK_BORROW && item.getBookId() != null) {
            bookRepository.findById(item.getBookId()).ifPresent(book ->
                findUser(item.getUserKeycloakId()).ifPresent(user ->
                    emailService.sendBorrowRequestDecline(user.getEmail(), user.getName(), book.getTitle(), reason)
                )
            );
        } else if (item.getType() == QueueType.USER_REGISTRATION) {
            findUser(item.getUserKeycloakId()).ifPresent(user ->
                emailService.sendRegistrationDeclined(user.getEmail(), user.getName(), reason)
            );
        }
    }

    private void applyBorrowApproval(QueueItem item) {
        Book book = requireBook(item);

        if (book.getAvailableCopies() <= 0) {
            throw new QueueApprovalException(Response.Status.CONFLICT,
                    "No copies of this book are currently available");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.update(book);

        item.setDueDate(LocalDateTime.now().plusDays(loanDays));

        findUser(item.getUserKeycloakId()).ifPresent(user ->
            emailService.sendBorrowRequestApproval(user.getEmail(), user.getName(), book.getTitle())
        );
    }

    private void applyReturnApproval(QueueItem item) {
        Book book = requireBook(item);

        // Cap at totalCopies defensively in case of duplicate approvals bypassing
        // the PENDING guard due to a data inconsistency.
        book.setAvailableCopies(Math.min(book.getAvailableCopies() + 1, book.getTotalCopies()));
        bookRepository.update(book);

        findUser(item.getUserKeycloakId()).ifPresent(user ->
            emailService.sendReturnConfirmation(user.getEmail(), user.getName(), book.getTitle())
        );

        // SPEC.md §6.3 step 2 / §6.4 step 2: promote the oldest active reservation,
        // if any, and restart its claim-window expiry.
        List<Reservation> activeReservations = reservationRepository.findActiveByBookId(book.getId());
        if (!activeReservations.isEmpty()) {
            Reservation next = activeReservations.get(0);
            LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(reservationExpiryDays);
            reservationRepository.markAsNotified(next.getId(), newExpiresAt);
            findUser(next.getUserKeycloakId()).ifPresent(user ->
                emailService.sendBookAvailabilityNotification(user.getEmail(), user.getName(), book.getTitle())
            );
        }
    }

    private void applyRegistrationApproval(QueueItem item) {
        User user = userRepository.findByKeycloakId(item.getUserKeycloakId())
                .orElseThrow(() -> new QueueApprovalException(Response.Status.NOT_FOUND, "User not found"));

        user.setActive(true);
        userRepository.update(user);

        emailService.sendRegistrationConfirmation(user.getEmail(), user.getName());
    }

    private Book requireBook(QueueItem item) {
        if (item.getBookId() == null) {
            throw new QueueApprovalException(Response.Status.BAD_REQUEST,
                    "Queue item has no associated book");
        }
        return bookRepository.findById(item.getBookId())
                .orElseThrow(() -> new QueueApprovalException(Response.Status.NOT_FOUND, "Book not found"));
    }

    private Optional<User> findUser(String userKeycloakId) {
        return userRepository.findByKeycloakId(userKeycloakId);
    }
}
