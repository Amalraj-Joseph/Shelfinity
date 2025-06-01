/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Enum representing the common response status for generic success or failure cases.
 */
@Schema(description = "The common response status indicating success or failure")
public enum CommonResponseStatus implements BaseStatus {

    /**
     * Indicates the operation was successful.
     */
    @Schema(description = "The operation was successful")
    SUCCESS("success"),

    /**
     * Indicates the operation failed.
     */
    @Schema(description = "The operation failed")
    FAILURE("failure");

    private final String statusName;

    CommonResponseStatus(String statusName) {
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
     * Gets a CommonResponseStatus enum from a string value.
     *
     * @param statusName The name of the status.
     * @return The CommonResponseStatus enum corresponding to the provided string.
     * @throws IllegalArgumentException If the status name is invalid.
     */
    public static CommonResponseStatus fromString(String statusName) {
        for (CommonResponseStatus status : CommonResponseStatus.values()) {
            if (status.statusName.equalsIgnoreCase(statusName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid common response status: " + statusName);
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
