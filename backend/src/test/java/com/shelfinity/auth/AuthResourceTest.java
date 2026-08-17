/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.security.JwtUtil;
import com.shelfinity.users.User;
import com.shelfinity.users.UserRepository;

import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AuthResource authResource;

    private static JwtUtil.UserInfo userInfo(String keycloakId) {
        JwtUtil.UserInfo info = new JwtUtil.UserInfo();
        info.setKeycloakId(keycloakId);
        return info;
    }

    @Test
    void login_noJwt_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = authResource.login();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void login_authenticatedButNoLocalRecord_returns404() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        Response response = authResource.login();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void login_validUser_returns200() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1"))
                .thenReturn(Optional.of(new User("kc-1", "a@b.com", "Alice")));

        Response response = authResource.login();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void validateToken_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = authResource.validateToken();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getCurrentUser_notAuthenticated_returns401() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.empty());

        Response response = authResource.getCurrentUser();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getCurrentUser_notFoundLocally_returns404() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        Response response = authResource.getCurrentUser();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void getCurrentUser_found_returns200() {
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1"))
                .thenReturn(Optional.of(new User("kc-1", "a@b.com", "Alice")));

        Response response = authResource.getCurrentUser();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void validateToken_authenticatedButNoLocalRecord_returns404() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        Response response = authResource.validateToken();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void validateToken_valid_returns200() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(jwtUtil.getCurrentUserInfo()).thenReturn(Optional.of(userInfo("kc-1")));
        when(userRepository.findByKeycloakId("kc-1"))
                .thenReturn(Optional.of(new User("kc-1", "a@b.com", "Alice")));

        Response response = authResource.validateToken();

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
