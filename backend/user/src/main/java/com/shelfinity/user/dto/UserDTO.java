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

import java.time.LocalDateTime;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.shelfinity.user.dto.enums.Role;

/**
 * Data Transfer Object (DTO) representing a user in the system, used for data transfer over REST APIs.
 * This class contains the user data that will be sent to/from the client and does not contain any database logic.
 */
@Schema(description = "User DTO representing a user in the system")
public class UserDTO {

    @Schema(description = "The full name of the user", example = "John Doe", required = true)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Unique identifier for the user", example = "john.doe@example.com", required = true)
    @JsonbProperty("id")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email; // Used as the user ID

    @JsonbTransient
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;

    @Schema(description = "The user's phone number", example = "+1234567890")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 to 15 digits")
    private String phoneNumber;

    @Schema(description = "The user's address", example = "123 Main St, Springfield, IL, 62701")
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Schema(description = "Role of the user in the system", required = true)
    @NotNull(message = "Role is required")
    private Role role;

    @Schema(description = "The date and time when the user was created", example = "2022-03-25T14:30:00", required = true)
    @PastOrPresent(message = "Created date must be in the past or present")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    @NotNull
    private LocalDateTime createdAt;

    @Schema(description = "The last login date and time of the user", example = "2023-03-25T14:30:00")
    @PastOrPresent(message = "Last login must be in the past or present")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLogin;

    @Schema(description = "Whether the user is locked", defaultValue = "false")
    @NotNull
    private boolean locked;

    @Schema(description = "Whether the user is enabled", defaultValue = "true")
    @NotNull
    private boolean enabled;

    /**
     * Default constructor for UserDTO.
     * Initializes a new instance of UserDTO with default values.
     */
    public UserDTO() {
        // Default constructor
    }

    /**
     * Constructs a UserDTO with the given parameters.
     *
     * @param name        The full name of the user.
     * @param email       The email of the user (also used as the unique identifier).
     * @param password    The user's password.
     * @param phoneNumber The phone number of the user.
     * @param address     The address of the user.
     * @param role        The role of the user.
     * @param createdAt   The creation timestamp of the user.
     * @param lastLogin   The last login timestamp of the user.
     * @param locked      Whether the user is locked.
     * @param enabled     Whether the user is enabled.
     */
    public UserDTO(String name, String email, String password, String phoneNumber, String address, Role role,
                   LocalDateTime createdAt, LocalDateTime lastLogin, boolean locked, boolean enabled) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.locked = locked;
        this.enabled = enabled;
    }

    /**
     * Provides a string representation of the UserDTO object.
     *
     * @return A string containing the UserDTO information.
     */
    @Override
    public String toString() {
        return "UserDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                ", locked=" + locked +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * Compares this UserDTO object to another object for equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return locked == userDTO.locked && enabled == userDTO.enabled &&
                Objects.equals(name, userDTO.name) &&
                Objects.equals(email, userDTO.email) &&
                Objects.equals(phoneNumber, userDTO.phoneNumber) &&
                Objects.equals(address, userDTO.address) &&
                role == userDTO.role &&
                Objects.equals(createdAt, userDTO.createdAt) &&
                Objects.equals(lastLogin, userDTO.lastLogin);
    }

    /**
     * Generates a hash code for the UserDTO object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, email, phoneNumber, address, role, createdAt, lastLogin, locked, enabled);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
