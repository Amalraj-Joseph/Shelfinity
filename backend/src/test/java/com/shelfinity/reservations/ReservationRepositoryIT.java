/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.RepositoryTestBase;

class ReservationRepositoryIT extends RepositoryTestBase {

    private ReservationRepository reservationRepository;

    @BeforeEach
    void wireRepository() throws Exception {
        reservationRepository = new ReservationRepository();
        Field field = ReservationRepository.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(reservationRepository, em);
    }

    private Reservation reservation(String userKeycloakId, UUID bookId, ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setUserKeycloakId(userKeycloakId);
        reservation.setBookId(bookId);
        reservation.setStatus(status);
        reservation.setExpiresAt(LocalDateTime.now().plusDays(7));
        return reservation;
    }

    @Test
    void saveAndFindById_roundTrips() {
        Reservation reservation = reservation("kc-1", UUID.randomUUID(), ReservationStatus.ACTIVE);

        inTransaction(() -> reservationRepository.save(reservation));

        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
    }

    @Test
    void findByUserKeycloakId_scopesToUser() {
        Reservation reservation = reservation("kc-scope-test", UUID.randomUUID(), ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(reservation));

        assertThat(reservationRepository.findByUserKeycloakId("kc-scope-test")).isNotEmpty();
        assertThat(reservationRepository.findByUserKeycloakId("kc-nobody")).isEmpty();
    }

    @Test
    void findActiveByBookId_ordersOldestFirst() throws InterruptedException {
        UUID bookId = UUID.randomUUID();
        Reservation first = reservation("kc-first", bookId, ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(first));
        Thread.sleep(10); // ensure createdAt ordering is deterministic
        Reservation second = reservation("kc-second", bookId, ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(second));

        var active = reservationRepository.findActiveByBookId(bookId);

        assertThat(active).hasSize(2);
        assertThat(active.get(0).getId()).isEqualTo(first.getId());
    }

    @Test
    void hasActiveReservation_trueOnlyForActiveStatus() {
        UUID bookId = UUID.randomUUID();
        Reservation cancelled = reservation("kc-cancelled-check", bookId, ReservationStatus.CANCELLED);
        inTransaction(() -> reservationRepository.save(cancelled));

        assertThat(reservationRepository.hasActiveReservation("kc-cancelled-check", bookId)).isFalse();

        Reservation active = reservation("kc-active-check", bookId, ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(active));

        assertThat(reservationRepository.hasActiveReservation("kc-active-check", bookId)).isTrue();
    }

    @Test
    void markAsNotified_setsStatusNotifiedAtAndExpiresAt() {
        Reservation reservation = reservation("kc-1", UUID.randomUUID(), ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(reservation));
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(7);

        inTransaction(() -> reservationRepository.markAsNotified(reservation.getId(), newExpiry));

        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.NOTIFIED);
        assertThat(updated.getNotifiedAt()).isNotNull();
        assertThat(updated.getExpiresAt()).isEqualToIgnoringNanos(newExpiry);
    }

    @Test
    void cancel_setsStatusCancelled() {
        Reservation reservation = reservation("kc-1", UUID.randomUUID(), ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(reservation));

        inTransaction(() -> reservationRepository.cancel(reservation.getId()));

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void markAsFulfilled_setsStatusFulfilled() {
        Reservation reservation = reservation("kc-1", UUID.randomUUID(), ReservationStatus.NOTIFIED);
        inTransaction(() -> reservationRepository.save(reservation));

        inTransaction(() -> reservationRepository.markAsFulfilled(reservation.getId()));

        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.FULFILLED);
    }

    @Test
    void markExpiredReservations_expiresOnlyPastActiveReservations() {
        Reservation expired = reservation("kc-expired", UUID.randomUUID(), ReservationStatus.ACTIVE);
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));
        Reservation stillActive = reservation("kc-still-active", UUID.randomUUID(), ReservationStatus.ACTIVE);
        stillActive.setExpiresAt(LocalDateTime.now().plusDays(1));
        inTransaction(() -> {
            reservationRepository.save(expired);
            reservationRepository.save(stillActive);
        });

        inTransaction(() -> reservationRepository.markExpiredReservations());
        // A bulk UPDATE (executeUpdate()) bypasses the persistence context, so
        // the first-level cache still holds the pre-update entities loaded by
        // save() above unless explicitly cleared.
        em.clear();

        assertThat(reservationRepository.findById(expired.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.EXPIRED);
        assertThat(reservationRepository.findById(stillActive.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void deleteById_removesRecord() {
        Reservation reservation = reservation("kc-1", UUID.randomUUID(), ReservationStatus.ACTIVE);
        inTransaction(() -> reservationRepository.save(reservation));
        UUID id = reservation.getId();

        inTransaction(() -> reservationRepository.deleteById(id));

        assertThat(reservationRepository.findById(id)).isEmpty();
    }
}
