/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.RepositoryTestBase;

class QueueRepositoryIT extends RepositoryTestBase {

    private QueueRepository queueRepository;

    @BeforeEach
    void wireRepository() throws Exception {
        queueRepository = new QueueRepository();
        Field field = QueueRepository.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(queueRepository, em);
    }

    private QueueItem item(QueueType type, QueueStatus status, String userKeycloakId, UUID bookId) {
        QueueItem item = new QueueItem(type, userKeycloakId, bookId, "desc");
        item.setStatus(status);
        return item;
    }

    @Test
    void saveAndFindById_roundTrips() {
        QueueItem item = item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-1", UUID.randomUUID());

        inTransaction(() -> queueRepository.save(item));

        assertThat(queueRepository.findById(item.getId())).isPresent();
    }

    @Test
    void findByStatus_filtersCorrectly() {
        QueueItem pending = item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-1", UUID.randomUUID());
        QueueItem approved = item(QueueType.BOOK_BORROW, QueueStatus.APPROVED, "kc-1", UUID.randomUUID());
        inTransaction(() -> {
            queueRepository.save(pending);
            queueRepository.save(approved);
        });

        assertThat(queueRepository.findByStatus(QueueStatus.PENDING))
                .extracting(QueueItem::getId)
                .contains(pending.getId())
                .doesNotContain(approved.getId());
    }

    @Test
    void findByType_filtersCorrectly() {
        QueueItem borrow = item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-1", UUID.randomUUID());
        QueueItem registration = item(QueueType.USER_REGISTRATION, QueueStatus.PENDING, "kc-2", null);
        inTransaction(() -> {
            queueRepository.save(borrow);
            queueRepository.save(registration);
        });

        assertThat(queueRepository.findByType(QueueType.USER_REGISTRATION))
                .extracting(QueueItem::getId)
                .contains(registration.getId())
                .doesNotContain(borrow.getId());
    }

    @Test
    void existsPendingForUserAndBook_trueOnlyForMatchingPendingItem() {
        UUID bookId = UUID.randomUUID();
        QueueItem pending = item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-dup-test", bookId);
        inTransaction(() -> queueRepository.save(pending));

        assertThat(queueRepository.existsPendingForUserAndBook("kc-dup-test", bookId, QueueType.BOOK_BORROW)).isTrue();
        assertThat(queueRepository.existsPendingForUserAndBook("kc-dup-test", bookId, QueueType.BOOK_RETURN)).isFalse();
        assertThat(queueRepository.existsPendingForUserAndBook("kc-other-user", bookId, QueueType.BOOK_BORROW)).isFalse();
    }

    @Test
    void existsPendingForUserAndBook_falseOnceApproved() {
        UUID bookId = UUID.randomUUID();
        QueueItem approved = item(QueueType.BOOK_BORROW, QueueStatus.APPROVED, "kc-approved-test", bookId);
        inTransaction(() -> queueRepository.save(approved));

        assertThat(queueRepository.existsPendingForUserAndBook("kc-approved-test", bookId, QueueType.BOOK_BORROW)).isFalse();
    }

    @Test
    void countPending_countsOnlyPendingStatus() {
        inTransaction(() -> {
            queueRepository.save(item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-count-1", UUID.randomUUID()));
            queueRepository.save(item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-count-2", UUID.randomUUID()));
            queueRepository.save(item(QueueType.BOOK_BORROW, QueueStatus.APPROVED, "kc-count-3", UUID.randomUUID()));
        });

        assertThat(queueRepository.countPending()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void findOverdueItems_onlyApprovedBorrowsPastDueDate() {
        QueueItem overdue = item(QueueType.BOOK_BORROW, QueueStatus.APPROVED, "kc-overdue", UUID.randomUUID());
        overdue.setDueDate(LocalDateTime.now().minusDays(1));
        QueueItem notYetDue = item(QueueType.BOOK_BORROW, QueueStatus.APPROVED, "kc-notdue", UUID.randomUUID());
        notYetDue.setDueDate(LocalDateTime.now().plusDays(5));
        inTransaction(() -> {
            queueRepository.save(overdue);
            queueRepository.save(notYetDue);
        });

        assertThat(queueRepository.findOverdueItems())
                .extracting(QueueItem::getId)
                .contains(overdue.getId())
                .doesNotContain(notYetDue.getId());
    }

    @Test
    void findOverdueItemsByUser_scopesToUser() {
        UUID bookId = UUID.randomUUID();
        QueueItem overdue = item(QueueType.BOOK_BORROW, QueueStatus.APPROVED, "kc-scope-user", bookId);
        overdue.setDueDate(LocalDateTime.now().minusDays(1));
        inTransaction(() -> queueRepository.save(overdue));

        assertThat(queueRepository.findOverdueItemsByUser("kc-scope-user")).isNotEmpty();
        assertThat(queueRepository.findOverdueItemsByUser("kc-no-such-user")).isEmpty();
    }

    @Test
    void update_persistsStatusChange() {
        QueueItem item = item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-1", UUID.randomUUID());
        inTransaction(() -> queueRepository.save(item));

        inTransaction(() -> {
            QueueItem managed = queueRepository.findById(item.getId()).orElseThrow();
            managed.setStatus(QueueStatus.APPROVED);
            queueRepository.update(managed);
        });

        assertThat(queueRepository.findById(item.getId()).orElseThrow().getStatus()).isEqualTo(QueueStatus.APPROVED);
    }

    @Test
    void deleteById_removesRecord() {
        QueueItem item = item(QueueType.BOOK_BORROW, QueueStatus.PENDING, "kc-1", UUID.randomUUID());
        inTransaction(() -> queueRepository.save(item));
        UUID id = item.getId();

        inTransaction(() -> queueRepository.deleteById(id));

        assertThat(queueRepository.findById(id)).isEmpty();
    }
}
