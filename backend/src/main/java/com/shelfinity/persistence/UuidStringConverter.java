/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.persistence;

import java.util.UUID;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Root-cause fix for a UUID/Postgres binding defect (SPEC.md §10.8),
 * final iteration. Two earlier attempts both failed for the same underlying
 * reason:
 * <ol>
 *   <li>{@code @Column(columnDefinition = "uuid")} alone: fixed DDL but not
 *       every JDBC binding path, and broke persisting a null value.</li>
 *   <li>An EclipseLink-native {@code org.eclipse.persistence.mappings.converters.Converter}
 *       that set the field's SQL type directly: worked perfectly in the
 *       standalone repository-tier tests (which bundle EclipseLink
 *       themselves), but failed on the actual Liberty deployment with
 *       {@code ClassCastException} / {@code NoClassDefFoundError} — Liberty's
 *       webProfile-10.0 feature bundles its own EclipseLink and deliberately
 *       does not expose its internal {@code org.eclipse.persistence.*} SPI
 *       packages to application code. Only {@code jakarta.persistence.*} is
 *       guaranteed visible, which is the correct, portable Jakarta EE
 *       contract — the app is not supposed to depend on the container's JPA
 *       provider's implementation classes.
 * </ol>
 * This converter sidesteps the whole native-{@code uuid}-column binding
 * problem by not using a native {@code uuid} column at all: it's a plain
 * {@code jakarta.persistence.AttributeConverter}, using only standard JPA
 * API (the same mechanism {@link com.shelfinity.email.EncryptedStringConverter}
 * already uses successfully). UUIDs are stored as their canonical 36-character
 * string form; comparisons become ordinary VARCHAR equality, which Postgres
 * never had a problem with.
 */
@Converter
public class UuidStringConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return dbData == null ? null : UUID.fromString(dbData);
    }
}
