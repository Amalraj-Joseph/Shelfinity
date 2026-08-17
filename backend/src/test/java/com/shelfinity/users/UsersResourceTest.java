/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.queues.QueueItem;
import com.shelfinity.queues.QueueRepository;
import com.shelfinity.queues.QueueType;
import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.dto.requests.CreateUserRequest;

import jakarta.ws.rs.core.Response;

/**
 * SPEC.md §10.1 (privilege escalation) and §10.3 (registration approval
 * wiring), both resolved — this locks in the fixed behavior of
 * {@link UsersResource#createUser}.
 */
@ExtendWith(MockitoExtension.class)
class UsersResourceTest {

    @Mock private UserRepository userRepository;
    @Mock private QueueRepository queueRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UsersResource usersResource;

    private static JwtUtil.UserInfo userInfo(String keycloakId) {
        JwtUtil.UserInfo info = new JwtUtil.UserInfo();
        info.setKeycloakId(keycloakId);
        return info;
    }

    private static CreateUserRequest request(String keycloakId, String email, String role) {
        CreateUserRequest req = new CreateUserRequest();
        req.setKeycloakId(keycloakId);
        req.setEmail(email);
        req.setName("Test User");
        req.setRole(role);
        return req;
    }

    @BeforeEach
    void stubNoExistingUser() {
        // lenient: a couple of tests (401, cross-account 403) return before this
        // lookup is ever reached, which is the correct/desired behavior, not a
        // test bug — Mockito's strict stubbing would otherwise fail those tests.
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void createUser_unauthenticatedCaller_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = usersResource.createUser(request("kc-1", "a@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(401);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_selfServiceForOwnAccount_createsInactiveUserWithRegistrationQueueItem() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = usersResource.createUser(request("kc-1", "a@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(201);
        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isActive()).isFalse();

        var queueCaptor = org.mockito.ArgumentCaptor.forClass(QueueItem.class);
        verify(queueRepository).save(queueCaptor.capture());
        assertThat(queueCaptor.getValue().getType()).isEqualTo(QueueType.USER_REGISTRATION);
        assertThat(queueCaptor.getValue().getUserKeycloakId()).isEqualTo("kc-1");
    }

    @Test
    void createUser_selfServiceCallerRequestingAdminRole_isForcedToUser() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // SPEC.md §10.1: a non-admin can never grant themselves ADMIN, even by
        // asking directly in the request body.
        usersResource.createUser(request("kc-1", "a@b.com", "ADMIN"));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void createUser_selfServiceForAnotherAccount_returns403AndDoesNotCreateAnything() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = usersResource.createUser(request("kc-someone-else", "a@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(userRepository, never()).save(any());
        verify(queueRepository, never()).save(any());
    }

    @Test
    void createUser_adminCreatingUser_activeImmediatelyWithNoQueueItem() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-admin")));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = usersResource.createUser(request("kc-new-employee", "new@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(201);
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
        verify(queueRepository, never()).save(any());
    }

    @Test
    void createUser_adminGrantingAdminRole_isHonored() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-admin")));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        usersResource.createUser(request("kc-new-admin", "newadmin@b.com", "ADMIN"));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void createUser_emailAlreadyExists_returns409() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);
        when(userRepository.findByEmail("taken@b.com"))
                .thenReturn(Optional.of(new User("kc-existing", "taken@b.com", "Existing")));

        Response response = usersResource.createUser(request("kc-1", "taken@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(409);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = usersResource.getAllUsers();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void getAllUsers_admin_returnsList() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.findAll()).thenReturn(List.of(new User("kc-1", "a@b.com", "Alice")));

        Response response = usersResource.getAllUsers();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getUserById_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Response response = usersResource.getUserById(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void getUserById_invalidUuid_returns400() {
        Response response = usersResource.getUserById("not-a-uuid");

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void getUserById_found_returns200() {
        UUID id = UUID.randomUUID();
        User user = new User("kc-1", "a@b.com", "Alice");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Response response = usersResource.getUserById(id.toString());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getCurrentUserProfile_notAuthenticated_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = usersResource.getCurrentUserProfile();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getCurrentUserProfile_noLocalRecord_returns404() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        Response response = usersResource.getCurrentUserProfile();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void getCurrentUserProfile_found_returns200() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1"))
                .thenReturn(Optional.of(new User("kc-1", "a@b.com", "Alice")));

        Response response = usersResource.getCurrentUserProfile();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void updateUser_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = usersResource.updateUser(UUID.randomUUID().toString(), request("kc-1", "a@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(403);
        verify(userRepository, never()).update(any());
    }

    @Test
    void updateUser_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Response response = usersResource.updateUser(id.toString(), request("kc-1", "a@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void updateUser_admin_reassignsRole() {
        UUID id = UUID.randomUUID();
        User existing = new User("kc-1", "a@b.com", "Alice");
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Response response = usersResource.updateUser(id.toString(), request("kc-1", "a@b.com", "ADMIN"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(existing.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateUser_invalidUuid_returns400() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);

        Response response = usersResource.updateUser("not-a-uuid", request("kc-1", "a@b.com", "USER"));

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void deleteUser_nonAdmin_returns403() {
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(false);

        Response response = usersResource.deleteUser(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(403);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Response response = usersResource.deleteUser(id.toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void deleteUser_admin_deletesAndReturns204() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.isCurrentUserAdmin()).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(new User("kc-1", "a@b.com", "Alice")));

        Response response = usersResource.deleteUser(id.toString());

        assertThat(response.getStatus()).isEqualTo(204);
        verify(userRepository).deleteById(id);
    }
}
