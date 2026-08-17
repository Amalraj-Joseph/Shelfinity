/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

class CorsFilterTest {

    @Test
    void filter_addsCorsHeaders() throws Exception {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        ContainerResponseContext response = mock(ContainerResponseContext.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        org.mockito.Mockito.when(response.getHeaders()).thenReturn(headers);

        new CorsFilter().filter(request, response);

        // FRONTEND_URL is env-driven (defaults to localhost:3000 when unset) —
        // don't assume this JVM's environment, just that some value was set.
        assertThat(headers.getFirst("Access-Control-Allow-Origin")).isNotNull();
        assertThat(headers.getFirst("Access-Control-Allow-Methods")).isEqualTo("GET, POST, PUT, DELETE, OPTIONS");
        assertThat(headers.getFirst("Access-Control-Allow-Headers")).isEqualTo("*");
        assertThat(headers.getFirst("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(headers.getFirst("Access-Control-Max-Age")).isEqualTo("3600");
    }
}
