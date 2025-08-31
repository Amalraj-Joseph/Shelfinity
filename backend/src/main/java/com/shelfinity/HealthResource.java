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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Health check endpoint for monitoring and Docker health checks.
 */
@Path("/health")
public class HealthResource {
    
    public HealthResource() {
        System.out.println("HealthResource constructor called");
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
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
