/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservation.api.impl;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.reservation.dto.requests.CreateReservationRequestDTO;
import com.shelfinity.reservation.dto.responses.ReservationDTO;
import com.shelfinity.reservation.service.ReservationService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/reservations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Reservations", description = "Manage reservations")
public class ReservationImpl {

    @Inject
    ReservationService service;

    @POST
    @Operation(summary = "Create reservation")
    @APIResponse(responseCode = "201", description = "Created")
    public Response create(@Valid @RequestBody(
            description = "Create reservation", required = true,
            content = @Content(schema = @Schema(implementation = CreateReservationRequestDTO.class))) CreateReservationRequestDTO in) {
        UUID id = service.create(in);
        return Response.status(Response.Status.CREATED)
                .entity("{\"id\":\"" + id + "\"}")
                .build();

    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Cancel reservation")
    public Response cancel(@Parameter(description = "Reservation ID") @PathParam("id") UUID id) {
        service.cancel(id);
        return Response.ok().build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Get reservation")
    public Response get(@Parameter(description = "Reservation ID") @PathParam("id") UUID id) {
        ReservationDTO d = service.get(id);
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(d).build();
    }

    @GET
    @Operation(summary = "List reservations")
    public List<ReservationDTO> list(@QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        return service.list(offset, limit);
    }
}
