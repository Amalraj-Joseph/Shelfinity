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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used for updating a user's password.
 * This DTO captures the required fields for authenticating and performing the update.
 */
@Schema(description = "DTO representing the request payload for updating user password")
public class UpdateUserPasswordRequestDTO {

    @Schema(description = "Email of the user", example = "john.doe@example.com", required = true)
    @JsonbProperty("id")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "Current password of the user", required = true)
    @NotBlank(message = "Old password is required")
    @Size(min = 8, message = "Old password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Old password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String oldPassword;

    @Schema(description = "New password of the user", required = true)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "New password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String newPassword;

    /**
     * Default constructor for UpdateUserPasswordRequestDTO.
     * Initializes a new instance of UpdateUserPasswordRequestDTO with default values.
     */
    public UpdateUserPasswordRequestDTO() {
        // Default constructor
    }

    /**
     * Constructs a UpdateUserPasswordRequestDTO with the given parameters.
     *
     * @param email       The email of the user (also used as the unique identifier).
     * @param oldPassword The user's old password.
     * @param newPassword The user's new password.
     */
    public UpdateUserPasswordRequestDTO(String email, String oldPassword, String newPassword) {
        this.email = email;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    /**
     * Returns a string representation of the UpdateUserPasswordRequestDTO object.
     * Passwords are intentionally excluded for security reasons.
     *
     * @return A string containing the email of the user.
     */
    @Override
    public String toString() {
        return "UpdateUserPasswordRequestDTO{" +
                "email='" + email + '\'' +
                '}';
    }

    /**
     * Checks whether this object is equal to another.
     * Compares based on email only.
     *
     * @param o The object to compare with.
     * @return true if the email matches; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdateUserPasswordRequestDTO)) return false;
        UpdateUserPasswordRequestDTO that = (UpdateUserPasswordRequestDTO) o;
        return Objects.equals(email, that.email);
    }

    /**
     * Generates a hash code based on the email.
     *
     * @return A hash code value.
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    // Getters and Setters

    /**
     * Returns the email of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the current password.
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * Sets the current password.
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * Returns the new password.
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the new password.
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
