/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */

package com.shelfinity.admin.service;

import java.util.List;
import java.util.UUID;

import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.logging.SFLogger;
import com.shelfinity.user.dto.enums.RegistrationStatus;
import com.shelfinity.user.dto.requests.UpdateRegistrationStatusRequestDTO;
import com.shelfinity.user.dto.responses.UpdateRegistrationStatusResponseDTO;
import com.shelfinity.user.entity.UserRegistrationRequest;
import com.shelfinity.user.service.UserService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@SFLoggable
@Transactional
public class QueueService {

    private static final String CLASS_NAME = QueueService.class.getName();
    private static String METHOD_NAME;

    @Inject
    private SFLogger logger;

    @Inject
    UserService userService;

    public List<UserRegistrationRequest> getRequests(UUID id, String email, String phone, String username) {
        METHOD_NAME = "getRequests(id,email,phone,username)";
        return userService.getRequests(id, email, phone, username);
    }

    public UpdateRegistrationStatusResponseDTO updateRegistrationRequest(UUID id, UpdateRegistrationStatusRequestDTO patch) {
        RegistrationStatus newStatus = patch.getStatus(); // may be null
        String remark = patch.getRemark();

        // Delegate business logic to user service
        return userService.updateRegistrationRequestStatus(id, newStatus, remark);
    }

}
