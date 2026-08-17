/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.email.EmailService;
import com.shelfinity.reservations.dto.CreateReservationRequest;
import com.shelfinity.reservations.dto.ReservationResponse;
import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for book reservation management.
 */
@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Tag(name = "Reservations")
public class ReservationResource {
    
    @Inject
    private ReservationRepository reservationRepository;
    
    @Inject
    private BookRepository bookRepository;
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private EmailService emailService;
    
    @Inject
    private JwtUtil jwtUtil;

    @Inject
    @ConfigProperty(name = "reservation.expiry.days", defaultValue = "7")
    private int reservationExpiryDays;

    // Resolves the user relation so responses carry a human-readable name/email
    // alongside the raw UUID, matching the book title/author already resolved
    // by the ReservationResponse(Reservation, Book) constructor.
    private ReservationResponse toResponse(Reservation reservation, Book book) {
        User user = userRepository.findByKeycloakId(reservation.getUserKeycloakId()).orElse(null);
        return new ReservationResponse(reservation, book, user);
    }

    /**
     * Create a new reservation.
     */
    @POST
    @Tag(name = "Reservations")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Create book reservation",
        description = "Creates a reservation for an unavailable book."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Reservation created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ReservationResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Book is available or already reserved",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Book is currently available\"}")
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
    public Response createReservation(@Valid CreateReservationRequest request) {
        // Get current user
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }

        // SPEC.md §6.1 step 3: block transacting until registration is approved.
        Optional<User> requester = userRepository.findByKeycloakId(userInfo.get().getKeycloakId());
        if (requester.isEmpty() || !requester.get().isActive()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Account pending approval\"}")
                    .build();
        }

        // Check if book exists
        Optional<Book> book = bookRepository.findById(request.getBookId());
        if (book.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Book not found\"}")
                    .build();
        }
        
        // Check if book is available
        if (book.get().getAvailableCopies() > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Book is currently available. Please submit a borrow request instead.\"}")
                    .build();
        }
        
        // Check if user already has an active reservation for this book
        if (reservationRepository.hasActiveReservation(userInfo.get().getKeycloakId(), request.getBookId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"You already have an active reservation for this book\"}")
                    .build();
        }
        
        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setUserKeycloakId(userInfo.get().getKeycloakId());
        reservation.setBookId(request.getBookId());
        reservation.setNotes(request.getNotes());
        // SPEC.md §10.5 (resolved): expiry is set here, not hardcoded in the entity.
        reservation.setExpiresAt(LocalDateTime.now().plusDays(reservationExpiryDays));

        Reservation savedReservation = reservationRepository.save(reservation);

        // Send confirmation email (requester is guaranteed present — checked above)
        emailService.sendReservationConfirmation(
            requester.get().getEmail(),
            requester.get().getName(),
            book.get().getTitle()
        );
        
        ReservationResponse response = toResponse(savedReservation, book.get());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
    
    /**
     * Get all reservations (admin only).
     */
    @GET
    @Tag(name = "Reservations")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get all reservations",
        description = "Retrieves all reservations. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Reservations retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ReservationResponse.class)
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
    public Response getAllReservations() {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationResponse> responses = reservations.stream()
                .map(r -> toResponse(r, bookRepository.findById(r.getBookId()).orElse(null)))
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Get current user's reservations.
     */
    @GET
    @Path("/my")
    @Tag(name = "Reservations")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Get my reservations",
        description = "Retrieves reservations for the current user."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Reservations retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ReservationResponse.class)
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
    public Response getMyReservations() {
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        List<Reservation> reservations = reservationRepository.findByUserKeycloakId(userInfo.get().getKeycloakId());
        List<ReservationResponse> responses = reservations.stream()
                .map(r -> toResponse(r, bookRepository.findById(r.getBookId()).orElse(null)))
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    /**
     * Cancel a reservation.
     */
    @DELETE
    @Path("/{id}")
    @Tag(name = "Reservations")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Cancel reservation",
        description = "Cancels a reservation. Users can only cancel their own reservations."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Reservation cancelled successfully"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Not authorized to cancel this reservation",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Not authorized\"}")
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Reservation not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Reservation not found\"}")
            )
        )
    })
    public Response cancelReservation(@PathParam("id") String id) {
        Optional<JwtUtil.UserInfo> userInfo = jwtUtil.getCurrentUserInfo();
        if (userInfo.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Authentication required\"}")
                    .build();
        }
        
        try {
            UUID reservationId = UUID.fromString(id);
            Optional<Reservation> reservation = reservationRepository.findById(reservationId);
            
            if (reservation.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Reservation not found\"}")
                        .build();
            }
            
            // Check if user owns this reservation or is admin
            if (!reservation.get().getUserKeycloakId().equals(userInfo.get().getKeycloakId()) 
                && !jwtUtil.isCurrentUserAdmin()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Not authorized to cancel this reservation\"}")
                        .build();
            }
            
            reservationRepository.cancel(reservationId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid reservation ID format\"}")
                    .build();
        }
    }
    
    /**
     * Mark reservation as fulfilled (admin only).
     */
    @POST
    @Path("/{id}/fulfill")
    @Tag(name = "Reservations")
    @SecurityRequirement(name = "JWT")
    @Operation(
        summary = "Fulfill reservation",
        description = "Marks a reservation as fulfilled. Admin access required."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Reservation fulfilled successfully"
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
            description = "Reservation not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(value = "{\"error\": \"Reservation not found\"}")
            )
        )
    })
    public Response fulfillReservation(@PathParam("id") String id) {
        // Verify admin access
        if (!jwtUtil.isCurrentUserAdmin()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin access required\"}")
                    .build();
        }
        
        try {
            UUID reservationId = UUID.fromString(id);
            Optional<Reservation> reservation = reservationRepository.findById(reservationId);
            
            if (reservation.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Reservation not found\"}")
                        .build();
            }
            
            reservationRepository.markAsFulfilled(reservationId);
            return Response.ok("{\"message\": \"Reservation fulfilled successfully\"}").build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid reservation ID format\"}")
                    .build();
        }
    }
}
