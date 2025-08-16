/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.admin.api;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/queue")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tags(@Tag(name = "Queue", description = "API to manage Admin Queue"))
public interface QueueAPI {

    @Operation(
        summary = "Admin accessing the QUEUE",
        description = "Fetch queue entries. All query params are optional and applied as filters (combined with AND).",
        operationId = "view_queue"
    )
    @APIResponse(
        responseCode = "200",
        description = "Successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @GET
    Response getQueue(
        @Parameter(
            description = "Filter by request ID",
            schema = @Schema(type = SchemaType.STRING, format = "uuid"),
            example = "a1b2c3d4-1111-2222-3333-444455556666"
        )
        @QueryParam("id") UUID id,

        @Parameter(
            description = "Filter by email address",
            schema = @Schema(type = SchemaType.STRING, format = "email"),
            example = "user@example.com"
        )
        @QueryParam("email") String email,

        @Parameter(
            description = "Filter by phone number (E.164 or local)",
            schema = @Schema(type = SchemaType.STRING),
            example = "+14155552671"
        )
        @QueryParam("phone") String phone,

        @Parameter(
            description = "Filter by username",
            schema = @Schema(type = SchemaType.STRING),
            example = "alice"
        )
        @QueryParam("username") String username
    );
}
