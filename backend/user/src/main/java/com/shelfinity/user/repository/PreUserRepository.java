/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.logging.SFLogger;
import com.shelfinity.user.dto.enums.RegistrationStatus;
import com.shelfinity.user.entity.UserRegistrationRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Repository class for performing CRUD operations on the UserRegistrationRequest entity.
 */
@ApplicationScoped
@Transactional
@SFLoggable
public class PreUserRepository extends BaseUserRepository<UserRegistrationRequest>{
        
    private static final String CLASS_NAME = PreUserRepository.class.getName();
    private static String METHOD_NAME;

    @Inject
    private SFLogger logger;

    @Override
    public List<UserRegistrationRequest> getAllUsers(UUID id, String email, String phone, String username) {
        METHOD_NAME = "getAllUsers";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching users with optional filters.");

        Map<String, Object> params = new HashMap<>();
        if (id != null) params.put("id", id);
        if (email != null && !email.isBlank()) params.put("email", email);
        if (phone != null && !phone.isBlank()) params.put("phoneNumber", phone);
        if (username != null && !username.isBlank()) params.put("username", username);

        return findByCriteria(UserRegistrationRequest.class, params);
    }

    @Override
    public List<String> getAllUserEmails() {
        METHOD_NAME = "getAllPreUserEmails";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching all user emails from reg request db.");
        return findByNamedQuery("UserRegistrationRequest.getAllUserEmails", String.class);
    }

    @Override
    public List<String> getAllUserPhoneNumbers() {
        METHOD_NAME = "getAllPreUserPhoneNumbers";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching all user phone numbers from reg request db.");
        return findByNamedQuery("UserRegistrationRequest.getAllUserPhoneNumbers", String.class);
    }

    @Override
    public List<String> getAllUserUsernames() {
        METHOD_NAME = "getAllPreUserUsernames";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching all user usernames from reg request db.");
        return findByNamedQuery("UserRegistrationRequest.getAllUserUsernames", String.class);
    }

    @Override
    public Optional<UserRegistrationRequest> getUserById(UUID id) {
        METHOD_NAME = "getPreUserByEmail";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by id: %s from reg reequest db.", id.toString()));
        return Optional.ofNullable(entityManager.find(UserRegistrationRequest.class, id));
    }

    @Override
    public Optional<UserRegistrationRequest> getUserByUsername(String username) {
        METHOD_NAME = "getPreUserById";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by username: %s from reg request db.", username));
        Optional<UserRegistrationRequest> request = findByNamedQuery("UserRegistrationRequest.findByUsername", "username", username, UserRegistrationRequest.class);
        return request;
    }

    @Override
    public Optional<UserRegistrationRequest> getUserByEmail(String email){
        METHOD_NAME = "getUserByEmail";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by email: %s", email));
        return findByNamedQuery("UserRegistrationRequest.findByEmail", "email", email, UserRegistrationRequest.class);
    }

    @Override
    public Optional<UserRegistrationRequest> getUserByPhoneNumber(String phoneNumber){
        METHOD_NAME = "getUserByPhoneNumber";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by email: %s from reg request db.", phoneNumber));
        Optional<UserRegistrationRequest> request = findByNamedQuery("UserRegistrationRequest.findByPhoneNumber", "phoneNumber", phoneNumber, UserRegistrationRequest.class);
        return request;
    }

    @Override
    public void addUser(UserRegistrationRequest preUser) {
        METHOD_NAME = "addPreUser";
        entityManager.persist(preUser);
        logger.info(CLASS_NAME, METHOD_NAME, String.format("New user request added: %s to reg request db.", preUser.getId().toString()));
    }

    public void updatePreUserStatus(UUID id, RegistrationStatus newStatus, String remarks, UUID  admin) {
        METHOD_NAME = "updateUserRole";
        UserRegistrationRequest userRequest = entityManager.find(UserRegistrationRequest.class, id);
        if (userRequest == null) {
            logger.warning(CLASS_NAME, METHOD_NAME, String.format("Attempt to update role for non-existing user: %s", id.toString()));
            return;
        }
        userRequest.setStatus(newStatus);
        userRequest.setRemark(remarks);
        userRequest.setUpdatedBy(admin);
        entityManager.merge(userRequest);
        logger.info(CLASS_NAME, METHOD_NAME, String.format("Status updated for user: %s, by admin: %s", userRequest.getUsername(), admin.toString()));
    }
}
