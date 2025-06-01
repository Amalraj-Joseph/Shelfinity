
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

import com.shelfinity.user.exception.UserAlreadyExistsException;

@Provider
public class UserAlreadyExistsExceptionMapper implements ExceptionMapper<UserAlreadyExistsException> {
    @Override
    public Response toResponse(UserAlreadyExistsException exception) {
        return Response.status(Response.Status.CONFLICT) // 409
                       .entity(exception.getMessage())
                       .build();
    }
}
