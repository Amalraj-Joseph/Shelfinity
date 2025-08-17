/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.common.api.mapper;

import java.time.Instant;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = "";
        }
        msg = msg.replace("\\", "\\\\").replace("\"", "\\\"");

        String json = "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"code\":\"INTERNAL_ERROR\","
                + "\"message\":\"" + msg + "\""
                + "}";

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
    }
}
