/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.user.dto.enums.RegistrationStatus;

@Schema(description = "Patch to update status/remark of a registration request")
public class UpdateRegistrationStatusRequestDTO {

    @Schema(description = "New status. If omitted, status remains unchanged (e.g. only remark update).",
            implementation = RegistrationStatus.class,
            example = "APPROVED")
    private RegistrationStatus status; // nullable

    @Schema(description = "Optional remark to store alongside the status change.", example = "Verified KYC manually")
    private String remark; // nullable

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
