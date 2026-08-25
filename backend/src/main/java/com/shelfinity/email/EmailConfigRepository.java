/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

/**
 * Repository for EmailConfig entity operations.
 */
@ApplicationScoped
@Transactional
public class EmailConfigRepository {

    @Inject
    private EntityManager entityManager;

    /**
     * Save a new email configuration.
     */
    public EmailConfig save(EmailConfig config) {
        // Deactivate all other configs when saving a new active one
        if (config.isActive()) {
            deactivateAll();
        }
        entityManager.persist(config);
        return config;
    }
    
    /**
     * Update an existing email configuration.
     */
    public EmailConfig update(EmailConfig config) {
        // Deactivate all other configs when updating to active
        if (config.isActive()) {
            deactivateAll();
        }
        return entityManager.merge(config);
    }
    
    /**
     * Find email configuration by ID.
     */
    public Optional<EmailConfig> findById(UUID id) {
        EmailConfig config = entityManager.find(EmailConfig.class, id);
        return Optional.ofNullable(config);
    }
    
    /**
     * Find the active email configuration.
     */
    public Optional<EmailConfig> findActiveConfig() {
        TypedQuery<EmailConfig> query = entityManager.createNamedQuery(
            "EmailConfig.findActive", EmailConfig.class);
        query.setMaxResults(1);
        List<EmailConfig> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Find all email configurations.
     */
    public List<EmailConfig> findAll() {
        TypedQuery<EmailConfig> query = entityManager.createNamedQuery(
            "EmailConfig.findAll", EmailConfig.class);
        return query.getResultList();
    }
    
    /**
     * Delete email configuration by ID.
     */
    public void deleteById(UUID id) {
        EmailConfig config = entityManager.find(EmailConfig.class, id);
        if (config != null) {
            entityManager.remove(config);
        }
    }
    
    /**
     * Deactivate all email configurations.
     */
    public void deactivateAll() {
        entityManager.createQuery("UPDATE EmailConfig e SET e.active = false")
                .executeUpdate();
    }
    
    /**
     * Activate a specific email configuration.
     */
    public void activate(UUID id) {
        deactivateAll();
        EmailConfig config = entityManager.find(EmailConfig.class, id);
        if (config != null) {
            config.setActive(true);
            entityManager.merge(config);
        }
    }
}
