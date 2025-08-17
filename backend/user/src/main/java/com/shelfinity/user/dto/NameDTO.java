/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.user.dto.enums.Salutation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) representing the name of a user.
 */
public class NameDTO {

    @Schema(description = "User's salutation (e.g., Mr, Mrs, Ms, Dr)", example = "MR", required = true)
    @NotNull(message = "Salutation is required")
    private Salutation salutation;

    @Schema(description = "User's first name", example = "John", required = true)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Schema(description = "User's middle name", example = "A.", required = false)
    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    private String middleName;

    @Schema(description = "User's last name", example = "Doe", required = true)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    public NameDTO() {
    }

    /**
     * Constructs a RegisterUserRequestDTO with all required and optional user
     * profile details.
     *
     * @param salutation The user's salutation (e.g., Mr, Ms, Dr).
     * @param firstName The user's first name.
     * @param middleName The user's middle name (optional).
     * @param lastName The user's last name.
     */
    public NameDTO(Salutation salutation, String firstName, String middleName, String lastName) {
        this.salutation = salutation;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    // --- Getters and Setters ---
    public Salutation getSalutation() {
        return salutation;
    }

    public void setSalutation(Salutation salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // --- equals & hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NameDTO nameDto)) {
            return false;
        }
        return salutation == nameDto.salutation
                && Objects.equals(firstName, nameDto.firstName)
                && Objects.equals(middleName, nameDto.middleName)
                && Objects.equals(lastName, nameDto.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salutation, firstName, middleName, lastName);
    }

    // --- Builder pattern ---
    public static class Builder {

        private Salutation salutation;
        private String firstName;
        private String middleName;
        private String lastName;

        public Builder salutation(Salutation salutation) {
            this.salutation = salutation;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public NameDTO build() {
            return new NameDTO(salutation, firstName, middleName, lastName);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
