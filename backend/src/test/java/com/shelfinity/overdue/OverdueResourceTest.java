/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.overdue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.security.JwtUtil;

import jakarta.ws.rs.core.Response;

/**
 * Container-managed @RolesAllowed("admin") isn't exercised outside a real
 * deployment; these tests cover the manual isAuthenticated() gate and
 * successful delegation to OverdueService, which is what's actually testable
 * at this layer without a running server.
 */
@ExtendWith(MockitoExtension.class)
class OverdueResourceTest {

    @Mock private OverdueService overdueService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private OverdueResource overdueResource;

    @Test
    void getAllOverdueItems_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = overdueResource.getAllOverdueItems();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getAllOverdueItems_authenticated_delegatesToService() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(overdueService.getOverdueItems()).thenReturn(List.of());

        Response response = overdueResource.getAllOverdueItems();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getMyOverdueItems_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = overdueResource.getMyOverdueItems();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getMyOverdueItems_authenticated_usesCallerKeycloakId() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(jwtUtil.getCurrentUserKeycloakId()).thenReturn(java.util.Optional.of("kc-1"));
        when(overdueService.getOverdueItemsForUser("kc-1")).thenReturn(List.of());

        Response response = overdueResource.getMyOverdueItems();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void getOverdueStats_notAuthenticated_returns401() {
        when(jwtUtil.isAuthenticated()).thenReturn(false);

        Response response = overdueResource.getOverdueStats();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void getOverdueStats_authenticated_returnsStats() {
        when(jwtUtil.isAuthenticated()).thenReturn(true);
        when(overdueService.getOverdueStats()).thenReturn(new OverdueService.OverdueStats(0, 0, 0));

        Response response = overdueResource.getOverdueStats();

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
