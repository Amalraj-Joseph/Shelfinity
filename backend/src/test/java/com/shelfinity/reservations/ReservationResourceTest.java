/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.Book;
import com.shelfinity.books.BookRepository;
import com.shelfinity.email.EmailService;
import com.shelfinity.reservations.dto.CreateReservationRequest;
import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.ws.rs.core.Response;

/**
 * SPEC.md §6.1 step 3 (approval gate) and §10.5 (resolved — configurable
 * expiry, no longer hardcoded in the entity).
 */
@ExtendWith(MockitoExtension.class)
class ReservationResourceTest {

    private static final int EXPIRY_DAYS = 7;

    @Mock private ReservationRepository reservationRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private ReservationResource reservationResource;

    @BeforeEach
    void setExpiryConfig() throws Exception {
        Field field = ReservationResource.class.getDeclaredField("reservationExpiryDays");
        field.setAccessible(true);
        field.set(reservationResource, EXPIRY_DAYS);
    }

    private static JwtUtil.UserInfo userInfo(String keycloakId) {
        JwtUtil.UserInfo info = new JwtUtil.UserInfo();
        info.setKeycloakId(keycloakId);
        return info;
    }

    @Test
    void createReservation_unauthenticated_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = reservationResource.createReservation(new CreateReservationRequest(UUID.randomUUID(), null));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void createReservation_inactiveAccount_returns403() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User inactive = new User("kc-1", "a@b.com", "Alice");
        inactive.setActive(false);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(inactive));

        Response response = reservationResource.createReservation(new CreateReservationRequest(UUID.randomUUID(), null));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_bookCurrentlyAvailable_returns400() {
        UUID bookId = UUID.randomUUID();
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User active = new User("kc-1", "a@b.com", "Alice");
        active.setActive(true);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(active));
        Book available = new Book("Title", "Author");
        available.setId(bookId);
        available.setAvailableCopies(2);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(available));

        Response response = reservationResource.createReservation(new CreateReservationRequest(bookId, null));

        assertThat(response.getStatus()).isEqualTo(400);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_validRequest_setsConfigurableExpiry() {
        UUID bookId = UUID.randomUUID();
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User active = new User("kc-1", "a@b.com", "Alice");
        active.setActive(true);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(active));
        Book unavailable = new Book("Title", "Author");
        unavailable.setId(bookId);
        unavailable.setAvailableCopies(0);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(unavailable));
        when(reservationRepository.hasActiveReservation("kc-1", bookId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = reservationResource.createReservation(new CreateReservationRequest(bookId, "please"));

        assertThat(response.getStatus()).isEqualTo(201);
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getExpiresAt())
                .isAfter(LocalDateTime.now().plusDays(EXPIRY_DAYS - 1))
                .isBefore(LocalDateTime.now().plusDays(EXPIRY_DAYS + 1));
        verify(emailService).sendReservationConfirmation("a@b.com", "Alice", "Title");
    }

    @Test
    void createReservation_alreadyHasActiveReservation_returns409() {
        UUID bookId = UUID.randomUUID();
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        User active = new User("kc-1", "a@b.com", "Alice");
        active.setActive(true);
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(active));
        Book unavailable = new Book("Title", "Author");
        unavailable.setId(bookId);
        unavailable.setAvailableCopies(0);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(unavailable));
        when(reservationRepository.hasActiveReservation("kc-1", bookId)).thenReturn(true);

        Response response = reservationResource.createReservation(new CreateReservationRequest(bookId, null));

        assertThat(response.getStatus()).isEqualTo(409);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_ownerCanCancel() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setUserKeycloakId("kc-1");
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        Response response = reservationResource.cancelReservation(id.toString());

        assertThat(response.getStatus()).isEqualTo(204);
        verify(reservationRepository).cancel(id);
    }

    @Test
    void cancelReservation_nonOwnerNonAdmin_returns403() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setUserKeycloakId("kc-owner");
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-someone-else")));
        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = reservationResource.cancelReservation(id.toString());

        assertThat(response.getStatus()).isEqualTo(403);
        verify(reservationRepository, never()).cancel(any());
    }

    @Test
    void fulfillReservation_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = reservationResource.fulfillReservation(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(403);
        verify(reservationRepository, never()).markAsFulfilled(any());
    }

    @Test
    void fulfillReservation_admin_marksFulfilled() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(reservationRepository.findById(id)).thenReturn(Optional.of(new Reservation()));

        Response response = reservationResource.fulfillReservation(id.toString());

        assertThat(response.getStatus()).isEqualTo(200);
        verify(reservationRepository).markAsFulfilled(id);
    }

    @Test
    void getAllReservations_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = reservationResource.getAllReservations();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void getAllReservations_admin_returnsList() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(reservationRepository.findAll()).thenReturn(java.util.List.of());

        Response response = reservationResource.getAllReservations();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getMyReservations_notAuthenticated_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = reservationResource.getMyReservations();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getMyReservations_authenticated_scopesToCaller() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(reservationRepository.findByUserKeycloakId("kc-1")).thenReturn(java.util.List.of());

        Response response = reservationResource.getMyReservations();

        assertThat(response.getStatus()).isEqualTo(200);
        verify(reservationRepository).findByUserKeycloakId("kc-1");
    }

    @Test
    void cancelReservation_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        Response response = reservationResource.cancelReservation(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void cancelReservation_adminCanCancelAnyones() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setUserKeycloakId("kc-owner");
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-admin")));
        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);

        Response response = reservationResource.cancelReservation(id.toString());

        assertThat(response.getStatus()).isEqualTo(204);
        verify(reservationRepository).cancel(id);
    }

    @Test
    void fulfillReservation_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        Response response = reservationResource.fulfillReservation(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
