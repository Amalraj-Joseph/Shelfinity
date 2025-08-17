/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.book.dto.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateBookRequest")
public class CreateBookRequestDTO {

    @NotBlank
    @Size(max = 200)
    public String title;
    @NotBlank
    @Size(max = 120)
    public String author;
    @NotBlank
    @Size(max = 32)
    public String isbn;
    public String publishedAt; // ISO-8601
}
