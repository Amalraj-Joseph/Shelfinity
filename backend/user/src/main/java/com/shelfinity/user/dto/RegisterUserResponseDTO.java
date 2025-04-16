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
package com.shelfinity.user.dto;

import com.shelfinity.user.dto.enums.RegistrationStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

/**
 * Data Transfer Object (DTO) for responding to a user registration request.
 * Indicates that the request has been received and is pending admin approval.
 */
@Schema(description = "DTO representing the response after user registration request is received")
public class RegisterUserResponseDTO {

    @Schema(description = "The status of the user registration request", required = true)
    private RegistrationStatus status;

    @Schema(description = "Human-readable message for the registration status", required = true)
    private String message;

    /**
     * Default constructor for RegisterUserResponseDTO.
     * Initializes a new instance of RegisterUserResponseDTO with default values.
     */
    public RegisterUserResponseDTO() {
        // Default constructor
    }

    /**
     * Constructs a RegisterUserResponseDTO with the given parameters.
     *
     * @param status  The registration status of the request (e.g. PENDING, APPROVED).
     * @param message The human-readable message associated with the registration status.
     */
    public RegisterUserResponseDTO(RegistrationStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Provides a string representation of the RegisterUserResponseDTO object.
     *
     * @return A string containing the RegisterUserResponseDTO information.
     */
    @Override
    public String toString() {
        return "RegisterUserResponseDTO{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

    /**
     * Compares this RegisterUserResponseDTO object to another object for equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterUserResponseDTO that = (RegisterUserResponseDTO) o;
        return status == that.status &&
               Objects.equals(message, that.message);
    }

    /**
     * Generates a hash code for the RegisterUserResponseDTO object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, message);
    }

    // Getters and Setters below

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
