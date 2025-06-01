/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Enum representing the status of a user registration request.
 */
@Schema(description = "The status of a user registration request")
public enum RegistrationStatus implements BaseStatus {

    /**
     * The registration request is received and waiting for admin approval.
     */
    @Schema(description = "The registration request is pending admin approval")
    PENDING("pending"),

    /**
     * The registration request has been approved by an admin.
     */
    @Schema(description = "The registration request has been approved")
    APPROVED("approved"),

    /**
     * The registration request has been rejected by an admin.
     */
    @Schema(description = "The registration request has been rejected")
    REJECTED("rejected");

    private final String statusName;

    RegistrationStatus(String statusName) {
        this.statusName = statusName;
    }

    /**
     * Gets the status name as a string.
     *
     * @return The status name as a string.
     */
    @Override
    public String getStatusName() {
        return statusName;
    }

    /**
     * Gets a RegistrationStatus enum from a string value.
     *
     * @param statusName The name of the status.
     * @return The RegistrationStatus enum corresponding to the provided string.
     * @throws IllegalArgumentException If the status name is invalid.
     */
    public static RegistrationStatus fromString(String statusName) {
        for (RegistrationStatus status : RegistrationStatus.values()) {
            if (status.statusName.equalsIgnoreCase(statusName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid registration status: " + statusName);
    }

    /**
     * Override toString() to return the status name as a string.
     *
     * @return The status name as a string.
     */
    @Override
    public String toString() {
        return statusName;
    }
}
