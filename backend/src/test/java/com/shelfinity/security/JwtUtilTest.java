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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * SPEC.md §4: Keycloak nests realm roles under `realm_access.roles`, not the
 * flat "groups" claim JsonWebToken#getGroups() reads — this is the parsing
 * logic that makes RBAC actually work, so it's covered directly rather than
 * only through the JAX-RS layer.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock private JsonWebToken jwt;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        Field field = JwtUtil.class.getDeclaredField("jwt");
        field.setAccessible(true);
        field.set(jwtUtil, jwt);
    }

    private void setNullJwt() throws Exception {
        Field field = JwtUtil.class.getDeclaredField("jwt");
        field.setAccessible(true);
        field.set(jwtUtil, null);
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
    void getCurrentUserRole_findsAdminInJsonObjectRealmAccess() {
        JsonObject realmAccess = Json.createObjectBuilder()
                .add("roles", Json.createArrayBuilder().add("offline_access").add("admin").build())
                .build();
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);

        assertThat(jwtUtil.getCurrentUserRole()).contains("admin");
        assertThat(jwtUtil.isCurrentUserAdmin()).isTrue();
    }

    @Test
    void getCurrentUserRole_findsUserInMapRealmAccess() {
        Map<String, Object> realmAccess = Map.of("roles", List.of("uma_authorization", "user"));
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);

        assertThat(jwtUtil.getCurrentUserRole()).contains("user");
        assertThat(jwtUtil.isCurrentUserAdmin()).isFalse();
        assertThat(jwtUtil.hasRole("user")).isTrue();
    }

    @Test
    void getCurrentUserRole_ignoresRolesOutsideKnownApplicationRoles() {
        JsonObject realmAccess = Json.createObjectBuilder()
                .add("roles", Json.createArrayBuilder().add("offline_access").add("uma_authorization").build())
                .build();
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);

        assertThat(jwtUtil.getCurrentUserRole()).isEmpty();
    }

    @Test
    void getCurrentUserRole_emptyWhenNoRealmAccessClaim() {
        when(jwt.getClaim("realm_access")).thenReturn(null);

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
        JsonObject realmAccess = Json.createObjectBuilder()
                .add("roles", Json.createArrayBuilder().add("user").build())
                .build();
        lenient().when(jwt.getClaim("realm_access")).thenReturn(realmAccess);

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
