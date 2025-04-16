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
