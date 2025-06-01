/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.shelfinity.user.entity.User;
import com.shelfinity.user.dto.enums.Role;
import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.logging.SFLogger;

/**
 * Repository class for performing CRUD operations on the User entity.
 */
 
@ApplicationScoped
@Transactional
@SFLoggable
public class UserRepository extends BaseUserRepository<User>{
        
    private static final String CLASS_NAME = UserRepository.class.getName();
    private static String METHOD_NAME;

    @Inject
    private SFLogger logger;

    @Override
    public List<String> getAllUserEmails() {
        METHOD_NAME = "getAllUserEmails";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching all user emails.");
        return findByNamedQuery("User.getAllUserEmails");
    }

    @Override
    public List<String> getAllUserPhoneNumbers() {
        METHOD_NAME = "getAllUserPhoneNumbers";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching all user phone numbers.");
        return findByNamedQuery("User.getAllUserPhoneNumbers"); 
    }

    @Override
    public List<String> getAllUserUsernames() {
        METHOD_NAME = "getAllUserUsernames";
        logger.fine(CLASS_NAME, METHOD_NAME, "Fetching all user usernames.");
        return findByNamedQuery("User.getAllUserUsernames"); 
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        METHOD_NAME = "getUserById";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by id: %s", id.toString()));
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        METHOD_NAME = "getUserByUsername";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by username: %s", username));
        return findByNamedQuery("User.findByUsername", "username", username, User.class);
    }

    @Override
    public Optional<User> getUserByEmail(String email){
        METHOD_NAME = "getUserByEmail";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by email: %s", email));
        return findByNamedQuery("User.findByEmail", "email", email, User.class);
    }

    @Override
    public Optional<User> getUserByPhoneNumber(String phoneNumber){
        METHOD_NAME = "getUserByPhoneNumber";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Fetching user by email: %s", phoneNumber));
        return findByNamedQuery("User.findByPhoneNumber", "phoneNumber", phoneNumber, User.class);
    }

    @Override
    public void addUser(User user) {
        METHOD_NAME = "addUser";
        entityManager.persist(user);
        logger.info(CLASS_NAME, METHOD_NAME, String.format("New user added: %s", user.getId().toString()));
    }

    public void updateUserProfile(User user) {
        METHOD_NAME = "updateUserProfile";
        entityManager.merge(user);
        logger.info(CLASS_NAME, METHOD_NAME, String.format("User profile updated: %s", user.getUsername()));
    }

    public int updateUserPassword(UUID id, String password){
        METHOD_NAME = "updateUserPassword";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("password", password);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.updatePasswordById", params);
    }

    public int updateUserPhoneNumber(UUID id, String phoneNumber){
        METHOD_NAME = "updateUserPhoneNumber";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("phoneNumber", phoneNumber);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.updatePhoneNumberById", params);
    }
    
    public int updateUserEmail(UUID id, String email){
        METHOD_NAME = "updateUserEmail";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("email", email);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.updateEmailById", params);
    }

    public int updateUserUsername(UUID id, String username){
        METHOD_NAME = "updateUserUsername";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("username", username);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.updateUsernameById", params);
    }

    public int updateUserRole(UUID id, Role role) {
        METHOD_NAME = "updateUserRole";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("role", role);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.updateRoleById", params);
    }

    public int toggleUserLock(UUID id) {
        METHOD_NAME = "toggleUserLock";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.toggleLocked", params);
    }

    public int toggleUserEnabled(UUID id) {
        METHOD_NAME = "toggleUserEnabled";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("lastUpdated", Instant.now());
        return executeUpdateNamedQuery("User.toggleEnabled", params);
    }

    public int updateLastLogin(UUID id) {
        METHOD_NAME = "updateLastLogin";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("lastLogin", Instant.now());
        return executeUpdateNamedQuery("User.updateLastLogin", params);
    }
}
