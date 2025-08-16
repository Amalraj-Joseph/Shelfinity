/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.shelfinity.common.logging.SFLogger;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public abstract class BaseUserRepository<T> {

    @PersistenceContext
    protected EntityManager entityManager;

    private static final String CLASS_NAME = BaseUserRepository.class.getName();
    private static String METHOD_NAME;

    @Inject
    protected SFLogger logger;

    public abstract List<T> getAllUsers();
    public abstract List<String> getAllUserEmails();
    public abstract List<String> getAllUserPhoneNumbers();
    public abstract List<String> getAllUserUsernames();
    public abstract Optional<T> getUserById(UUID id);
    public abstract Optional<T> getUserByUsername(String username);
    public abstract Optional<T> getUserByEmail(String email);
    public abstract Optional<T> getUserByPhoneNumber(String phoneNumber);
    public abstract void addUser(T User);

    protected <T> List<T> findByNamedQuery(String namedQuery, Class<T> resultClass){
        METHOD_NAME = "findByNamedQuery";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Executing named query: %s", namedQuery));
        return entityManager
                .createNamedQuery(namedQuery, resultClass)
                .getResultList();
    }

    protected Optional<T> findByNamedQuery(String namedQuery, String parameterName, String parameterValue, Class<T> resultClass){
        METHOD_NAME = "findByNamedQuery";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Executing named query %s with paramter %s=%s", namedQuery, parameterName, parameterValue));
        Optional<T> result = Optional.ofNullable(entityManager.createNamedQuery(namedQuery, resultClass)
                         .setParameter(parameterName, parameterValue)
                         .getResultStream()
                         .findFirst()
                         .orElse(null));
        return result;        
    }

    protected int executeUpdateNamedQuery(String namedQuery, Map<String, Object> parameters) {
        METHOD_NAME = "executeUpdateNamedQuery";
        logger.fine(CLASS_NAME, METHOD_NAME, String.format("Executing update named query %s with params %s", namedQuery, parameters));

        Query query = entityManager.createNamedQuery(namedQuery);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.executeUpdate(); // Returns number of rows updated
    }
}
