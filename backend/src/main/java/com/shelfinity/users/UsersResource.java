/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users;

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

import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.dto.requests.CreateUserRequest;
import com.shelfinity.users.dto.responses.UserResponse;

import jakarta.annotation.security.RolesAllowed;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * REST API for user management.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Users")
public class UsersResource {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Context
    private SecurityContext securityContext;
    
    /**
     * Create a new user.
     */
    @POST
    @Tag(name = "Users")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user account in the system. Typically called after user registration in Keycloak."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "409",
            description = "User already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"User already exists\"}")
            )
        )
    })
    public Response createUser(
        @RequestBody(
            description = "User information to create",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CreateUserRequest.class),
                examples = @ExampleObject(
                    name = "Create User Example",
                    value = """
                    {
                        "keycloakId": "123e4567-e89b-12d3-a456-426614174000",
                        "email": "user@example.com",
                        "name": "John Doe",
                        "role": "USER"
                    }
                    """
                )
            )
        ) @Valid CreateUserRequest request) {
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"User already exists\"}")
                    .build();
        }
        
        // Create new user
        User user = new User();
        user.setKeycloakId(request.getKeycloakId());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole("ADMIN".equalsIgnoreCase(request.getRole()) ? UserRole.ADMIN : UserRole.USER);
        
        User createdUser = userRepository.save(user);
        UserResponse response = new UserResponse(createdUser);
        
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
    
    /**
     * Get all users (admin only).
     */
    @GET
    @Tag(name = "Users")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all users in the system. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
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
    public Response getAllUsers() {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Get user by ID.
     */
    @GET
    @Path("/{id}")
    @Tag(name = "Users")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their ID."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"User not found\"}")
            )
        )
    })
    public Response getUserById(@PathParam("id") String id) {
        try {
            UUID userId = UUID.fromString(id);
            Optional<User> user = userRepository.findById(userId);
            
            if (user.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }
            
            UserResponse response = new UserResponse(user.get());
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid user ID format\"}")
                    .build();
        }
    }
    
    /**
     * Get current user profile.
     */
    @GET
    @Path("/me")
    @Tag(name = "Users")
    @SecurityRequirement(name = "JWT")
    @RolesAllowed("user")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile of the currently authenticated user."
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
    public Response getCurrentUserProfile() {
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
    
    /**
     * Update user by ID (admin only).
     */
    @PUT
    @Path("/{id}")
    @Tag(name = "Users")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Update user by ID",
        description = "Updates a specific user by their ID. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class)
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
            description = "User not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"User not found\"}")
            )
        )
    })
    public Response updateUser(@PathParam("id") String id, @Valid CreateUserRequest request) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID userId = UUID.fromString(id);
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }
            
            User user = userOpt.get();
            user.setEmail(request.getEmail());
            user.setName(request.getName());
            user.setRole("ADMIN".equalsIgnoreCase(request.getRole()) ? UserRole.ADMIN : UserRole.USER);
            
            User updatedUser = userRepository.update(user);
            UserResponse response = new UserResponse(updatedUser);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid user ID format\"}")
                    .build();
        }
    }
    
    /**
     * Delete user by ID (admin only).
     */
    @DELETE
    @Path("/{id}")
    @Tag(name = "Users")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Delete user by ID",
        description = "Deletes a specific user by their ID. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "User deleted successfully"
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
            description = "User not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"User not found\"}")
            )
        )
    })
    public Response deleteUser(@PathParam("id") String id) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID userId = UUID.fromString(id);
            Optional<User> user = userRepository.findById(userId);
            
            if (user.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                    .build();
            }
            
            userRepository.deleteById(userId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid user ID format\"}")
                    .build();
        }
    }
}
