/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.requests;

import java.time.LocalDate;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.common.annotations.agerange.AgeRange;
import com.shelfinity.user.dto.AddressDTO;
import com.shelfinity.user.dto.NameDTO;
import com.shelfinity.user.dto.enums.Gender;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used for submitting registration request of a new
 * user via REST APIs. This class captures the data needed from the client
 * during user registration.
 */
@Schema(description = "DTO representing the request payload for registering a new user")
public class RegisterUserRequestDTO {

    @Schema(description = "Name of the User", required = true)
    @NotNull(message = "Name is required")
    @Valid
    private NameDTO name;

    @Schema(description = "User's date of birth", example = "1995-08-20", required = true)
    @NotNull(message = "Date of birth is required")
    @AgeRange(min = 12, max = 130, message = "User must be between 12 and 130 years old")
    private LocalDate dateOfBirth;

    @Schema(description = "User's gender", example = "MALE", required = false)
    private Gender gender;

    @Schema(description = "Unique username for the user", example = "johndoe123", required = true)
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can only contain letters, numbers, dots (.), underscores (_) and hyphens (-)"
    )
    private String username;

    @Schema(description = "Email of the user", example = "john.doe@example.com", required = true)
    @JsonbProperty("id")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "User's password", example = "Abcd1234!", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;

    @Schema(description = "Phone number with country code", example = "+91-1876543210", required = true)
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[1-9]{1,3}-[0-9]{7,12}$",
            message = "Phone number must be in the format +<country_code>-<number> (e.g., +91-1876543210)"
    )
    private String phoneNumber;

    @Schema(description = "Address of the user", required = true)
    @NotNull(message = "Address is required")
    @Valid
    private AddressDTO address;

    /**
     * Default constructor for RegisterUserRequestDTO. Initializes a new
     * instance of RegisterUserRequestDTO with default values.
     */
    public RegisterUserRequestDTO() {
        // Default constructor
    }

    /**
     * Constructs a RegisterUserRequestDTO with all required and optional user
     * profile details.
     *
     * @param name The user's name.
     * @param dateOfBirth The user's date of birth.
     * @param gender The user's gender (optional).
     * @param username The unique username of the user.
     * @param email The email of the user (used as identifier).
     * @param password The user's password.
     * @param phoneNumber The user's phone number with country code.
     * @param address The user's address.
     */
    public RegisterUserRequestDTO(
            NameDTO name,
            LocalDate dateOfBirth,
            Gender gender,
            String username,
            String email,
            String password,
            String phoneNumber,
            AddressDTO address
    ) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    /**
     * Provides a string representation of the RegisterUserRequestDTO object.
     *
     * @return A string containing the RegisterUserRequestDTO information.
     */
    @Override
    public String toString() {
        return "UserDTO{"
                + "salutation='" + name.getSalutation() + '\''
                + ", firstName='" + name.getFirstName() + '\''
                + ", middleName='" + name.getMiddleName() + '\''
                + ", lastName='" + name.getLastName() + '\''
                + ", email='" + email + '\''
                + '}';
    }

    /**
     * Compares this RegisterUserRequestDTO object to another object for
     * equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterUserRequestDTO registerUserRequestDTO = (RegisterUserRequestDTO) o;
        return Objects.equals(name.getSalutation(), registerUserRequestDTO.getName().getSalutation())
                && Objects.equals(name.getFirstName(), registerUserRequestDTO.getName().getFirstName())
                && Objects.equals(name.getMiddleName(), registerUserRequestDTO.getName().getMiddleName())
                && Objects.equals(name.getLastName(), registerUserRequestDTO.getName().getLastName())
                && Objects.equals(email, registerUserRequestDTO.email);
    }

    /**
     * Generates a hash code for the RegisterUserRequestDTO object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name.getSalutation(), name.getFirstName(), name.getMiddleName(), name.getLastName(), email);
    }

    // Getters and Setters below
    public NameDTO getName() {
        return name;
    }

    public void setName(NameDTO name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
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

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public static class Builder {

        private NameDTO name;
        private LocalDate dateOfBirth;
        private Gender gender;
        private String username;
        private String email;
        private String password;
        private String phoneNumber;
        private AddressDTO address;

        public Builder name(NameDTO name) {
            this.name = name;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder address(AddressDTO address) {
            this.address = address;
            return this;
        }

        public RegisterUserRequestDTO build() {
            return new RegisterUserRequestDTO(
                    name,
                    dateOfBirth,
                    gender,
                    username,
                    email,
                    password,
                    phoneNumber,
                    address
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
