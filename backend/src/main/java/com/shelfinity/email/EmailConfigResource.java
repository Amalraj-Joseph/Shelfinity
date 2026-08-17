/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.shelfinity.email.dto.EmailConfigResponse;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.security.JwtUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for email configuration management.
 */
@Path("/email/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Email Configuration")
public class EmailConfigResource {
    
    @Inject
    private EmailConfigRepository emailConfigRepository;
    
    @Inject
    private EmailService emailService;
    
    @Inject
    private JwtUtil jwtUtil;
    
    /**
     * Create or update email configuration (admin only).
     */
    @POST
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Create or update email configuration",
        description = "Creates or updates the email server configuration. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Email configuration saved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailConfigResponse.class)
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
    public Response saveEmailConfig(@Valid EmailConfig config) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        EmailConfig savedConfig = emailConfigRepository.save(config);

        // Refresh email service with new configuration
        emailService.refreshMailSession();

        return Response.ok(new EmailConfigResponse(savedConfig)).build();
    }
    
    /**
     * Get active email configuration (admin only).
     */
    @GET
    @Path("/active")
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get active email configuration",
        description = "Retrieves the currently active email configuration. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Active configuration retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailConfigResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "No active configuration found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"No active email configuration found\"}")
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
    public Response getActiveConfig() {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        Optional<EmailConfig> config = emailConfigRepository.findActiveConfig();
        if (config.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"No active email configuration found\"}")
                    .build();
        }
        
        return Response.ok(new EmailConfigResponse(config.get())).build();
    }
    
    /**
     * Get all email configurations (admin only).
     */
    @GET
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get all email configurations",
        description = "Retrieves all email configurations. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Configurations retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailConfigResponse.class)
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
    public Response getAllConfigs() {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        List<EmailConfig> configs = emailConfigRepository.findAll();
        List<EmailConfigResponse> responses = configs.stream()
                .map(EmailConfigResponse::new)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }
    
    /**
     * Update email configuration (admin only).
     */
    @PUT
    @Path("/{id}")
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Update email configuration",
        description = "Updates an existing email configuration. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Configuration updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailConfigResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Configuration not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Email configuration not found\"}")
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
    public Response updateConfig(@PathParam("id") String id, @Valid EmailConfig config) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID configId = UUID.fromString(id);
            Optional<EmailConfig> existingConfig = emailConfigRepository.findById(configId);
            
            if (existingConfig.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Email configuration not found\"}")
                        .build();
            }
            
            config.setId(configId);
            // The password is never echoed back by any GET (EmailConfigResponse
            // omits it), so a typical edit-then-submit flow arrives with no
            // password field set. Treat that as "leave unchanged", not "clear it".
            if (config.getPassword() == null || config.getPassword().isEmpty()) {
                config.setPassword(existingConfig.get().getPassword());
            }
            EmailConfig updatedConfig = emailConfigRepository.update(config);
            
            // Refresh email service if this is the active configuration
            if (updatedConfig.isActive()) {
                emailService.refreshMailSession();
            }
            
            return Response.ok(new EmailConfigResponse(updatedConfig)).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid configuration ID format\"}")
                    .build();
        }
    }
    
    /**
     * Activate a specific email configuration (admin only).
     */
    @POST
    @Path("/{id}/activate")
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Activate email configuration",
        description = "Activates a specific email configuration. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Configuration activated successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Configuration not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Email configuration not found\"}")
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
    public Response activateConfig(@PathParam("id") String id) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID configId = UUID.fromString(id);
            Optional<EmailConfig> config = emailConfigRepository.findById(configId);
            
            if (config.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Email configuration not found\"}")
                        .build();
            }
            
            emailConfigRepository.activate(configId);
            emailService.refreshMailSession();
            
            return Response.ok("{\"message\": \"Email configuration activated successfully\"}").build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid configuration ID format\"}")
                    .build();
        }
    }
    
    /**
     * Delete email configuration (admin only).
     */
    @DELETE
    @Path("/{id}")
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Delete email configuration",
        description = "Deletes an email configuration. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Configuration deleted successfully"
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
    public Response deleteConfig(@PathParam("id") String id) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID configId = UUID.fromString(id);
            emailConfigRepository.deleteById(configId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid configuration ID format\"}")
                    .build();
        }
    }
    
    /**
     * Test email configuration by sending a test email (admin only).
     */
    @POST
    @Path("/test")
    @Tag(name = "Email Configuration")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Test email configuration",
        description = "Sends a test email to verify the configuration. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Test email sent successfully"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Failed to send test email",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Failed to send test email\"}")
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
    public Response testEmailConfig(TestEmailRequest request) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        boolean success = emailService.sendEmail(
            request.getTo(),
            "Shelfinity Email Configuration Test",
            "This is a test email from Shelfinity Library Management System. " +
            "If you received this email, your email configuration is working correctly."
        );
        
        if (success) {
            return Response.ok("{\"message\": \"Test email sent successfully\"}").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to send test email. Please check your configuration.\"}")
                    .build();
        }
    }
    
    /**
     * Request DTO for testing email configuration.
     */
    public static class TestEmailRequest {
        private String to;
        
        public String getTo() {
            return to;
        }
        
        public void setTo(String to) {
            this.to = to;
        }
    }
}
