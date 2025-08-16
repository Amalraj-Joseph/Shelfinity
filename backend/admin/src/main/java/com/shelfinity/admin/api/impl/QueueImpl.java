/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.admin.api.impl;

import java.util.List;
import java.util.UUID;

import com.shelfinity.admin.api.QueueAPI;
import com.shelfinity.admin.service.QueueService;
import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.user.entity.UserRegistrationRequest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@SFLoggable
public class QueueImpl implements QueueAPI {

    @Inject
    QueueService queueService;

    @Override
    public Response getQueue(UUID id, String email, String phone, String username) {
        // Pass through the optional filters; service will handle nulls/empties.
        List<UserRegistrationRequest> requests = queueService.getRequests(id, email, phone, username);
        return Response.ok(requests).build();
    }
}
