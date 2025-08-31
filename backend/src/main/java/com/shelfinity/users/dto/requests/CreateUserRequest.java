/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO for creating a new user.
 */
@Schema(description = "Request to create a new user")
public class CreateUserRequest {
    
    @Schema(description = "Keycloak user ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotBlank(message = "Keycloak ID is required")
    private String keycloakId;
    
    @Schema(description = "User's email address", example = "user@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Schema(description = "User's full name", example = "John Doe")
    @NotBlank(message = "Name is required")
    private String name;
    
    @Schema(description = "User role", example = "USER")
    private String role = "USER";
    
    // Default constructor
    public CreateUserRequest() {}
    
    // Constructor with required fields
    public CreateUserRequest(String keycloakId, String email, String name) {
        this.keycloakId = keycloakId;
        this.email = email;
        this.name = name;
    }
    
    // Getters and Setters
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
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "keycloakId='" + keycloakId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
