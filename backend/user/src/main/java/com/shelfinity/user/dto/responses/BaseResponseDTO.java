/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.responses;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.user.dto.enums.BaseStatus;

/**
 * Generic base class for all response DTOs with a status.
 *
 * @param <T> Type of status that implements BaseStatus.
 */
@Schema(description = "Base class for response DTOs with a status")
public class BaseResponseDTO<T extends BaseStatus> {

    @Schema(description = "Human-readable message for the response", required = true)
    private String message;

    @Schema(description = "Status object for the response", required = true)
    private T status;

    /**
     * Default constructor for BaseResponseDTO. Initializes a new instance of
     * BaseResponseDTO with default values.
     */
    public BaseResponseDTO() {
        // Default constructor
    }

    /**
     * Constructs a BaseResponseDTO with the given parameters.
     *
     * @param status The operation status.
     * @param message Human readable message for the user
     */
    public BaseResponseDTO(T status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Provides a string representation of the BaseResponseDTO object.
     *
     * @return A string containing the BaseResponseDTO information.
     */
    @Override
    public String toString() {
        return "BaseResponseDTO{"
                + "status=" + status
                + ", message='" + message + '\''
                + '}';
    }

    /**
     * Compares this BaseResponseDTO object to another object for equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseResponseDTO)) {
            return false;
        }
        BaseResponseDTO<?> that = (BaseResponseDTO<?>) o;
        return Objects.equals(status, that.status)
                && Objects.equals(message, that.message);
    }

    /**
     * Generates a hash code for the BaseResponseDTO object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, message);
    }

    // Getters and Setters below
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getStatus() {
        return status;
    }

    public void setStatus(T status) {
        this.status = status;
    }
}
