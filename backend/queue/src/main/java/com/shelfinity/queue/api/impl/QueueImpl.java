package com.shelfinity.queue.api.impl;

import com.shelfinity.queue.dto.requests.UpdateQueueItemRequestDTO;
import com.shelfinity.queue.dto.responses.QueueItemDTO;
import com.shelfinity.queue.service.QueueService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/admin/queue")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name="Admin Queue", description="Admin review queue for registration & reservation")
public class QueueImpl {
    @Inject QueueService service;

    @GET
    @Operation(summary="List queue items", description="Filter by type and/or status. Ordered by createdAt ASC.")
    public List<QueueItemDTO> list(@QueryParam("type") @DefaultValue("") String type,
                                   @QueryParam("status") @DefaultValue("") String status,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("50") int limit){
        String t = type.isBlank()? null : type;
        String s = status.isBlank()? null : status;
        return service.list(t, s, offset, limit);
    }

    @GET @Path("{id}")
    @Operation(summary="Get one queue item")
    public Response get(@Parameter(description="Queue item ID") @PathParam("id") UUID id){
        QueueItemDTO d = service.get(id);
        if (d == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(d).build();
    }

    @PATCH @Path("{id}")
    @Operation(summary="Patch queue item",
               description="Update status and/or remark on the queue item and propagate to the underlying table via SQL.")
    @APIResponse(responseCode="204", description="Updated")
    public Response patch(@Parameter(description="Queue item ID") @PathParam("id") UUID id,
                          @Valid @RequestBody(
                              description="Patch payload", required=true,
                              content=@Content(schema=@Schema(implementation=UpdateQueueItemRequestDTO.class)))
                          UpdateQueueItemRequestDTO in){
        service.patch(id, in);
        return Response.noContent().build();
    }

    @DELETE @Path("{id}")
    @Operation(summary="Delete queue item")
    public Response delete(@Parameter(description="Queue item ID") @PathParam("id") UUID id){
        service.delete(id); return Response.noContent().build();
    }
}