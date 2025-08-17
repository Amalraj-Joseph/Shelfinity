/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.responses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO representing a collection of all user email addresses in the system. This
 * is a lightweight projection used for lookups and validations.
 */
@Schema(description = "DTO containing all user email addresses")
public class AllUsersDTO {

    @Schema(
            description = "List of all user email addresses",
            required = true,
            example = "[\"john@example.com\", \"jane@example.com\"]"
    )
    @NotEmpty(message = "Email list cannot be empty")
    private final List<@Email(message = "Invalid email address") String> emails;

    /**
     * Constructs an AllUsersDTO with the provided list of email addresses.
     * Performs a defensive copy and sorts the list.
     *
     * @param emails list of user email addresses
     */
    public AllUsersDTO(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("Email list cannot be null or empty");
        }

        this.emails = Collections.unmodifiableList(
                new ArrayList<>(emails).stream()
                        .map(String::trim)
                        .sorted(String::compareToIgnoreCase)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Gets the list of user email addresses (sorted, immutable).
     *
     * @return unmodifiable list of emails
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return string representation of AllUsersDTO
     */
    @Override
    public String toString() {
        return "AllUsersDTO{"
                + "emails=" + emails
                + '}';
    }

    /**
     * Compares this object with the specified object for equality. Emails are
     * compared after sorting in a case-insensitive manner.
     *
     * @param o the object to compare with
     * @return true if both objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AllUsersDTO)) {
            return false;
        }
        AllUsersDTO that = (AllUsersDTO) o;
        return emails.equals(that.emails);
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return hash code of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(emails);
    }
}
