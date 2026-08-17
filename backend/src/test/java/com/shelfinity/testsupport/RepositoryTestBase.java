/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.testsupport;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Base for the repository tier: real Postgres via Testcontainers, plain JPA
 * (no app server) — SPEC.md's testing decisions log explains why this tier
 * avoids trying to boot Liberty inside the Maven build.
 *
 * The container is a class-level singleton shared across every subclass in
 * the same JVM (Testcontainers' well-known "singleton container" pattern —
 * started once, never explicitly stopped, reaped by Ryuk at JVM exit) so N
 * *RepositoryIT classes don't each pay full Postgres startup cost. Each
 * subclass gets its own EntityManagerFactory with a fresh
 * drop-and-create schema, since the schema is cheap to rebuild and it keeps
 * classes independent even though they share the underlying database server.
 */
@Testcontainers
public abstract class RepositoryTestBase {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("shelfinity_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    private static EntityManagerFactory entityManagerFactory;
    protected EntityManager em;

    @BeforeAll
    static void openEntityManagerFactory() {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", POSTGRES.getJdbcUrl());
        overrides.put("jakarta.persistence.jdbc.user", POSTGRES.getUsername());
        overrides.put("jakarta.persistence.jdbc.password", POSTGRES.getPassword());
        overrides.put("jakarta.persistence.jdbc.driver", POSTGRES.getDriverClassName());
        // Rebuild the schema fresh for each *RepositoryIT class, since the
        // underlying container (and its schema) is shared across classes.
        overrides.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");

        entityManagerFactory = Persistence.createEntityManagerFactory("shelfinityTestPU", overrides);
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void openEntityManager() {
        em = entityManagerFactory.createEntityManager();
    }

    @AfterEach
    void closeEntityManager() {
        if (em != null) {
            em.close();
        }
    }

    /** Runs {@code work} inside a RESOURCE_LOCAL transaction and commits it. */
    protected void inTransaction(Runnable work) {
        em.getTransaction().begin();
        try {
            work.run();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
