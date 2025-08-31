/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import com.shelfinity.queues.dto.requests.CreateQueueItemRequest;
import com.shelfinity.queues.dto.requests.UpdateQueueItemRequest;
import com.shelfinity.queues.dto.responses.QueueItemResponse;
import com.shelfinity.security.JwtUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API for queue management.
 */
@Path("/queues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class QueueResource {
    
    @Inject
    private QueueRepository queueRepository;
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Context
    private HttpHeaders headers;
    
    /**
     * Create a new queue item.
     */
    @POST
    public Response createQueueItem(@Valid CreateQueueItemRequest request) {
        // Verify user is authenticated
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        // Create new queue item
        QueueItem queueItem = new QueueItem(request.getType(), request.getUserKeycloakId(), 
                                          request.getBookId(), request.getDescription());
        
        QueueItem savedItem = queueRepository.save(queueItem);
        QueueItemResponse response = new QueueItemResponse(savedItem);
        
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }
    
    /**
     * Get all queue items (admin only).
     */
    @GET
    public Response getAllQueueItems(@QueryParam("status") String status,
                                   @QueryParam("type") String type) {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
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
     * Get pending queue items (admin only).
     */
    @GET
    @Path("/pending")
    public Response getPendingQueueItems() {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        List<QueueItem> items = queueRepository.findPending();
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
    public Response getQueueItemById(@PathParam("id") String id) {
        try {
            UUID itemId = UUID.fromString(id);
            Optional<QueueItem> item = queueRepository.findById(itemId);
            
            if (item.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Queue item not found\"}")
                        .build();
            }
            
            // Check if user is requesting their own item or is admin
            Optional<JwtUtil.UserInfo> currentUser = getCurrentUser();
            if (currentUser.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Authentication required\"}")
                        .build();
            }
            
            QueueItem requestedItem = item.get();
            if (!currentUser.get().getKeycloakId().equals(requestedItem.getUserKeycloakId()) && 
                !"ADMIN".equals(currentUser.get().getRole())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Access denied\"}")
                        .build();
            }
            
            QueueItemResponse response = new QueueItemResponse(requestedItem);
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid queue item ID format\"}")
                    .build();
        }
    }
    
    /**
     * Update queue item (admin only).
     */
    @PATCH
    @Path("/{id}")
    public Response updateQueueItem(@PathParam("id") String id, @Valid UpdateQueueItemRequest request) {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
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
            item.setAdminRemark(request.getAdminRemark());
            item.setProcessedAt(LocalDateTime.now());
            item.setProcessedBy(userInfo.get().getKeycloakId());
            
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
     * Delete queue item (admin only).
     */
    @DELETE
    @Path("/{id}")
    public Response deleteQueueItem(@PathParam("id") String id) {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
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
     * Get queue statistics (admin only).
     */
    @GET
    @Path("/stats")
    public Response getQueueStats() {
        // Verify admin access
        Optional<JwtUtil.UserInfo> userInfo = getCurrentUser();
        if (userInfo.isEmpty() || !"ADMIN".equals(userInfo.get().getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        long pendingCount = queueRepository.countPending();
        long userRegistrations = queueRepository.countByType(QueueType.USER_REGISTRATION);
        long bookBorrows = queueRepository.countByType(QueueType.BOOK_BORROW);
        long bookReturns = queueRepository.countByType(QueueType.BOOK_RETURN);
        long bookReservations = queueRepository.countByType(QueueType.BOOK_RESERVATION);
        
        String stats = String.format(
            "{\"pendingCount\": %d, \"userRegistrations\": %d, \"bookBorrows\": %d, \"bookReturns\": %d, \"bookReservations\": %d}",
            pendingCount, userRegistrations, bookBorrows, bookReturns, bookReservations
        );
        
        return Response.ok(stats).build();
    }
    
    /**
     * Get current user from JWT token.
     */
    private Optional<JwtUtil.UserInfo> getCurrentUser() {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return Optional.empty();
        }
        
        Optional<String> token = jwtUtil.extractTokenFromHeader(authHeader);
        if (token.isEmpty()) {
            return Optional.empty();
        }
        
        return jwtUtil.extractUserInfo(token.get());
    }
}
