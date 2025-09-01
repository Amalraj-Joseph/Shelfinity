/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Queue item entity for admin approval queue.
 */
@Entity
@Table(name = "queue_items")
@NamedQueries({
    @NamedQuery(name = "QueueItem.findAll", query = "SELECT q FROM QueueItem q ORDER BY q.createdAt DESC"),
    @NamedQuery(name = "QueueItem.findByStatus", query = "SELECT q FROM QueueItem q WHERE q.status = :status ORDER BY q.createdAt DESC"),
    @NamedQuery(name = "QueueItem.findByType", query = "SELECT q FROM QueueItem q WHERE q.type = :type ORDER BY q.createdAt DESC"),
    @NamedQuery(name = "QueueItem.findPending", query = "SELECT q FROM QueueItem q WHERE q.status = 'PENDING' ORDER BY q.createdAt ASC")
})
public class QueueItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueType type;
    
    @NotBlank
    @Column(name = "user_keycloak_id", nullable = false)
    private String userKeycloakId;
    
    @Column(name = "book_id")
    private UUID bookId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.PENDING;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 1000)
    private String adminRemark;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "processed_by")
    private String processedBy;
    
    // Default constructor
    public QueueItem() {
        // JPA will handle createdAt through @PrePersist
    }
    
    // Constructor for user registration
    public QueueItem(QueueType type, String userKeycloakId, String description) {
        this();
        this.type = type;
        this.userKeycloakId = userKeycloakId;
        this.description = description;
    }
    
    // Constructor for book operations
    public QueueItem(QueueType type, String userKeycloakId, UUID bookId, String description) {
        this(type, userKeycloakId, description);
        this.bookId = bookId;
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
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "QueueItem{" +
                "id=" + id +
                ", type=" + type +
                ", userKeycloakId='" + userKeycloakId + '\'' +
                ", bookId=" + bookId +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueItem queueItem = (QueueItem) o;
        return id != null && id.equals(queueItem.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
