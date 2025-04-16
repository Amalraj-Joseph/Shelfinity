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
