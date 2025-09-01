/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Health check endpoint for monitoring and Docker health checks.
 */
@Path("/health")
@Tag(name = "Health")
public class HealthResource {
    
    public HealthResource() {
        System.out.println("HealthResource constructor called");
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get system health status",
        description = "Returns the current health status of the Shelfinity backend service, including uptime and version information."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "System is healthy and operational",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    name = "Health Status",
                    value = """
                    {
                        "status": "UP",
                        "timestamp": "2025-01-09T14:30:45.123",
                        "service": "Shelfinity Backend",
                        "version": "1.0.0"
                    }
                    """
                )
            )
        )
    })
    public Response getHealth() {
        System.out.println("HealthResource.getHealth() called");
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("service", "Shelfinity Backend");
        health.put("version", "1.0.0");
        
        return Response.ok(health).build();
    }
}
