/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a book reservation.
 */
public class CreateReservationRequest {
    
    @NotNull(message = "Book ID is required")
    private UUID bookId;
    
    private String notes;
    
    // Constructors
    public CreateReservationRequest() {
    }
    
    public CreateReservationRequest(UUID bookId, String notes) {
        this.bookId = bookId;
        this.notes = notes;
    }
    
    // Getters and Setters
    public UUID getBookId() {
        return bookId;
    }
    
    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "CreateReservationRequest{" +
                "bookId=" + bookId +
                ", notes='" + notes + '\'' +
                '}';
    }
}

// Made with Bob
