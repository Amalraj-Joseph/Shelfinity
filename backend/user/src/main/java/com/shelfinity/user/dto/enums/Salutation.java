/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Salutation of a person")
public enum Salutation {

    MR("Mr"),
    MRS("Mrs"),
    MS("Ms"),
    DR("Dr"),
    PROF("Prof");

    private final String salutation;

    Salutation(String salutation) {
        this.salutation = salutation;
    }

    /**
     * Gets the salutation as a string.
     *
     * @return The salutation as a string.
     */
    public String getSalutation() {
        return salutation;
    }

    /**
     * Gets a Salutation enum from a string value.
     *
     * @param salutation salutation.
     * @return The enum corresponding to the provided salutation.
     * @throws IllegalArgumentException If the salutation is invalid.
     */
    public static Salutation fromString(String salutation) {
        for (Salutation sal : Salutation.values()) {
            if (sal.salutation.equalsIgnoreCase(salutation)) {
                return sal;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + salutation);
    }

    /**
     * Override toString() to return the salutation as a string.
     *
     * @return The salutation as a string.
     */
    @Override
    public String toString() {
        return salutation;
    }
}
