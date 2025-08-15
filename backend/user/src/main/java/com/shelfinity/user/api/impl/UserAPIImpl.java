/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.api.impl;

import java.util.List;
import java.util.Locale;

import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.messages.Messages;
import com.shelfinity.user.api.UserAPI;
import com.shelfinity.user.dto.enums.RegistrationStatus;
import com.shelfinity.user.dto.requests.RegisterUserRequestDTO;
import com.shelfinity.user.dto.responses.RegisterUserResponseDTO;
import com.shelfinity.user.service.UserService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@RequestScoped
@SFLoggable
public class UserAPIImpl implements UserAPI {

    @Inject
    private UserService userService;

    @Context HttpHeaders headers;

    @Override
    public Response registerUser(RegisterUserRequestDTO request) {

        // Let exceptions bubble up, they will be handled by exception mappers
        Locale locale = getPreferredLocale();
        userService.registerUser(request);

        RegisterUserResponseDTO response = new RegisterUserResponseDTO();
        response.setStatus(RegistrationStatus.APPROVED);
        response.setMessage(Messages.resolveMessage(Messages.SFUI003, locale));

        return Response.status(Status.CREATED).entity(response).build();
    }

    private Locale getPreferredLocale() {
        List<Locale> langs = headers.getAcceptableLanguages();
        return langs.isEmpty() ? Locale.ENGLISH : langs.get(0);
    }
}
