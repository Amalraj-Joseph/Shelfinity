/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Encrypts {@link EmailConfig#password} at rest with AES/GCM — SPEC.md §10.6.
 *
 * JPA instantiates {@code AttributeConverter}s directly, outside CDI, so this
 * reads its key via the static {@link ConfigProvider#getConfig()} rather than
 * {@code @Inject @ConfigProperty}. The configured key string (of any length)
 * is SHA-256 hashed to a fixed 32-byte AES-256 key — a deliberate, documented
 * simplification over a full password-based KDF (PBKDF2/Argon2), justified
 * because this secures an application-level SMTP credential at rest, not a
 * user-facing authentication path.
 *
 * A random 12-byte IV is generated per encryption and stored alongside the
 * ciphertext (base64 of IV || ciphertext) so the column is self-contained.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String CONFIG_KEY_PROPERTY = "email.config.encryption.key";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt email config secret", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String storedValue) {
        if (storedValue == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(storedValue);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt email config secret", e);
        }
    }

    private SecretKeySpec deriveKey() throws NoSuchAlgorithmException {
        String configuredKey = ConfigProvider.getConfig()
                .getOptionalValue(CONFIG_KEY_PROPERTY, String.class)
                .orElse("dev-only-insecure-default-change-me");
        byte[] hashed = MessageDigest.getInstance("SHA-256")
                .digest(configuredKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(hashed, "AES");
    }
}
