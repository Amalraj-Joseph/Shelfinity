/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues.dto.requests;

import com.shelfinity.queues.QueueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for creating a new queue item.
 */
public class CreateQueueItemRequest {
    
    @NotNull(message = "Queue type is required")
    private QueueType type;
    
    @NotBlank(message = "User Keycloak ID is required")
    private String userKeycloakId;
    
    private UUID bookId;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    // Default constructor
    public CreateQueueItemRequest() {}
    
    // Constructor for user registration
    public CreateQueueItemRequest(QueueType type, String userKeycloakId, String description) {
        this.type = type;
        this.userKeycloakId = userKeycloakId;
        this.description = description;
    }
    
    // Constructor for book operations
    public CreateQueueItemRequest(QueueType type, String userKeycloakId, UUID bookId, String description) {
        this.type = type;
        this.userKeycloakId = userKeycloakId;
        this.bookId = bookId;
        this.description = description;
    }
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "CreateQueueItemRequest{" +
                "type=" + type +
                ", userKeycloakId='" + userKeycloakId + '\'' +
                ", bookId=" + bookId +
                ", description='" + description + '\'' +
                '}';
    }
}
