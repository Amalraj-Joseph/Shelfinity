/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservation.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.shelfinity.reservation.dto.requests.CreateReservationRequestDTO;
import com.shelfinity.reservation.dto.responses.ReservationDTO;
import com.shelfinity.reservation.entity.Reservation;
import com.shelfinity.reservation.repository.ReservationRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ReservationService {

    @Inject
    ReservationRepository repo;

    public UUID create(CreateReservationRequestDTO in) {
        Reservation r = new Reservation();
        r.setBookId(in.bookId);
        r.setUserId(in.userId);
        r.setReservedFrom(Instant.parse(in.reservedFrom));
        r.setReservedTo(Instant.parse(in.reservedTo));
        repo.add(r);
        return r.getId();
    }

    public void cancel(UUID id) {
        Reservation r = repo.get(id);
        if (r != null) {
            r.setStatus(Reservation.Status.CANCELLED);
            repo.merge(r);
        }
    }

    public ReservationDTO get(UUID id) {
        Reservation r = repo.get(id);
        if (r == null) {
            return null;
        }
        return toDTO(r);
    }

    public List<ReservationDTO> list(int offset, int limit) {
        return repo.list(offset, limit).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ReservationDTO toDTO(Reservation r) {
        ReservationDTO d = new ReservationDTO();
        d.id = r.getId();
        d.bookId = r.getBookId();
        d.userId = r.getUserId();
        d.reservedFrom = r.getReservedFrom();
        d.reservedTo = r.getReservedTo();
        d.status = r.getStatus();
        d.createdAt = r.getCreatedAt();
        d.updatedAt = r.getUpdatedAt();
        return d;
    }
}
