/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.book.dto.responses;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.book.entity.Book.Status;

@Schema(name = "Book")
public class BookDTO {

    public UUID id;
    public String title;
    public String author;
    public String isbn;
    public Instant publishedAt;
    public Status status;
    public Instant createdAt;
    public Instant updatedAt;
}
