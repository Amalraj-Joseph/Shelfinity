/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues.dto.responses;

import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueStatus;
import com.shelfinity.queues.QueueType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for queue item responses.
 */
public class QueueItemResponse {
    
    private UUID id;
    private QueueType type;
    private String userKeycloakId;
    private UUID bookId;
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
    
    public UUID getBookId() {
        return bookId;
    }
    
    public void setBookId(UUID bookId) {
        this.bookId = bookId;
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
                ", bookId=" + bookId +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
