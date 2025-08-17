/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.book.dto.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "UpdateBookRequest")
public class UpdateBookRequestDTO {

    public String title;
    public String author;
    public String isbn;
    public String publishedAt; // ISO-8601
    public String status;
}
