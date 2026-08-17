/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.shelfinity.books.Book;
import com.shelfinity.reservations.Reservation;
import com.shelfinity.reservations.ReservationStatus;
import com.shelfinity.users.User;

/**
 * Response DTO for reservation information.
 */
public class ReservationResponse {

    private UUID id;
    private String userKeycloakId;
    private String userName;
    private String userEmail;
    private UUID bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime notifiedAt;
    private String notes;

    // Constructors
    public ReservationResponse() {
    }

    public ReservationResponse(Reservation reservation, Book book) {
        this(reservation, book, null);
    }

    public ReservationResponse(Reservation reservation, Book book, User user) {
        this.id = reservation.getId();
        this.userKeycloakId = reservation.getUserKeycloakId();
        this.bookId = reservation.getBookId();
        this.status = reservation.getStatus();
        this.createdAt = reservation.getCreatedAt();
        this.updatedAt = reservation.getUpdatedAt();
        this.expiresAt = reservation.getExpiresAt();
        this.notifiedAt = reservation.getNotifiedAt();
        this.notes = reservation.getNotes();

        if (book != null) {
            this.bookTitle = book.getTitle();
            this.bookAuthor = book.getAuthor();
            this.bookIsbn = book.getIsbn();
        }

        if (user != null) {
            this.userName = user.getName();
            this.userEmail = user.getEmail();
        }
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public UUID getBookId() {
        return bookId;
    }
    
    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
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
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }
    
    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "ReservationResponse{" +
                "id=" + id +
                ", userKeycloakId='" + userKeycloakId + '\'' +
                ", userName='" + userName + '\'' +
                ", bookId=" + bookId +
                ", bookTitle='" + bookTitle + '\'' +
                ", bookAuthor='" + bookAuthor + '\'' +
                ", bookIsbn='" + bookIsbn + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", expiresAt=" + expiresAt +
                ", notifiedAt=" + notifiedAt +
                ", notes='" + notes + '\'' +
                '}';
    }
}

// Made with Bob
