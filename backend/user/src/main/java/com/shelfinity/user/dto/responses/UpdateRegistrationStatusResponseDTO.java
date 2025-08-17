/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.responses;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.user.dto.enums.RegistrationStatus;

@Schema(description = "Result of updating the registration request status")
public class UpdateRegistrationStatusResponseDTO {

    private UUID id;
    private RegistrationStatus status;
    private String remark;
    private boolean movedToUsers;
    private Instant lastUpdated;
    private UUID updatedBy;

    public UpdateRegistrationStatusResponseDTO(UUID id, RegistrationStatus status, String remark, boolean movedToUsers,
            Instant lastUpdated, UUID updatedBy) {
        this.id = id;
        this.status = status;
        this.remark = remark;
        this.movedToUsers = movedToUsers;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updatedBy;
    }

    public UpdateRegistrationStatusResponseDTO() {
    }

    public UUID getId() {
        return id;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public String getRemark() {
        return remark;
    }

    public boolean isMovedToUsers() {
        return movedToUsers;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setMovedToUsers(boolean movedToUsers) {
        this.movedToUsers = movedToUsers;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
