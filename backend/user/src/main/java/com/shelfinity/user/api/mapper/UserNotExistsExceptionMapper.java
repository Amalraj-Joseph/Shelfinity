/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.api.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.shelfinity.user.exception.UserNotExistsException;

@Provider
public class UserNotExistsExceptionMapper implements ExceptionMapper<UserNotExistsException> {
    @Override
    public Response toResponse(UserNotExistsException exception) {
        return Response.status(Response.Status.NOT_FOUND) // 404
                       .entity(exception.getMessage())
                       .build();
    }
}