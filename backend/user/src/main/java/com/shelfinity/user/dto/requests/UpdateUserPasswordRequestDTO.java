/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used for updating a user's password. This DTO
 * captures the required fields for authenticating and performing the update.
 */
@Schema(description = "DTO representing the request payload for updating user password")
public class UpdateUserPasswordRequestDTO {

    @Schema(description = "Current password of the user", required = true)
    @NotBlank(message = "Old password is required")
    @Size(min = 8, message = "Old password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String oldPassword;

    @Schema(description = "New password of the user", required = true)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String newPassword;

    /**
     * Default constructor for UpdateUserPasswordRequestDTO. Initializes a new
     * instance of UpdateUserPasswordRequestDTO with default values.
     */
    public UpdateUserPasswordRequestDTO() {
        // Default constructor
    }

    /**
     * Constructs a UpdateUserPasswordRequestDTO with the given parameters.
     *
     * @param oldPassword The user's old password.
     * @param newPassword The user's new password.
     */
    public UpdateUserPasswordRequestDTO(String email, String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters and Setters
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
