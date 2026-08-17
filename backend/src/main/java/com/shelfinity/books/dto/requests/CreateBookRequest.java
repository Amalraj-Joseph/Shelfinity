/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO for creating a new book.
 */
public class CreateBookRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    private String isbn;

    private String description;

    private String genre;

    private Integer publicationYear;

    @Positive(message = "Total copies must be positive")
    private int totalCopies = 1;

    // Default constructor
    public CreateBookRequest() {}

    // Constructor with required fields
    public CreateBookRequest(String title, String author) {
        this.title = title;
        this.author = author;
    }

    // Constructor with all fields
    public CreateBookRequest(String title, String author, String isbn, String description, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.totalCopies = totalCopies;
    }
    
    // Getters and Setters
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    @Override
    public String toString() {
        return "CreateBookRequest{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", description='" + description + '\'' +
                ", totalCopies=" + totalCopies +
                '}';
    }
}
