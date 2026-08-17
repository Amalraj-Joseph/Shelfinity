/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * SPEC.md §10.6 (resolved) — SMTP credentials must be encrypted at rest and
 * round-trip correctly. Uses the default dev key since no
 * email.config.encryption.key system property is set in this test JVM.
 */
class EncryptedStringConverterTest {

    private final EncryptedStringConverter converter = new EncryptedStringConverter();

    @Test
    void roundTrips_plaintextThroughEncryptionAndBack() {
        String stored = converter.convertToDatabaseColumn("super-secret-smtp-password");

        assertThat(stored).isNotEqualTo("super-secret-smtp-password");
        assertThat(converter.convertToEntityAttribute(stored)).isEqualTo("super-secret-smtp-password");
    }

    @Test
    void convertToDatabaseColumn_nullStaysNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_nullStaysNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void convertToDatabaseColumn_producesDifferentCiphertextEachTime() {
        // Random IV per encryption (SPEC.md §10.6) — same plaintext must not
        // produce the same stored value twice, or an attacker with read access
        // to the column could correlate identical passwords across rows.
        String first = converter.convertToDatabaseColumn("same-password");
        String second = converter.convertToDatabaseColumn("same-password");

        assertThat(first).isNotEqualTo(second);
        assertThat(converter.convertToEntityAttribute(first)).isEqualTo("same-password");
        assertThat(converter.convertToEntityAttribute(second)).isEqualTo("same-password");
    }

    @Test
    void roundTrips_emptyString() {
        String stored = converter.convertToDatabaseColumn("");

        assertThat(converter.convertToEntityAttribute(stored)).isEmpty();
    }
}
