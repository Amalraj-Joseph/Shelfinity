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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO representing the request payload for updating an existing user's profile.
 * Password is intentionally excluded; it should be updated via a separate flow.
 */
@Schema(description = "DTO representing the request payload for updating a user's profile (excluding password)")
public class UpdateUserProfileRequestDTO {

    @Schema(description = "The full name of the user", example = "John Doe", required = true)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Email of the user", example = "john.doe@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "Phone number", example = "+1234567890", required = true)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 to 15 digits")
    private String phoneNumber;

    @Schema(description = "Address of the user", example = "123 Main St, Springfield, IL, 62701", required = true)
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    /**
     * Default constructor for UpdateUserProfileRequestDTO.
     * Initializes a new instance of UpdateUserProfileRequestDTO with default values.
     */
    public UpdateUserProfileRequestDTO() {
        // Default constructor
    }

    /**
     * Constructs a UpdateUserProfileRequestDTO with the given parameters.
     *
     * @param name        The full name of the user.
     * @param email       The email of the user (also used as the unique identifier).
     * @param phoneNumber The phone number of the user.
     * @param address     The address of the user.
     */
    public UpdateUserProfileRequestDTO(String name, String email, String phoneNumber, String address) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    /**
     * Returns a string representation of the UpdateUserProfileRequestDTO object.
     * The password field is intentionally omitted for security reasons.
     *
     * @return A string containing the name, email, phone number, and address of the user.
     */
    @Override
    public String toString() {
        return "UpdateUserProfileRequestDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two UpdateUserProfileRequestDTO objects are considered equal if their name,
     * email, phone number, and address are equal. Password is not considered in equality check.
     *
     * @param o The reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdateUserProfileRequestDTO)) return false;
        UpdateUserProfileRequestDTO that = (UpdateUserProfileRequestDTO) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(email, that.email) &&
               Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(address, that.address);
    }

    /**
     * Returns a hash code value for the object.
     * This implementation considers the name, email, phone number, and address.
     * Password is excluded for consistency with equals().
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, email, phoneNumber, address);
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
