/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class UuidStringConverterTest {

    private final UuidStringConverter converter = new UuidStringConverter();

    @Test
    void convertToDatabaseColumn_uuid_returnsCanonicalString() {
        UUID id = UUID.randomUUID();

        assertThat(converter.convertToDatabaseColumn(id)).isEqualTo(id.toString());
    }

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_string_returnsUuid() {
        UUID id = UUID.randomUUID();

        assertThat(converter.convertToEntityAttribute(id.toString())).isEqualTo(id);
    }

    @Test
    void convertToEntityAttribute_null_returnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void roundTrip_preservesValue() {
        UUID id = UUID.randomUUID();

        UUID roundTripped = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(id));

        assertThat(roundTripped).isEqualTo(id);
    }
}
