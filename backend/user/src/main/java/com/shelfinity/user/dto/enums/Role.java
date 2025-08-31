/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


/**
 * Enum representing the different roles a user can have in the system.
 */
@Schema(description = "The role of the user in the system")
public enum Role {

    /**
     * Represents an administrator with the highest level of privileges.
     */
    @Schema(description = "Administrator with full access to the system")
    ADMIN("admin"),

    /**
     * Represents a general user with limited access.
     */
    @Schema(description = "General user with limited access")
    USER("user");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Gets the role name as a string.
     *
     * @return The role name as a string.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Gets a Role enum from a string value.
     * 
     * @param roleName The name of the role.
     * @return The Role enum corresponding to the provided string.
     * @throws IllegalArgumentException If the role name is invalid.
     */
    public static Role fromString(String roleName) {
        for (Role role : Role.values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + roleName);
    }

    /**
     * Override toString() to return the role name as a string.
     *
     * @return The role name as a string.
     */
    @Override
    public String toString() {
        return roleName;
    }
}
