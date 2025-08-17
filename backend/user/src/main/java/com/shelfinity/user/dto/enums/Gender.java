/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Gender of a person")
public enum Gender {

    MALE("male"),
    FEMALE("female"),
    NON_BINARY("non-binary"),
    OTHER("other"),
    PREFER_NOT_TO_SAY("prefer-not-to-say");

    private final String gender;

    Gender(String gender) {
        this.gender = gender;
    }

    /**
     * Gets the gender as a string.
     *
     * @return The gender as a string.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Gets a gender enum from a string value.
     *
     * @param gender gender.
     * @return The enum corresponding to the provided gender.
     * @throws IllegalArgumentException If the gender is invalid.
     */
    public static Gender fromString(String gender) {
        for (Gender gen : Gender.values()) {
            if (gen.gender.equalsIgnoreCase(gender)) {
                return gen;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + gender);
    }

    /**
     * Override toString() to return the gender as a string.
     *
     * @return The gender as a string.
     */
    @Override
    public String toString() {
        return gender;
    }
}
