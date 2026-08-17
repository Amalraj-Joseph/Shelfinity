/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues.dto.responses;

import com.shelfinity.books.Book;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueStatus;
import com.shelfinity.queues.QueueType;
import com.shelfinity.users.User;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for queue item responses.
 */
public class QueueItemResponse {

    private UUID id;
    private QueueType type;
    private String userKeycloakId;
    private String userName;
    private String userEmail;
    private UUID bookId;
    private String bookTitle;
    private String bookIsbn;
    private QueueStatus status;
    private String description;
    private String adminRemark;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private String processedBy;

    // Default constructor
    public QueueItemResponse() {}

    // Constructor from QueueItem entity.
    // dueDate was missing entirely here until an end-to-end smoke test showed
    // the API response never carrying it, even though QueueApprovalService
    // correctly sets and persists it on borrow approval — no unit/mock test
    // caught this because none of them serialize a real QueueItemResponse and
    // inspect the JSON a client actually receives.
    public QueueItemResponse(QueueItem queueItem) {
        this(queueItem, null, null);
    }

    // Enriched constructor: resolves the book/user relations so admin UIs can
    // display a title/ISBN and a name/email instead of raw UUIDs.
    public QueueItemResponse(QueueItem queueItem, Book book, User user) {
        this.id = queueItem.getId();
        this.type = queueItem.getType();
        this.userKeycloakId = queueItem.getUserKeycloakId();
        this.bookId = queueItem.getBookId();
        this.status = queueItem.getStatus();
        this.description = queueItem.getDescription();
        this.adminRemark = queueItem.getAdminRemark();
        this.dueDate = queueItem.getDueDate();
        this.createdAt = queueItem.getCreatedAt();
        this.updatedAt = queueItem.getUpdatedAt();
        this.processedAt = queueItem.getProcessedAt();
        this.processedBy = queueItem.getProcessedBy();

        if (book != null) {
            this.bookTitle = book.getTitle();
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
    
    public QueueType getType() {
        return type;
    }
    
    public void setType(QueueType type) {
        this.type = type;
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

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public QueueStatus getStatus() {
        return status;
    }
    
    public void setStatus(QueueStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAdminRemark() {
        return adminRemark;
    }
    
    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
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
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getProcessedBy() {
        return processedBy;
    }
    
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }
    
    @Override
    public String toString() {
        return "QueueItemResponse{" +
                "id=" + id +
                ", type=" + type +
                ", userKeycloakId='" + userKeycloakId + '\'' +
                ", userName='" + userName + '\'' +
                ", bookId=" + bookId +
                ", bookTitle='" + bookTitle + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
