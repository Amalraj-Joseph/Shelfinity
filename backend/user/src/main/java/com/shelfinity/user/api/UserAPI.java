/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import com.shelfinity.user.dto.requests.RegisterUserRequestDTO;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tags(@Tag(name = "users", description = "API to manage user resources"))
public interface UserAPI {

    @Operation(
        summary = "New user registration request",
        description = "A new user submits his/her registration form",
        operationId = "register_user"
    )
    @APIResponse(
        responseCode = "201",
        description = "Successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
        responseCode = "409",
        description = "User already exists",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
        responseCode = "400",
        description = "Bad Request",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @POST
    Response registerUser(
        @RequestBody(
            description = "Values needed for submitting a registration request.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = RegisterUserRequestDTO.class)
            )
        )
        @NotNull
        @Valid
        RegisterUserRequestDTO request
    );
}
