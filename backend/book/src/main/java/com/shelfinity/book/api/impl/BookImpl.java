/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.book.api.impl;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.book.dto.requests.CreateBookRequestDTO;
import com.shelfinity.book.dto.requests.UpdateBookRequestDTO;
import com.shelfinity.book.dto.responses.BookDTO;
import com.shelfinity.book.service.BookService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/books")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name="Books", description="Manage library books")
public class BookImpl {
    @Inject BookService service;

    @POST
    @Operation(summary="Create book")
    @APIResponse(responseCode = "201", description = "Created")
    public Response create(@Valid @RequestBody(description="Create book input", required=true,
            content=@Content(schema=@Schema(implementation=CreateBookRequestDTO.class))) CreateBookRequestDTO in){
        UUID id = service.create(in);
        return Response.status(Response.Status.CREATED)
               .entity("{\"id\":\"" + id + "\"}")
               .build();

    }

    @PUT @Path("{id}")
    @Operation(summary="Update book")
    public Response update(@Parameter(description="Book ID") @PathParam("id") UUID id,
                           @Valid @RequestBody(description="Update book input",
                           content=@Content(schema=@Schema(implementation=UpdateBookRequestDTO.class))) UpdateBookRequestDTO in){
        service.update(id, in); return Response.ok().build();
    }

    @DELETE @Path("{id}")
    @Operation(summary="Delete book")
    public Response delete(@Parameter(description="Book ID") @PathParam("id") UUID id){
        service.delete(id); return Response.noContent().build();
    }

    @GET @Path("{id}")
    @Operation(summary="Get book")
    public Response get(@Parameter(description="Book ID") @PathParam("id") UUID id){
        BookDTO d = service.get(id);
        if (d == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(d).build();
    }

    @GET
    @Operation(summary="List books")
    public List<BookDTO> list(@QueryParam("offset") @DefaultValue("0") int offset,
                              @QueryParam("limit") @DefaultValue("50") int limit){
        return service.list(offset, limit);
    }
}
