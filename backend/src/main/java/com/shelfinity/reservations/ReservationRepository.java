/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

/**
 * Repository for Reservation entity operations.
 */
@ApplicationScoped
public class ReservationRepository {
    
    @PersistenceContext(unitName = "shelfinityPU")
    private EntityManager entityManager;
    
    /**
     * Save a new reservation.
     */
    @Transactional
    public Reservation save(Reservation reservation) {
        entityManager.persist(reservation);
        return reservation;
    }
    
    /**
     * Update an existing reservation.
     */
    @Transactional
    public Reservation update(Reservation reservation) {
        return entityManager.merge(reservation);
    }
    
    /**
     * Find reservation by ID.
     */
    public Optional<Reservation> findById(UUID id) {
        Reservation reservation = entityManager.find(Reservation.class, id);
        return Optional.ofNullable(reservation);
    }
    
    /**
     * Find all reservations.
     */
    public List<Reservation> findAll() {
        TypedQuery<Reservation> query = entityManager.createNamedQuery(
            "Reservation.findAll", Reservation.class);
        return query.getResultList();
    }
    
    /**
     * Find reservations by user Keycloak ID.
     */
    public List<Reservation> findByUserKeycloakId(String userKeycloakId) {
        TypedQuery<Reservation> query = entityManager.createNamedQuery(
            "Reservation.findByUserKeycloakId", Reservation.class);
        query.setParameter("userKeycloakId", userKeycloakId);
        return query.getResultList();
    }
    
    /**
     * Find reservations by book ID.
     */
    public List<Reservation> findByBookId(UUID bookId) {
        TypedQuery<Reservation> query = entityManager.createNamedQuery(
            "Reservation.findByBookId", Reservation.class);
        query.setParameter("bookId", bookId);
        return query.getResultList();
    }
    
    /**
     * Find active reservations by book ID.
     */
    public List<Reservation> findActiveByBookId(UUID bookId) {
        TypedQuery<Reservation> query = entityManager.createNamedQuery(
            "Reservation.findActiveByBookId", Reservation.class);
        query.setParameter("bookId", bookId);
        return query.getResultList();
    }
    
    /**
     * Find reservations by status.
     */
    public List<Reservation> findByStatus(ReservationStatus status) {
        TypedQuery<Reservation> query = entityManager.createNamedQuery(
            "Reservation.findByStatus", Reservation.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    /**
     * Find expired reservations.
     */
    public List<Reservation> findExpired() {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.status = :status AND r.expiresAt < :now",
            Reservation.class);
        query.setParameter("status", ReservationStatus.ACTIVE);
        query.setParameter("now", LocalDateTime.now());
        return query.getResultList();
    }
    
    /**
     * Check if user has an active reservation for a book.
     */
    public boolean hasActiveReservation(String userKeycloakId, UUID bookId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Reservation r WHERE r.userKeycloakId = :userKeycloakId " +
            "AND r.bookId = :bookId AND r.status = :status",
            Long.class);
        query.setParameter("userKeycloakId", userKeycloakId);
        query.setParameter("bookId", bookId);
        query.setParameter("status", ReservationStatus.ACTIVE);
        return query.getSingleResult() > 0;
    }
    
    /**
     * Delete reservation by ID.
     */
    @Transactional
    public void deleteById(UUID id) {
        Reservation reservation = entityManager.find(Reservation.class, id);
        if (reservation != null) {
            entityManager.remove(reservation);
        }
    }
    
    /**
     * Cancel reservation.
     */
    @Transactional
    public void cancel(UUID id) {
        Reservation reservation = entityManager.find(Reservation.class, id);
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            entityManager.merge(reservation);
        }
    }
    
    /**
     * Mark reservation as notified.
     */
    @Transactional
    public void markAsNotified(UUID id) {
        Reservation reservation = entityManager.find(Reservation.class, id);
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.NOTIFIED);
            reservation.setNotifiedAt(LocalDateTime.now());
            entityManager.merge(reservation);
        }
    }
    
    /**
     * Mark reservation as fulfilled.
     */
    @Transactional
    public void markAsFulfilled(UUID id) {
        Reservation reservation = entityManager.find(Reservation.class, id);
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.FULFILLED);
            entityManager.merge(reservation);
        }
    }
    
    /**
     * Mark expired reservations.
     */
    @Transactional
    public int markExpiredReservations() {
        return entityManager.createQuery(
            "UPDATE Reservation r SET r.status = :expiredStatus " +
            "WHERE r.status = :activeStatus AND r.expiresAt < :now")
            .setParameter("expiredStatus", ReservationStatus.EXPIRED)
            .setParameter("activeStatus", ReservationStatus.ACTIVE)
            .setParameter("now", LocalDateTime.now())
            .executeUpdate();
    }
}

// Made with Bob
