/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.RepositoryTestBase;

/**
 * SPEC.md §10.6 (resolved) — also exercises the encryption round-trip through
 * a real persist/find cycle, not just the converter in isolation.
 */
class EmailConfigRepositoryIT extends RepositoryTestBase {

    private EmailConfigRepository emailConfigRepository;

    @BeforeEach
    void wireRepository() throws Exception {
        emailConfigRepository = new EmailConfigRepository();
        Field field = EmailConfigRepository.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(emailConfigRepository, em);
    }

    private EmailConfig config(String senderEmail, boolean active) {
        EmailConfig config = new EmailConfig("smtp.example.com", 587, senderEmail);
        config.setPassword("super-secret-password");
        config.setActive(active);
        return config;
    }

    @Test
    void saveAndFindById_passwordRoundTripsThroughEncryption() {
        EmailConfig config = config("noreply-" + UUID.randomUUID() + "@shelfinity.com", true);

        inTransaction(() -> emailConfigRepository.save(config));

        EmailConfig found = emailConfigRepository.findById(config.getId()).orElseThrow();
        assertThat(found.getPassword()).isEqualTo("super-secret-password");
    }

    @Test
    void findActiveConfig_returnsOnlyActiveOnes() {
        EmailConfig active = config("active-" + UUID.randomUUID() + "@shelfinity.com", true);
        inTransaction(() -> emailConfigRepository.save(active));

        var found = emailConfigRepository.findActiveConfig();

        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void activate_deactivatesOthersAndActivatesTarget() {
        EmailConfig first = config("first-" + UUID.randomUUID() + "@shelfinity.com", true);
        EmailConfig second = config("second-" + UUID.randomUUID() + "@shelfinity.com", false);
        inTransaction(() -> {
            emailConfigRepository.save(first);
            emailConfigRepository.save(second);
        });

        inTransaction(() -> emailConfigRepository.activate(second.getId()));
        // activate() calls deactivateAll(), a bulk UPDATE that bypasses the
        // persistence context — without clearing, findById would return the
        // stale cached `first` (still active=true) instead of re-querying.
        em.clear();

        assertThat(emailConfigRepository.findById(second.getId()).orElseThrow().isActive()).isTrue();
        assertThat(emailConfigRepository.findById(first.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void update_persistsChanges() {
        EmailConfig config = config("update-" + UUID.randomUUID() + "@shelfinity.com", false);
        inTransaction(() -> emailConfigRepository.save(config));

        inTransaction(() -> {
            EmailConfig managed = emailConfigRepository.findById(config.getId()).orElseThrow();
            managed.setSmtpHost("smtp.newhost.com");
            emailConfigRepository.update(managed);
        });

        assertThat(emailConfigRepository.findById(config.getId()).orElseThrow().getSmtpHost())
                .isEqualTo("smtp.newhost.com");
    }

    @Test
    void deleteById_removesRecord() {
        EmailConfig config = config("delete-" + UUID.randomUUID() + "@shelfinity.com", false);
        inTransaction(() -> emailConfigRepository.save(config));
        UUID id = config.getId();

        inTransaction(() -> emailConfigRepository.deleteById(id));

        assertThat(emailConfigRepository.findById(id)).isEmpty();
    }
}
