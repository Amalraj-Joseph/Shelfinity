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
package com.shelfinity.user.dto.responses;

import com.shelfinity.user.dto.enums.RegistrationStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO representing the response after a user registration request.
 */
@Schema(description = "DTO representing the response after user registration request is received")
public class RegisterUserResponseDTO extends BaseResponseDTO<RegistrationStatus> {

    /**
     * Default constructor for RegisterUserResponseDTO.
     * Initializes a new instance of RegisterUserResponseDTO with default values.
     */
    public RegisterUserResponseDTO() {
        super();
    }

    /**
     * Constructs a RegisterUserResponseDTO with the given parameters.
     *
     * @param status      The operation status.
     * @param message     Human readable message for the user
     */
    public RegisterUserResponseDTO(RegistrationStatus status, String message) {
        super(status, message);
    }
}
