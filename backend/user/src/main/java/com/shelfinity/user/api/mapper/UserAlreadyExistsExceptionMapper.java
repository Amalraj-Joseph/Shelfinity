
/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.api.mapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.shelfinity.user.exception.UserAlreadyExistsException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserAlreadyExistsExceptionMapper implements ExceptionMapper<UserAlreadyExistsException> {
    @Override
    public Response toResponse(UserAlreadyExistsException exception) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", Response.Status.CONFLICT.getStatusCode());
        errorResponse.put("error", "Conflict");
        errorResponse.put("message", exception.getMessage());
        
        return Response.status(Response.Status.CONFLICT) // 409
                       .type(MediaType.APPLICATION_JSON)
                       .entity(errorResponse)
                       .build();
    }
}
