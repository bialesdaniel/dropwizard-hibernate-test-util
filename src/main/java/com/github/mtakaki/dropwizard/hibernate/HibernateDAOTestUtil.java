package com.github.mtakaki.dropwizard.hibernate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.setup.Environment;
import lombok.Getter;

/**
 * Test utility class that will setup hibernate and SessionFactory. For each
 * test it will spawn an in memory database so every test is completely isolated
 * from each other.
 *
 * @author mtakaki
 *
 */
public class HibernateDAOTestUtil implements TestRule {
    @Getter
    private SessionFactory sessionFactory;
    @Getter
    private Session session;
    private final Class<?> entityClass;
    private final Class<?>[] entitiesClass;

    public HibernateDAOTestUtil(final Class<?> entity, final Class<?>... entities) {
        this.entityClass = entity;
        this.entitiesClass = entities;
        this.setupLogger();
    }

    /**
     * Sets up {@link Logger} at a {@code WARN} level so we don't pollute the
     * console with hibernate debug logs.
     */
    private void setupLogger() {
        // Getting rid of too many warnings in the console.
        final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);
    }

    /**
     * Creates the SessionFactory, opens the Session, and binds the session to
     * the context.
     */
    private void setupSessionAndTransaction() {
        this.sessionFactory = this.buildSessionFactory();
        this.session = this.sessionFactory.openSession();
        ManagedSessionContext.bind(this.session);
    }

    /**
     * Setups hibernate and builds database connection and SessionFactory.
     *
     * @return A SessionFactory used to connect to the in memory database.
     */
    private SessionFactory buildSessionFactory() {
        final SessionFactoryFactory factoryFactory = new SessionFactoryFactory();
        final HibernateBundle<HibernateDAOTestUtilConfiguration> bundle = new HibernateBundle<HibernateDAOTestUtilConfiguration>(
                this.entityClass, this.entitiesClass) {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(final HibernateDAOTestUtilConfiguration configuration) {
                return configuration.getDatabase();
            }
        };
        final HibernateDAOTestUtilConfiguration configuration = new HibernateDAOTestUtilConfiguration();
        final Map<String, String> properties = new HashMap<>();
        properties.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        properties.put(AvailableSettings.GENERATE_STATISTICS, "false");
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put("jadira.usertype.autoRegisterUserTypes", "true");
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");

        final DataSourceFactory databaseConfiguration = configuration.getDatabase();
        databaseConfiguration.setDriverClass("org.hsqldb.jdbcDriver");
        databaseConfiguration.setUrl("jdbc:hsqldb:mem:test");
        databaseConfiguration.setUser("sa");
        databaseConfiguration.setPassword("");
        databaseConfiguration.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        configuration.getDatabase().setProperties(properties);
        return factoryFactory.build(bundle,
                new Environment("hibernate-test-util", new ObjectMapper(), null, new MetricRegistry(),
                        Thread.currentThread().getContextClassLoader()),
                configuration.getDatabase(), Arrays.asList(this.entityClass));
    }

    /**
     * Closes the session, if it's open, drops the schema, and closes the
     * session factory.
     */
    public void closeSessionAndDropSchema() {
        if (this.session != null && this.session.isOpen()) {
            final Transaction transaction = this.session.beginTransaction();
            try {
                this.session.createNativeQuery("TRUNCATE SCHEMA PUBLIC AND COMMIT").executeUpdate();
                transaction.commit();
                this.session.close();
            } finally {
                transaction.rollback();
            }
        }
        this.sessionFactory.close();
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        this.setupSessionAndTransaction();
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    HibernateDAOTestUtil.this.closeSessionAndDropSchema();
                }
            }
        };
    }
}