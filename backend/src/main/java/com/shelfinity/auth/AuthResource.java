/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.auth;

import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;
import com.shelfinity.users.dto.responses.UserResponse;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for authentication operations.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Authentication")
public class AuthResource {
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private UserRepository userRepository;
    
    /**
     * Login endpoint - validates JWT token and returns user info.
     * The actual authentication is done by Keycloak, this endpoint
     * just validates the token and returns user information.
     */
    @POST
    @Path("/login")
    @Tag(name = "Authentication")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Login with JWT token",
        description = "Validates the JWT token from Keycloak and returns user information"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Authentication required\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found in system",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"User not found\"}")
            )
        )
    })
    public Response login() {
        // Get user info from JWT token
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        // Find user in database by Keycloak ID
        Optional<User> user = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (user.isEmpty()) {
            // User authenticated in Keycloak but not in our system
            // This could happen for new users
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found in system. Please contact administrator.\"}")
                    .build();
        }
        
        UserResponse response = new UserResponse(user.get());
        return Response.ok(response).build();
    }
    
    /**
     * Validate token endpoint - checks if the current JWT token is valid.
     */
    @GET
    @Path("/validate")
    @Tag(name = "Authentication")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Validate JWT token",
        description = "Validates the current JWT token and returns user information if valid"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Token is valid",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token is invalid or expired",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Invalid or expired token\"}")
            )
        )
    })
    public Response validateToken() {
        // Check if user is authenticated
        if (!jwtUtil.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid or expired token\"}")
                    .build();
        }
        
        // Get user info from JWT token
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid or expired token\"}")
                    .build();
        }
        
        // Find user in database
        Optional<User> user = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found in system\"}")
                    .build();
        }
        
        UserResponse response = new UserResponse(user.get());
        return Response.ok(response).build();
    }
    
    /**
     * Get current user profile.
     */
    @GET
    @Path("/me")
    @Tag(name = "Authentication")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the profile of the currently authenticated user"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
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
    public Response getCurrentUser() {
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        Optional<User> user = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found\"}")
                    .build();
        }
        
        UserResponse response = new UserResponse(user.get());
        return Response.ok(response).build();
    }
}
