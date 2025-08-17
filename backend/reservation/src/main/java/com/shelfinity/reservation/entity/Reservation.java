/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservation.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    private UUID id;
    private UUID bookId;
    private UUID userId;
    private Instant reservedFrom;
    private Instant reservedTo;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        ACTIVE, CANCELLED, EXPIRED, FULFILLED
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // getters/setters omitted
    public UUID getId() {
        return id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getReservedFrom() {
        return reservedFrom;
    }

    public Instant getReservedTo() {
        return reservedTo;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setReservedFrom(Instant reservedFrom) {
        this.reservedFrom = reservedFrom;
    }

    public void setReservedTo(Instant reservedTo) {
        this.reservedTo = reservedTo;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
