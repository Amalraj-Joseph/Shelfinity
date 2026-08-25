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
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
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

    @PostConstruct
    void init() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jtaDataSource", dataSource);
        properties.put("eclipselink.target-server", "WebSphere_Liberty");
        entityManagerFactory = Persistence.createEntityManagerFactory("shelfinityPU", properties);
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
