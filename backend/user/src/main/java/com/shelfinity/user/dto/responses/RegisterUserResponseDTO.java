/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.responses;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.user.dto.enums.RegistrationStatus;

/**
 * DTO representing the response after a user registration request.
 */
@Schema(description = "DTO representing the response after user registration request is received")
public class RegisterUserResponseDTO extends BaseResponseDTO<RegistrationStatus> {

    /**
     * Default constructor for RegisterUserResponseDTO. Initializes a new
     * instance of RegisterUserResponseDTO with default values.
     */
    public RegisterUserResponseDTO() {
        super();
    }

    /**
     * Constructs a RegisterUserResponseDTO with the given parameters.
     *
     * @param status The operation status.
     * @param message Human readable message for the user
     */
    public RegisterUserResponseDTO(RegistrationStatus status, String message) {
        super(status, message);
    }
}
