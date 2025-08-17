/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservation.dto.responses;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.reservation.entity.Reservation.Status;

@Schema(name = "Reservation")
public class ReservationDTO {

    public UUID id;
    public UUID bookId;
    public UUID userId;
    public Instant reservedFrom;
    public Instant reservedTo;
    public Status status;
    public Instant createdAt;
    public Instant updatedAt;
}
