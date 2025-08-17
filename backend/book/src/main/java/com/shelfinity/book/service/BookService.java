/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.book.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.shelfinity.book.dto.requests.CreateBookRequestDTO;
import com.shelfinity.book.dto.requests.UpdateBookRequestDTO;
import com.shelfinity.book.dto.responses.BookDTO;
import com.shelfinity.book.entity.Book;
import com.shelfinity.book.entity.Book.Status;
import com.shelfinity.book.repository.BookRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookService {

    @Inject
    BookRepository repo;

    public UUID create(CreateBookRequestDTO in) {
        Book b = new Book();
        b.setTitle(in.title);
        b.setAuthor(in.author);
        b.setIsbn(in.isbn);
        if (in.publishedAt != null && !in.publishedAt.isBlank()) {
            b.setPublishedAt(Instant.parse(in.publishedAt));
        }
        repo.add(b);
        return b.getId();
    }

    public void update(UUID id, UpdateBookRequestDTO in) {
        Book b = repo.get(id);
        if (b == null) {
            throw new IllegalArgumentException("Book not found");
        }
        if (in.title != null) {
            b.setTitle(in.title);
        }
        if (in.author != null) {
            b.setAuthor(in.author);
        }
        if (in.isbn != null) {
            b.setIsbn(in.isbn);
        }
        if (in.publishedAt != null) {
            b.setPublishedAt(Instant.parse(in.publishedAt));
        }
        if (in.status != null) {
            b.setStatus(Status.valueOf(in.status));
        }
        repo.merge(b);
    }

    public void delete(UUID id) {
        Book b = repo.get(id);
        if (b != null) {
            repo.delete(b);
        }
    }

    public BookDTO get(UUID id) {
        Book b = repo.get(id);
        if (b == null) {
            return null;
        }
        return toDTO(b);
    }

    public List<BookDTO> list(int offset, int limit) {
        return repo.list(offset, limit).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private BookDTO toDTO(Book b) {
        BookDTO d = new BookDTO();
        d.id = b.getId();
        d.title = b.getTitle();
        d.author = b.getAuthor();
        d.isbn = b.getIsbn();
        d.publishedAt = b.getPublishedAt();
        d.status = b.getStatus();
        d.createdAt = b.getCreatedAt();
        d.updatedAt = b.getUpdatedAt();
        return d;
    }
}
