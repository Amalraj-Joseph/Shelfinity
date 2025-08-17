/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.responses;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.user.dto.enums.CommonResponseStatus;

/**
 * DTO representing a generic success/failure response.
 */
@Schema(description = "Generic response DTO with success or failure status")
public class CommonResponseDTO extends BaseResponseDTO<CommonResponseStatus> {

    /**
     * Default constructor for CommonResponseDTO. Initializes a new instance of
     * CommonResponseDTO with default values.
     */
    public CommonResponseDTO() {
        super();
    }

    /**
     * Constructs a CommonResponseDTO with the given parameters.
     *
     * @param status The operation status.
     * @param message Human readable message for the user
     */
    public CommonResponseDTO(CommonResponseStatus status, String message) {
        super(status, message);
    }
}
