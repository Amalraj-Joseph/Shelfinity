/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Book entity for the library management system.
 */
@Entity
@Table(name = "books")
@NamedQueries({
    @NamedQuery(name = "Book.findAll", query = "SELECT b FROM Book b ORDER BY b.title"),
    @NamedQuery(name = "Book.findByTitle", query = "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:title)"),
    @NamedQuery(name = "Book.findByAuthor", query = "SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(:author)"),
    @NamedQuery(name = "Book.findByIsbn", query = "SELECT b FROM Book b WHERE b.isbn = :isbn"),
    @NamedQuery(name = "Book.findAvailable", query = "SELECT b FROM Book b WHERE b.available = true ORDER BY b.title")
})
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Column(nullable = false)
    private String author;
    
    @Column(unique = true)
    private String isbn;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private boolean available = true;
    
    @Positive
    @Column(nullable = false)
    private int totalCopies = 1;
    
    @Column(name = "available_copies", nullable = false)
    private int availableCopies = 1;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Book() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public Book(String title, String author) {
        this();
        this.title = title;
        this.author = author;
    }
    
    // Constructor with all fields
    public Book(String title, String author, String isbn, String description, int totalCopies) {
        this(title, author);
        this.isbn = isbn;
        this.description = description;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isAvailable() {
        return available && availableCopies > 0;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public int getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public int getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", available=" + available +
                ", totalCopies=" + totalCopies +
                ", availableCopies=" + availableCopies +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id != null && id.equals(book.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
