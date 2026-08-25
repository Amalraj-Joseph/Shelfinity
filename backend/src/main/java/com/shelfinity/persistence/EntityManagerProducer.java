/*
 * Copyright (c) 2025 Amalraj Joseph
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.persistence;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.transaction.TransactionScoped;

/**
 * Hands out the shelfinityPU EntityManager without going through Liberty's
 * container-managed persistence unit bootstrap (what a plain
 * {@code @PersistenceContext} field triggers) — see
 * deploy/JNDI_RACE_DEBUGGING_SUMMARY.md for the full investigation. That path
 * resolves persistence.xml's {@code <jta-data-source>} via a bare
 * {@code new InitialContext().lookup(...)} inside Liberty's own JPA container
 * code ({@code OSGiJPAPUnitInfo.lookupDataSource}), which intermittently
 * loses a one-shot {@code javax.naming.spi.NamingManager} builder
 * registration race at kernel startup — a confirmed Liberty-level bug (14
 * other causes were ruled out during that investigation: feature list, JVM
 * flags, Liberty version, distribution type, JNDI name scoping, and more),
 * not a config mistake here.
 *
 * This sidesteps it entirely: the {@link DataSource} comes from
 * {@code @Resource} field injection instead, which resolves through
 * Liberty's injection engine ({@code InjectionBinding}) — a different,
 * unaffected code path, already proven working in the very same failing
 * trace (the outer {@code @PersistenceContext} field injection itself always
 * succeeded; only JPA's own internal JNDI call, two layers deeper, was
 * broken). The {@link EntityManagerFactory} is then built programmatically —
 * {@code jakarta.persistence.jtaDataSource} pointed at that already-resolved
 * {@link DataSource} instance directly, no JNDI name lookup involved at
 * EntityManagerFactory creation time at all — with
 * {@code eclipselink.target-server=WebSphere_Liberty} so EclipseLink still
 * enlists connections in the ambient JTA transaction via Liberty's own
 * transaction manager, exactly as a container-managed persistence unit
 * would.
 *
 * {@code @TransactionScoped} (not {@code @RequestScoped}/{@code @Dependent})
 * is what makes this a faithful replacement rather than a subtly different
 * one: every repository in this app is {@code @Transactional} (class- or
 * method-level), so an active JTA transaction is always present by the time
 * any EntityManager access happens here. Scoping to the transaction means
 * every repository touched within one ambient transaction (see
 * {@code ReportService}, which injects three) shares the exact same
 * persistence context — matching {@code @PersistenceContext}'s original
 * behavior — with a fresh one created per transaction rather than reused
 * stale across unrelated ones the way a naive request-scoped instance would
 * be.
 *
 * One consequence of {@code @TransactionScoped}: per the JTA spec it's a
 * <i>passivating</i> normal scope ({@code @NormalScope(passivating = true)}),
 * so Weld checks — at the point the producer method actually returns an
 * instance, not at deploy time — whether that instance's runtime class
 * implements {@link Serializable}. EclipseLink's real {@code EntityManager}
 * doesn't, which throws {@code WELD-000053} on every request. Nothing about
 * this scope's lifetime (one JTA transaction) ever actually passivates a
 * bean to disk or replicates it, so this is a structural check to satisfy,
 * not a real serialization concern — {@link #currentEntityManager()} wraps
 * the real EntityManager in a delegating {@link Proxy} that also implements
 * {@code Serializable} purely to pass it.
 */
@ApplicationScoped
public class EntityManagerProducer {

    @Resource(lookup = "jdbc/shelfinityDS")
    private DataSource dataSource;

    private EntityManagerFactory entityManagerFactory;

    /**
     * Forces this {@code @ApplicationScoped} bean to actually instantiate
     * (running {@code @PostConstruct}) at application startup, rather than
     * lazily on first CDI proxy use like a normal-scoped bean otherwise
     * would. That laziness was the real bug behind every previous schema-
     * generation failure here: the first thing that ever touches this
     * producer is a repository's {@code @Inject EntityManager} field,
     * dereferenced from inside a {@code @Transactional} method — so the
     * JTA global transaction the interceptor started is already active by
     * the time {@link #init()} used to run, and Liberty flatly refuses
     * {@code Connection.setAutoCommit(...)} on any connection from this
     * pool while one is ("DSRA9350E: Operation setAutoCommit is not
     * allowed during a global transaction"). Worse, any connection this
     * class opened from the same JTA-associated DataSource during that
     * window was itself silently enlisted in that same ambient
     * transaction, so a schema-generation probe failure poisoned it — and
     * every subsequent statement on it, including the real runtime query
     * that triggered this producer's instantiation in the first place —
     * with Postgres's "current transaction is aborted". Observing
     * {@code @Initialized(ApplicationScoped.class)} — fired once, when the
     * application scope itself starts up — is the standard CDI idiom for
     * an eager singleton: resolving this observer method requires an
     * instance of the bean that declares it, so Weld creates one right
     * then, well before any request (and so any transaction) exists.
     */
    void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        // Intentionally empty — see Javadoc above.
    }

    @PostConstruct
    void init() {
        runSchemaGeneration();

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jtaDataSource", dataSource);
        properties.put("eclipselink.target-server", "WebSphere_Liberty");
        properties.put("jakarta.persistence.schema-generation.database.action", "none");
        // eclipselink.target-server=WebSphere_Liberty tells EclipseLink every
        // connection's transaction is externally (container) managed, so DDL
        // execution always routes through a JTA-transaction-bound accessor —
        // confirmed by testing: it does this even when a schema-generation
        // connection is explicitly supplied, and even with onStartup() below
        // guaranteeing this runs before any transaction exists (so there's no
        // transaction for that accessor to be bound to, and every CREATE
        // TABLE fails "DatabaseAccessor not connected"). So schema generation
        // itself happens separately, in runSchemaGeneration() below, without
        // this flag at all. A short-lived connection here just covers this
        // EMF's own database-platform detection at creation time.
        try (Connection loginConnection = dataSource.getConnection()) {
            loginConnection.setAutoCommit(true);
            properties.put("jakarta.persistence.schema-generation.connection", loginConnection);
            entityManagerFactory = Persistence.createEntityManagerFactory("shelfinityPU", properties);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize EntityManagerFactory", e);
        }
    }

    /**
     * Runs schema generation as its own separate, throwaway, RESOURCE_LOCAL
     * EntityManagerFactory — deliberately without the real EMF's {@code
     * jtaDataSource}/{@code eclipselink.target-server} pair, since that
     * combination routes DDL execution through a JTA-transaction-bound
     * accessor this producer never has (see the comment in {@link #init()}).
     * Instead this hands EclipseLink a plain {@code nonJtaDataSource} and
     * lets it acquire and release its own connections per statement, the
     * normal RESOURCE_LOCAL way — wrapped so every connection comes back
     * with autocommit forced on, since Liberty's pool hands out connections
     * with autocommit <i>off</i> by default and EclipseLink's RESOURCE_LOCAL
     * DDL execution never calls {@code setAutoCommit} itself. Without that,
     * the first (expected) probe failure — the table doesn't exist yet —
     * leaves the connection's transaction aborted, and every following
     * statement, including the actual CREATE TABLEs, is rejected by Postgres
     * with "current transaction is aborted". Forcing autocommit here only
     * works at all because {@link #onStartup} guarantees this runs before
     * any JTA global transaction exists — Liberty otherwise rejects
     * {@code setAutoCommit} outright ("DSRA9350E: Operation setAutoCommit is
     * not allowed during a global transaction").
     */
    private void runSchemaGeneration() {
        Map<String, Object> ddlProperties = new HashMap<>();
        ddlProperties.put("jakarta.persistence.transactionType", "RESOURCE_LOCAL");
        ddlProperties.put("jakarta.persistence.nonJtaDataSource", autoCommitDataSource(dataSource));
        ddlProperties.put("jakarta.persistence.schema-generation.database.action", "create");
        Persistence.createEntityManagerFactory("shelfinityPU", ddlProperties).close();
    }

    /** Wraps a DataSource so every connection it hands out comes back with autocommit forced on. */
    private static DataSource autoCommitDataSource(DataSource delegate) {
        return (DataSource) Proxy.newProxyInstance(
                EntityManagerProducer.class.getClassLoader(),
                new Class<?>[] { DataSource.class },
                (proxy, method, args) -> {
                    Object result = method.invoke(delegate, args);
                    if (result instanceof Connection) {
                        ((Connection) result).setAutoCommit(true);
                    }
                    return result;
                });
    }

    @Produces
    @TransactionScoped
    public EntityManager currentEntityManager() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.joinTransaction();
        return serializableProxy(entityManager);
    }

    public void closeEntityManager(@Disposes EntityManager entityManager) {
        entityManager.close();
    }

    @PreDestroy
    void destroy() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    private static EntityManager serializableProxy(EntityManager delegate) {
        return (EntityManager) Proxy.newProxyInstance(
                EntityManagerProducer.class.getClassLoader(),
                new Class<?>[] { EntityManager.class, Serializable.class },
                (proxy, method, args) -> {
                    try {
                        return method.invoke(delegate, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
