/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;
import com.shelfinity.users.UserRole;

/**
 * Role/admin checks are a DB lookup (User.role, keyed by the JWT subject)
 * rather than a claim parsed off the token — see JwtUtil.getCurrentUserRole's
 * Javadoc for why (Keycloak and IBM Cloud App ID don't share a token
 * role-claim shape, so the DB is the one source of truth both providers
 * agree on). Covered directly here rather than only through the JAX-RS
 * layer.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock private JsonWebToken jwt;
    @Mock private UserRepository userRepository;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField("jwt", jwt);
        setField("userRepository", userRepository);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = JwtUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtUtil, value);
    }

    private void setNullJwt() throws Exception {
        setField("jwt", null);
    }

    @Test
    void isAuthenticated_trueWhenJwtAndSubjectPresent() {
        when(jwt.getSubject()).thenReturn("kc-subject-1");

        assertThat(jwtUtil.isAuthenticated()).isTrue();
    }

    @Test
    void isAuthenticated_falseWhenJwtIsNull() throws Exception {
        setNullJwt();

        assertThat(jwtUtil.isAuthenticated()).isFalse();
    }

    @Test
    void isAuthenticated_falseWhenSubjectIsNull() {
        when(jwt.getSubject()).thenReturn(null);

        assertThat(jwtUtil.isAuthenticated()).isFalse();
    }

    @Test
    void getCurrentUserEmail_returnsEmailClaim() {
        when(jwt.getClaim(Claims.email)).thenReturn("alice@shelfinity.com");

        assertThat(jwtUtil.getCurrentUserEmail()).contains("alice@shelfinity.com");
    }

    @Test
    void getCurrentUserKeycloakId_returnsSubject() {
        when(jwt.getSubject()).thenReturn("kc-subject-1");

        assertThat(jwtUtil.getCurrentUserKeycloakId()).contains("kc-subject-1");
    }

    @Test
    void getCurrentUserRole_returnsAdminForAdminDbRow() {
        when(jwt.getSubject()).thenReturn("kc-subject-1");
        when(userRepository.findByKeycloakId("kc-subject-1"))
                .thenReturn(Optional.of(new User("kc-subject-1", "a@b.com", "Admin", UserRole.ADMIN)));

        assertThat(jwtUtil.getCurrentUserRole()).contains("admin");
        assertThat(jwtUtil.isCurrentUserAdmin()).isTrue();
    }

    @Test
    void getCurrentUserRole_returnsUserForUserDbRow() {
        when(jwt.getSubject()).thenReturn("kc-subject-1");
        when(userRepository.findByKeycloakId("kc-subject-1"))
                .thenReturn(Optional.of(new User("kc-subject-1", "a@b.com", "Alice", UserRole.USER)));

        assertThat(jwtUtil.getCurrentUserRole()).contains("user");
        assertThat(jwtUtil.isCurrentUserAdmin()).isFalse();
        assertThat(jwtUtil.hasRole("user")).isTrue();
    }

    @Test
    void getCurrentUserRole_emptyWhenNoMatchingDbRow() {
        when(jwt.getSubject()).thenReturn("kc-not-synced-yet");
        when(userRepository.findByKeycloakId("kc-not-synced-yet")).thenReturn(Optional.empty());

        assertThat(jwtUtil.getCurrentUserRole()).isEmpty();
    }

    @Test
    void getCurrentUserRole_emptyWhenJwtIsNull() throws Exception {
        setNullJwt();

        assertThat(jwtUtil.getCurrentUserRole()).isEmpty();
    }

    @Test
    void getCurrentUserInfo_buildsUserInfoWhenAuthenticated() {
        when(jwt.getSubject()).thenReturn("kc-subject-1");
        lenient().when(jwt.getClaim(Claims.email)).thenReturn("alice@shelfinity.com");
        lenient().when(userRepository.findByKeycloakId("kc-subject-1"))
                .thenReturn(Optional.of(new User("kc-subject-1", "alice@shelfinity.com", "Alice", UserRole.USER)));

        Optional<JwtUtil.UserInfo> info = jwtUtil.getCurrentUserInfo();

        assertThat(info).isPresent();
        assertThat(info.get().getKeycloakId()).isEqualTo("kc-subject-1");
        assertThat(info.get().getEmail()).isEqualTo("alice@shelfinity.com");
        assertThat(info.get().getRole()).isEqualTo("user");
    }

    @Test
    void getCurrentUserInfo_emptyWhenNotAuthenticated() throws Exception {
        setNullJwt();

        assertThat(jwtUtil.getCurrentUserInfo()).isEmpty();
    }
}
