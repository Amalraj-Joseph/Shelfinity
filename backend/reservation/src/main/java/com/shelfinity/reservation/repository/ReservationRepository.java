/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.reservation.repository;

import java.util.List;
import java.util.UUID;

import com.shelfinity.reservation.entity.Reservation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ReservationRepository {

    @PersistenceContext(unitName = "default")
    EntityManager em;

    public void add(Reservation r) {
        em.persist(r);
    }

    public Reservation get(UUID id) {
        return em.find(Reservation.class, id);
    }

    public Reservation merge(Reservation r) {
        return em.merge(r);
    }

    public void delete(Reservation r) {
        em.remove(r);
    }

    public List<Reservation> list(int offset, int limit) {
        return em.createQuery("SELECT r FROM Reservation r ORDER BY r.createdAt DESC", Reservation.class)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
    }
}
