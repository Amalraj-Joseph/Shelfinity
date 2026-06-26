/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.overdue;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.overdue.OverdueService.OverdueStats;
import com.shelfinity.queues.QueueItem;
import com.shelfinity.security.JwtUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for overdue book tracking.
 */
@Path("/overdue")
@Tag(name = "Overdue", description = "Overdue book tracking operations")
public class OverdueResource {
    
    private static final Logger LOGGER = Logger.getLogger(OverdueResource.class.getName());
    
    @Inject
    private OverdueService overdueService;
    
    @Inject
    private JwtUtil jwtUtil;
    
    /**
     * Get all overdue items (admin only).
     */
    @GET
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all overdue items", description = "Retrieve all currently overdue books (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of overdue items retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItem.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllOverdueItems() {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            List<QueueItem> overdueItems = overdueService.getOverdueItems();
            return Response.ok(overdueItems).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error retrieving overdue items: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve overdue items\"}")
                .build();
        }
    }
    
    /**
     * Get overdue items for the current user.
     */
    @GET
    @Path("/my")
    @RolesAllowed({"admin", "user"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get my overdue items", description = "Retrieve overdue books for the authenticated user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User's overdue items retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = QueueItem.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getMyOverdueItems() {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            String userKeycloakId = jwtUtil.getCurrentUserKeycloakId()
                .orElseThrow(() -> new RuntimeException("User Keycloak ID not found"));
            
            List<QueueItem> overdueItems = overdueService.getOverdueItemsForUser(userKeycloakId);
            return Response.ok(overdueItems).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error retrieving user's overdue items: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve overdue items\"}")
                .build();
        }
    }
    
    /**
     * Get overdue statistics (admin only).
     */
    @GET
    @Path("/stats")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get overdue statistics", description = "Retrieve statistics about overdue books (admin only)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Overdue statistics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = OverdueStats.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @APIResponse(responseCode = "403", description = "Forbidden - Admin access required"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getOverdueStats() {
        try {
            if (!jwtUtil.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Not authenticated\"}")
                    .build();
            }
            
            OverdueStats stats = overdueService.getOverdueStats();
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error retrieving overdue statistics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve overdue statistics\"}")
                .build();
        }
    }
}

// Made with Bob
