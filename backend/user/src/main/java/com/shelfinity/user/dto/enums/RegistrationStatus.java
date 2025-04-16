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
package com.shelfinity.user.dto.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Enum representing the status of a user registration request.
 */
@Schema(description = "The status of a user registration request")
public enum RegistrationStatus {

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
