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
package com.shelfinity.user.dto.responses;

import com.shelfinity.user.dto.enums.BaseStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

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
     * Default constructor for BaseResponseDTO.
     * Initializes a new instance of BaseResponseDTO with default values.
     */
    public BaseResponseDTO() {
        // Default constructor
    }

    /**
     * Constructs a BaseResponseDTO with the given parameters.
     *
     * @param status      The operation status.
     * @param message     Human readable message for the user
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
        return "BaseResponseDTO{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

    /**
     * Compares this BaseResponseDTO object to another object for equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseResponseDTO)) return false;
        BaseResponseDTO<?> that = (BaseResponseDTO<?>) o;
        return Objects.equals(status, that.status) &&
               Objects.equals(message, that.message);
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
