/*
 * MIT License
 * 
 * Copyright (c) 2025 Shadow-Codex
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
