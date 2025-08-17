/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.book.repository;

import java.util.List;
import java.util.UUID;

import com.shelfinity.book.entity.Book;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class BookRepository {

    @PersistenceContext(unitName = "default")
    EntityManager em;

    public void add(Book book) {
        em.persist(book);
    }

    public Book get(UUID id) {
        return em.find(Book.class, id);
    }

    public Book merge(Book book) {
        return em.merge(book);
    }

    public void delete(Book book) {
        em.remove(book);
    }

    public List<Book> list(int offset, int limit) {
        return em.createQuery("SELECT b FROM Book b ORDER BY b.createdAt DESC", Book.class)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
    }
}
