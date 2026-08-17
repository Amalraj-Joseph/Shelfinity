/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 */
@ApplicationScoped
@Transactional
public class UserRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Find user by ID.
     */
    public Optional<User> findById(UUID id) {
        User user = entityManager.find(User.class, id);
        return Optional.ofNullable(user);
    }
    
    /**
     * Find user by Keycloak ID.
     */
    public Optional<User> findByKeycloakId(String keycloakId) {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findByKeycloakId", User.class);
        query.setParameter("keycloakId", keycloakId);
        List<User> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Find user by email.
     */
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findByEmail", User.class);
        query.setParameter("email", email);
        List<User> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Find users by role.
     */
    public List<User> findByRole(UserRole role) {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findByRole", User.class);
        query.setParameter("role", role);
        return query.getResultList();
    }
    
    /**
     * Find all users.
     */
    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findAll", User.class);
        return query.getResultList();
    }
    
    /**
     * Save a new user.
     */
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }
    
    /**
     * Update an existing user.
     */
    public User update(User user) {
        return entityManager.merge(user);
    }
    
    /**
     * Delete a user.
     */
    public void delete(User user) {
        entityManager.remove(user);
    }
    
    /**
     * Delete user by ID.
     */
    public void deleteById(UUID id) {
        findById(id).ifPresent(this::delete);
    }
    
    /**
     * Check if user exists by Keycloak ID.
     */
    public boolean existsByKeycloakId(String keycloakId) {
        return findByKeycloakId(keycloakId).isPresent();
    }
    
    /**
     * Check if user exists by email.
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
