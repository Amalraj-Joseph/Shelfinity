/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Book entity operations.
 */
@ApplicationScoped
@Transactional
public class BookRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Find book by ID.
     */
    public Optional<Book> findById(UUID id) {
        Book book = entityManager.find(Book.class, id);
        return Optional.ofNullable(book);
    }
    
    /**
     * Find book by ISBN.
     */
    public Optional<Book> findByIsbn(String isbn) {
        TypedQuery<Book> query = entityManager.createNamedQuery("Book.findByIsbn", Book.class);
        query.setParameter("isbn", isbn);
        List<Book> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Find books by title (partial match).
     */
    public List<Book> findByTitle(String title) {
        TypedQuery<Book> query = entityManager.createNamedQuery("Book.findByTitle", Book.class);
        query.setParameter("title", "%" + title + "%");
        return query.getResultList();
    }
    
    /**
     * Find books by author (partial match).
     */
    public List<Book> findByAuthor(String author) {
        TypedQuery<Book> query = entityManager.createNamedQuery("Book.findByAuthor", Book.class);
        query.setParameter("author", "%" + author + "%");
        return query.getResultList();
    }
    
    /**
     * Find all available books.
     */
    public List<Book> findAvailable() {
        TypedQuery<Book> query = entityManager.createNamedQuery("Book.findAvailable", Book.class);
        return query.getResultList();
    }

    /**
     * Find books by genre (case-insensitive exact match).
     */
    public List<Book> findByGenre(String genre) {
        TypedQuery<Book> query = entityManager.createNamedQuery("Book.findByGenre", Book.class);
        query.setParameter("genre", genre);
        return query.getResultList();
    }
    
    /**
     * Find all books.
     */
    public List<Book> findAll() {
        TypedQuery<Book> query = entityManager.createNamedQuery("Book.findAll", Book.class);
        return query.getResultList();
    }
    
    /**
     * Save a new book.
     */
    public Book save(Book book) {
        entityManager.persist(book);
        return book;
    }
    
    /**
     * Update an existing book.
     */
    public Book update(Book book) {
        return entityManager.merge(book);
    }
    
    /**
     * Delete a book.
     */
    public void delete(Book book) {
        entityManager.remove(book);
    }
    
    /**
     * Delete book by ID.
     */
    public void deleteById(UUID id) {
        findById(id).ifPresent(this::delete);
    }
    
    /**
     * Check if book exists by ISBN.
     */
    public boolean existsByIsbn(String isbn) {
        return findByIsbn(isbn).isPresent();
    }
    
    /**
     * Search books by title or author.
     */
    public List<Book> search(String searchTerm) {
        TypedQuery<Book> query = entityManager.createQuery(
            "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:searchTerm) OR LOWER(b.author) LIKE LOWER(:searchTerm) ORDER BY b.title",
            Book.class
        );
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.getResultList();
    }
}
