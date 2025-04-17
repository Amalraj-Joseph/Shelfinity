/*
 * MIT License
 * 
 * Copyright (c) 2025 Shadow-Codex
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.shelfinity.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import com.shelfinity.user.entity.User;
import com.shelfinity.user.dto.enums.Role;
import com.shelfinity.common.logging.SFLoggable;
import com.shelfinity.common.logging.SFLogger;

/**
 * Repository class for performing CRUD operations on the User entity.
 */
@Stateless
@Named
@Transactional
@SFLoggable
public class UserRepository {

    private static final String GET_ALL_EMAILS_QUERY = "SELECT u.email FROM User u";

    @PersistenceContext
    private EntityManager entityManager;

    private final SFLogger logger;

    @Inject
    public UserRepository(SFLogger logger){
        this.logger = logger;
    }

    public List<String> getAllUserEmails() {
        logger.fine("Fetching all user emails.");
        return entityManager
                .createQuery(GET_ALL_EMAILS_QUERY, String.class)
                .getResultList();
    }

    public Optional<User> getUserByEmail(String email) {
        logger.fine(String.format("Fetching user by email: {}", email));
        return Optional.ofNullable(entityManager.find(User.class, email));
    }

    public void addUser(User user) {
        entityManager.persist(user);
        logger.info(String.format("New user added: {}", user.getEmail()));
    }

    public void updateUserProfile(User user) {
        User existingUser = entityManager.find(User.class, user.getEmail());
        if (existingUser == null) {
            logger.warning(String.format("Attempt to update non-existing user: {}", user.getEmail()));
            return;
        }

        existingUser.setName(user.getName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setAddress(user.getAddress());
        entityManager.merge(existingUser);
        logger.info(String.format("User profile updated: {}", user.getEmail()));
    }

    public void updateUserPassword(String email, String password) {
        User user = entityManager.find(User.class, email);
        if (user == null) {
            logger.warning(String.format("Attempt to update password for non-existing user: {}", email));
            return;
        }

        user.setPassword(password);
        entityManager.merge(user);
        logger.info(String.format("Password updated for user: {}", email));
    }

    public void updateUserRole(String email, Role role) {
        User user = entityManager.find(User.class, email);
        if (user == null) {
            logger.warning(String.format("Attempt to update role for non-existing user: {}", email));
            return;
        }

        user.setRole(role);
        entityManager.merge(user);
        logger.info(String.format("Role updated for user: {}, new role: {}", email, role));
    }

    public void toggleUserLock(String email) {
        User user = entityManager.find(User.class, email);
        if (user == null) {
            logger.warning(String.format("Attempt to toggle lock for non-existing user: {}", email));
            return;
        }

        user.setLocked(!user.isLocked());
        entityManager.merge(user);
        logger.info(String.format("Lock status toggled for user: {}. Now locked: {}", email, user.isLocked()));
    }

    public void toggleUserEnabled(String email) {
        User user = entityManager.find(User.class, email);
        if (user == null) {
            logger.warning(String.format("Attempt to toggle enabled status for non-existing user: {}", email));
            return;
        }

        user.setEnabled(!user.isEnabled());
        entityManager.merge(user);
        logger.info(String.format("Enabled status toggled for user: {}. Now enabled: {}", email, user.isEnabled()));
    }

    public void updateLastLogin(String email) {
        User user = entityManager.find(User.class, email);
        if (user == null) {
            logger.warning(String.format("Attempt to update last login for non-existing user: {}", email));
            return;
        }

        user.setLastLogin(LocalDateTime.now());
        entityManager.merge(user);
        logger.fine(String.format("Last login updated for user: {}", email));
    }
}
