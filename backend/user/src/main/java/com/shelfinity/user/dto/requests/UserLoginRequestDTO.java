/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.requests;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) representing the login request by a user.
 */
@Schema(description = "DTO representing a user login request with username and password")
public class UserLoginRequestDTO {

    @Schema(description = "The username of the user", required = true, example = "john.doe@example.com")
    @JsonbProperty("id")
    @NotBlank(message = "username is required")
    @Email(message = "Invalid username format")
    private String username;

    @Schema(description = "The password of the user", required = true, example = "securePassword123")
    private String password;

    /**
     * Default constructor for UserLoginRequestDTO.
     * Initializes a new instance of UserLoginRequestDTO with default values.
     */
    public UserLoginRequestDTO() {
        // Default constructor
    }

    /**
     * Constructs a UserLoginRequestDTO with the given username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     */
    public UserLoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Provides a string representation of the UserLoginRequestDTO object.
     *
     * @return A string containing the UserLoginRequestDTO information.
     */
    @Override
    public String toString() {
        return "UserLoginRequestDTO{" +
               "username='" + username + '\'' +
               ", password='***'" +
               '}';
    }

    /**
     * Compares this UserLoginRequestDTO object to another object for equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLoginRequestDTO that = (UserLoginRequestDTO) o;
        return Objects.equals(username, that.username);
    }

    /**
     * Generates a hash code for the UserLoginRequestDTO object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
