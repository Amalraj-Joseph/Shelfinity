/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shelfinity.testsupport.RepositoryTestBase;

/**
 * Repository tier (real Postgres via Testcontainers) — exercises the actual
 * named queries and unique constraints, which mocks can't verify.
 */
class UserRepositoryIT extends RepositoryTestBase {

    private UserRepository userRepository;

    @BeforeEach
    void wireRepository() throws Exception {
        userRepository = new UserRepository();
        Field field = UserRepository.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(userRepository, em);
    }

    private User uniqueUser() {
        String suffix = UUID.randomUUID().toString();
        return new User("kc-" + suffix, "user-" + suffix + "@shelfinity.com", "Test User");
    }

    @Test
    void saveAndFindById_roundTrips() {
        User user = uniqueUser();
        inTransaction(() -> userRepository.save(user));

        var found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getRole()).isEqualTo(UserRole.USER);
        assertThat(found.get().isActive()).isFalse(); // SPEC.md §10.3: fail-closed default
    }

    @Test
    void findByKeycloakId_findsExactMatch() {
        User user = uniqueUser();
        inTransaction(() -> userRepository.save(user));

        assertThat(userRepository.findByKeycloakId(user.getKeycloakId())).isPresent();
        assertThat(userRepository.findByKeycloakId("no-such-keycloak-id")).isEmpty();
    }

    @Test
    void findByEmail_enforcesUniqueConstraintAcrossDuplicates() {
        User user = uniqueUser();
        inTransaction(() -> userRepository.save(user));

        assertThat(userRepository.findByEmail(user.getEmail())).isPresent();
        assertThat(userRepository.existsByEmail(user.getEmail())).isTrue();
        assertThat(userRepository.existsByEmail("nobody-" + UUID.randomUUID() + "@shelfinity.com")).isFalse();
    }

    @Test
    void findByRole_filtersCorrectly() {
        User admin = uniqueUser();
        admin.setRole(UserRole.ADMIN);
        User regular = uniqueUser();
        inTransaction(() -> {
            userRepository.save(admin);
            userRepository.save(regular);
        });

        assertThat(userRepository.findByRole(UserRole.ADMIN))
                .extracting(User::getId)
                .contains(admin.getId())
                .doesNotContain(regular.getId());
    }

    @Test
    void update_persistsChanges() {
        User user = uniqueUser();
        inTransaction(() -> userRepository.save(user));

        inTransaction(() -> {
            User managed = userRepository.findById(user.getId()).orElseThrow();
            managed.setActive(true);
            userRepository.update(managed);
        });

        assertThat(userRepository.findById(user.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void deleteById_removesRecord() {
        User user = uniqueUser();
        inTransaction(() -> userRepository.save(user));
        UUID id = user.getId();

        inTransaction(() -> userRepository.deleteById(id));

        assertThat(userRepository.findById(id)).isEmpty();
    }
}
