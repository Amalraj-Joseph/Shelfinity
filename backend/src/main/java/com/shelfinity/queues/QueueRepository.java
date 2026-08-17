/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.queues;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for QueueItem entity operations.
 */
@ApplicationScoped
@Transactional
public class QueueRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Find queue item by ID.
     */
    public Optional<QueueItem> findById(UUID id) {
        QueueItem queueItem = entityManager.find(QueueItem.class, id);
        return Optional.ofNullable(queueItem);
    }
    
    /**
     * Find all queue items.
     */
    public List<QueueItem> findAll() {
        TypedQuery<QueueItem> query = entityManager.createNamedQuery("QueueItem.findAll", QueueItem.class);
        return query.getResultList();
    }
    
    /**
     * Find queue items by status.
     */
    public List<QueueItem> findByStatus(QueueStatus status) {
        TypedQuery<QueueItem> query = entityManager.createNamedQuery("QueueItem.findByStatus", QueueItem.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    /**
     * Find queue items by type.
     */
    public List<QueueItem> findByType(QueueType type) {
        TypedQuery<QueueItem> query = entityManager.createNamedQuery("QueueItem.findByType", QueueItem.class);
        query.setParameter("type", type);
        return query.getResultList();
    }
    
    /**
     * Find pending queue items.
     */
    public List<QueueItem> findPending() {
        TypedQuery<QueueItem> query = entityManager.createNamedQuery("QueueItem.findPending", QueueItem.class);
        query.setParameter("status", QueueStatus.PENDING);
        return query.getResultList();
    }
    
    /**
     * Find queue items by user Keycloak ID.
     */
    public List<QueueItem> findByUserKeycloakId(String userKeycloakId) {
        TypedQuery<QueueItem> query = entityManager.createQuery(
            "SELECT q FROM QueueItem q WHERE q.userKeycloakId = :userKeycloakId ORDER BY q.createdAt DESC",
            QueueItem.class
        );
        query.setParameter("userKeycloakId", userKeycloakId);
        return query.getResultList();
    }
    
    /**
     * Find queue items by book ID.
     */
    public List<QueueItem> findByBookId(UUID bookId) {
        TypedQuery<QueueItem> query = entityManager.createQuery(
            "SELECT q FROM QueueItem q WHERE q.bookId = :bookId ORDER BY q.createdAt DESC",
            QueueItem.class
        );
        query.setParameter("bookId", bookId);
        return query.getResultList();
    }
    
    /**
     * Save a new queue item.
     */
    public QueueItem save(QueueItem queueItem) {
        entityManager.persist(queueItem);
        return queueItem;
    }
    
    /**
     * Update an existing queue item.
     */
    public QueueItem update(QueueItem queueItem) {
        return entityManager.merge(queueItem);
    }
    
    /**
     * Delete a queue item.
     */
    public void delete(QueueItem queueItem) {
        entityManager.remove(queueItem);
    }
    
    /**
     * Delete queue item by ID.
     */
    public void deleteById(UUID id) {
        findById(id).ifPresent(this::delete);
    }
    
    /**
     * Check whether the user already has a PENDING item of the given type for
     * the given book (duplicate-request guard — SPEC.md §6 execution plan).
     */
    public boolean existsPendingForUserAndBook(String userKeycloakId, UUID bookId, QueueType type) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(q) FROM QueueItem q WHERE q.userKeycloakId = :userKeycloakId " +
            "AND q.bookId = :bookId AND q.type = :type AND q.status = :status",
            Long.class
        );
        query.setParameter("userKeycloakId", userKeycloakId);
        query.setParameter("bookId", bookId);
        query.setParameter("type", type);
        query.setParameter("status", QueueStatus.PENDING);
        return query.getSingleResult() > 0;
    }

    /**
     * Count pending queue items.
     */
    public long countPending() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(q) FROM QueueItem q WHERE q.status = :status",
            Long.class
        );
        query.setParameter("status", QueueStatus.PENDING);
        return query.getSingleResult();
    }
    
    /**
     * Count queue items by type.
     */
    public long countByType(QueueType type) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(q) FROM QueueItem q WHERE q.type = :type",
            Long.class
        );
        query.setParameter("type", type);
        return query.getSingleResult();
    }
    
    /**
     * Find all overdue items (approved borrows past their due date).
     */
    public List<QueueItem> findOverdueItems() {
        TypedQuery<QueueItem> query = entityManager.createQuery(
            "SELECT q FROM QueueItem q WHERE q.type = :type AND q.status = :status " +
            "AND q.dueDate IS NOT NULL AND q.dueDate < :now ORDER BY q.dueDate ASC",
            QueueItem.class
        );
        query.setParameter("type", QueueType.BOOK_BORROW);
        query.setParameter("status", QueueStatus.APPROVED);
        query.setParameter("now", java.time.LocalDateTime.now());
        return query.getResultList();
    }
    
    /**
     * Find overdue items for a specific user.
     */
    public List<QueueItem> findOverdueItemsByUser(String userKeycloakId) {
        TypedQuery<QueueItem> query = entityManager.createQuery(
            "SELECT q FROM QueueItem q WHERE q.userKeycloakId = :userKeycloakId " +
            "AND q.type = :type AND q.status = :status " +
            "AND q.dueDate IS NOT NULL AND q.dueDate < :now ORDER BY q.dueDate ASC",
            QueueItem.class
        );
        query.setParameter("userKeycloakId", userKeycloakId);
        query.setParameter("type", QueueType.BOOK_BORROW);
        query.setParameter("status", QueueStatus.APPROVED);
        query.setParameter("now", java.time.LocalDateTime.now());
        return query.getResultList();
    }
}
