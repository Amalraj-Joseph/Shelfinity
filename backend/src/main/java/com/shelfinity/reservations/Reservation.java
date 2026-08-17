/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.shelfinity.persistence.UuidStringConverter;

/**
 * Entity representing a book reservation.
 */
@Entity
@Table(name = "reservations")
@NamedQueries({
    @NamedQuery(name = "Reservation.findAll", query = "SELECT r FROM Reservation r ORDER BY r.createdAt DESC"),
    @NamedQuery(name = "Reservation.findByUserKeycloakId", query = "SELECT r FROM Reservation r WHERE r.userKeycloakId = :userKeycloakId ORDER BY r.createdAt DESC"),
    @NamedQuery(name = "Reservation.findByBookId", query = "SELECT r FROM Reservation r WHERE r.bookId = :bookId ORDER BY r.createdAt ASC"),
    @NamedQuery(name = "Reservation.findByStatus", query = "SELECT r FROM Reservation r WHERE r.status = :status ORDER BY r.createdAt ASC"),
    // Was a bare 'ACTIVE' string literal instead of a bound :status parameter —
    // EclipseLink can't compare a String literal against an enum-mapped column
    // and throws at query-prepare time. This is the exact query
    // QueueApprovalService.applyReturnApproval() calls to promote the next
    // reservation on a book return, so reservation promotion never actually
    // worked at runtime; caught by ReservationRepositoryIT, not the mocked
    // QueueApprovalServiceTest, which is the whole reason the repository tier
    // exists (SPEC.md testing decisions log).
    @NamedQuery(name = "Reservation.findActiveByBookId", query = "SELECT r FROM Reservation r WHERE r.bookId = :bookId AND r.status = :status ORDER BY r.createdAt ASC")
})
public class Reservation {
    
    // See UuidStringConverter for why (SPEC.md §10.8).
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Convert(converter = UuidStringConverter.class)
    private UUID id;

    @NotNull
    @Column(name = "user_keycloak_id", nullable = false)
    private String userKeycloakId;

    @NotNull
    @Column(name = "book_id", nullable = false)
    @Convert(converter = UuidStringConverter.class)
    private UUID bookId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "notes")
    private String notes;
    
    // Default constructor
    public Reservation() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getUserKeycloakId() {
        return userKeycloakId;
    }
    
    public void setUserKeycloakId(String userKeycloakId) {
        this.userKeycloakId = userKeycloakId;
    }
    
    public UUID getBookId() {
        return bookId;
    }
    
    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }
    
    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // expiresAt is set explicitly by ReservationResource (configurable via
        // reservation.expiry.days) rather than hardcoded here — SPEC.md §10.5.
        // JPA entity lifecycle callbacks have no CDI/MicroProfile Config access.
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", userKeycloakId='" + userKeycloakId + '\'' +
                ", bookId=" + bookId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

// Made with Bob
