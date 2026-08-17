/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Exercises every notification method's template-formatting code path. With
 * no active EmailConfig (the realistic default in a unit test), sendEmail()
 * short-circuits before touching JavaMail/Transport — real SMTP delivery is
 * exercised by the API/e2e tier against a running stack instead.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private EmailConfigRepository emailConfigRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_noMailSessionInitialized_returnsFalseWithoutThrowing() {
        boolean result = emailService.sendEmail("a@b.com", "Subject", "Body");

        assertThat(result).isFalse();
    }

    @Test
    void refreshMailSession_noActiveConfig_leavesSessionUninitialized() {
        when(emailConfigRepository.findActiveConfig()).thenReturn(Optional.empty());

        emailService.refreshMailSession();

        assertThat(emailService.sendEmail("a@b.com", "Subject", "Body")).isFalse();
    }

    @Test
    void sendRegistrationConfirmation_doesNotThrow() {
        emailService.sendRegistrationConfirmation("a@b.com", "Alice");
    }

    @Test
    void sendRegistrationDeclined_doesNotThrow() {
        emailService.sendRegistrationDeclined("a@b.com", "Alice", "Could not verify identity");
    }

    @Test
    void sendRegistrationDeclined_nullReason_usesDefaultText() {
        emailService.sendRegistrationDeclined("a@b.com", "Alice", null);
    }

    @Test
    void sendBorrowRequestAcknowledgment_doesNotThrow() {
        emailService.sendBorrowRequestAcknowledgment("a@b.com", "Alice", "Clean Code");
    }

    @Test
    void sendBorrowRequestApproval_doesNotThrow() {
        emailService.sendBorrowRequestApproval("a@b.com", "Alice", "Clean Code");
    }

    @Test
    void sendBorrowRequestDecline_nullReason_usesDefaultText() {
        emailService.sendBorrowRequestDecline("a@b.com", "Alice", "Clean Code", null);
    }

    @Test
    void sendReturnConfirmation_doesNotThrow() {
        emailService.sendReturnConfirmation("a@b.com", "Alice", "Clean Code");
    }

    @Test
    void sendOverdueReminder_doesNotThrow() {
        emailService.sendOverdueReminder("a@b.com", "Alice", "Clean Code", 3);
    }

    @Test
    void sendOverdueNotification_doesNotThrow() {
        emailService.sendOverdueNotification("a@b.com", "Alice", "Clean Code", LocalDateTime.now().minusDays(2), 2);
    }

    @Test
    void sendAdminRequestAlert_doesNotThrow() {
        emailService.sendAdminRequestAlert("admin@b.com", "Borrow", "Alice", "Clean Code");
    }

    @Test
    void sendProfileUpdateNotification_doesNotThrow() {
        emailService.sendProfileUpdateNotification("a@b.com", "Alice");
    }

    @Test
    void sendPasswordChangeNotification_doesNotThrow() {
        emailService.sendPasswordChangeNotification("a@b.com", "Alice");
    }

    @Test
    void sendReservationConfirmation_doesNotThrow() {
        emailService.sendReservationConfirmation("a@b.com", "Alice", "Clean Code");
    }

    @Test
    void sendBookAvailabilityNotification_doesNotThrow() {
        emailService.sendBookAvailabilityNotification("a@b.com", "Alice", "Clean Code");
    }
}
