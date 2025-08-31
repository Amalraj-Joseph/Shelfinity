/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users.dto.responses;

import com.shelfinity.users.User;
import com.shelfinity.users.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user responses.
 */
public class UserResponse {
    
    private UUID id;
    private String keycloakId;
    private String email;
    private String name;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public UserResponse() {}
    
    // Constructor from User entity
    public UserResponse(User user) {
        this.id = user.getId();
        this.keycloakId = user.getKeycloakId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getKeycloakId() {
        return keycloakId;
    }
    
    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
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
    
    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", keycloakId='" + keycloakId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
