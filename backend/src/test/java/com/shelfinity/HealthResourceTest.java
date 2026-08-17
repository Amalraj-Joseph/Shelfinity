/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

class HealthResourceTest {

    @Test
    @SuppressWarnings("unchecked")
    void getHealth_returns200WithStatusUp() {
        Response response = new HealthResource().getHealth();

        assertThat(response.getStatus()).isEqualTo(200);
        Map<String, Object> body = (Map<String, Object>) response.getEntity();
        assertThat(body).containsEntry("status", "UP");
        assertThat(body).containsEntry("service", "Shelfinity Backend");
        assertThat(body).containsKey("timestamp");
    }
}
