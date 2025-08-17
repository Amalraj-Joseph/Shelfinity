/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.api;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import com.shelfinity.user.dto.requests.RegisterUserRequestDTO;
import com.shelfinity.user.dto.requests.UpdateUserProfileRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tags(
        @Tag(name = "users", description = "API to manage user resources"))
public interface UserAPI {

    @POST
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
    Response registerUser(
            @RequestBody(
                    description = "Values needed for submitting a registration request.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RegisterUserRequestDTO.class)
                    )
            )
            @NotNull
            @Valid RegisterUserRequestDTO request
    );

    @Path("/{id}")
    @GET
    @Operation(
            summary = "Accessing a User Entity",
            description = "Fetching a User record from the DB.",
            operationId = "view_user"
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
    @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    Response getUser(
            @Parameter(
                    description = "Filter by request ID",
                    schema = @Schema(type = SchemaType.STRING, format = "uuid"),
                    example = "a1b2c3d4-1111-2222-3333-444455556666"
            )
            @PathParam("id") UUID id
    );

    @Path("/{id}")
    @PUT
    @Operation(
            summary = "Updating a User Entity",
            description = "Fetching a User record from the DB.",
            operationId = "update_user"
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
    @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    Response updateUser(
            @Parameter(
                    description = "Filter by request ID",
                    schema = @Schema(type = SchemaType.STRING, format = "uuid"),
                    example = "a1b2c3d4-1111-2222-3333-444455556666"
            )
            @PathParam("id") UUID id,
            @Valid UpdateUserProfileRequestDTO update
    );
}
