/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.queues.dto.requests.CreateQueueItemRequest;
import com.shelfinity.queues.dto.requests.UpdateQueueItemRequest;
import com.shelfinity.queues.dto.responses.QueueItemResponse;
import com.shelfinity.security.JwtUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for queue management.
 */
@Path("/queues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Queue")
public class QueueResource {
    
    @Inject
    private QueueRepository queueRepository;
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Context
    private SecurityContext securityContext;
    
    /**
     * Create a new queue item.
     */
    @POST
    @Tag(name = "Queue")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Create queue item",
        description = "Creates a new queue item for book borrowing or other requests."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Queue item created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItemResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Authentication required\"}")
            )
        )
    })
    public Response createQueueItem(@Valid CreateQueueItemRequest request) {
        // Verify user is authenticated
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        // Create new queue item
        QueueItem queueItem = new QueueItem();
        queueItem.setType(request.getType());
        queueItem.setUserKeycloakId(userInfo.get().getKeycloakId());
        queueItem.setBookId(request.getBookId());
        queueItem.setDescription(request.getDescription());
        queueItem.setStatus(QueueStatus.PENDING);
        
        QueueItem savedItem = queueRepository.save(queueItem);
        QueueItemResponse response = new QueueItemResponse(savedItem);
        
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
    
    /**
     * Get all queue items (admin only).
     */
    @GET
    @Tag(name = "Queue")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get all queue items",
        description = "Retrieves a list of all queue items. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Queue items retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItemResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        )
    })
    public Response getAllQueueItems(@QueryParam("status") String status,
                                   @QueryParam("type") String type) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        List<QueueItem> items;
        if (status != null && !status.trim().isEmpty()) {
            try {
                QueueStatus queueStatus = QueueStatus.valueOf(status.toUpperCase());
                items = queueRepository.findByStatus(queueStatus);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid status value\"}")
                        .build();
            }
        } else if (type != null && !type.trim().isEmpty()) {
            try {
                QueueType queueType = QueueType.valueOf(type.toUpperCase());
                items = queueRepository.findByType(queueType);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid type value\"}")
                        .build();
            }
        } else {
            items = queueRepository.findAll();
        }
        
        List<QueueItemResponse> responses = items.stream()
                .map(QueueItemResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Get queue item by ID.
     */
    @GET
    @Path("/{id}")
    @Tag(name = "Queue")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get queue item by ID",
        description = "Retrieves a specific queue item by its ID."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Queue item retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItemResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Queue item not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Queue item not found\"}")
            )
        )
    })
    public Response getQueueItemById(@PathParam("id") String id) {
        try {
            UUID itemId = UUID.fromString(id);
            Optional<QueueItem> item = queueRepository.findById(itemId);
            
            if (item.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Queue item not found\"}")
                        .build();
            }
            
            QueueItemResponse response = new QueueItemResponse(item.get());
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid queue item ID format\"}")
                    .build();
        }
    }
    
    /**
     * Update queue item status (admin only).
     */
    @PATCH
    @Path("/{id}/status")
    @Tag(name = "Queue")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Update queue item status",
        description = "Updates the status of a specific queue item. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Queue item status updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItemResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Queue item not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Queue item not found\"}")
            )
        )
    })
    public Response updateQueueItemStatus(@PathParam("id") String id, 
                                        @Valid UpdateQueueItemRequest request) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID itemId = UUID.fromString(id);
            Optional<QueueItem> itemOpt = queueRepository.findById(itemId);
            
            if (itemOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Queue item not found\"}")
                        .build();
            }
            
            QueueItem item = itemOpt.get();
            item.setStatus(request.getStatus());
            item.setProcessedAt(LocalDateTime.now());
            
            QueueItem updatedItem = queueRepository.update(item);
            QueueItemResponse response = new QueueItemResponse(updatedItem);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid queue item ID format\"}")
                    .build();
        }
    }
    
    /**
     * Delete queue item by ID (admin only).
     */
    @DELETE
    @Path("/{id}")
    @Tag(name = "Queue")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Delete queue item by ID",
        description = "Deletes a specific queue item by its ID. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Queue item deleted successfully"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Admin access required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Admin access required\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Queue item not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Queue item not found\"}")
            )
        )
    })
    public Response deleteQueueItem(@PathParam("id") String id) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID itemId = UUID.fromString(id);
            Optional<QueueItem> item = queueRepository.findById(itemId);
            
            if (item.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Queue item not found\"}")
                        .build();
            }
            
            queueRepository.deleteById(itemId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid queue item ID format\"}")
                    .build();
        }
    }
    
    /**
     * Get current user's queue items.
     */
    @GET
    @Path("/my")
    @Tag(name = "Queue")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get current user's queue items",
        description = "Retrieves queue items for the currently authenticated user."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User's queue items retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItemResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Authentication required\"}")
            )
        )
    })
    public Response getMyQueueItems() {
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        List<QueueItem> items = queueRepository.findByUserKeycloakId(userInfo.get().getKeycloakId());
        List<QueueItemResponse> responses = items.stream()
                .map(QueueItemResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
}
