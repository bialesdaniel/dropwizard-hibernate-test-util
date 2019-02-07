package com.github.mtakaki.dropwizard.hibernate;

import org.hibernate.SessionFactory;

import com.google.common.base.Optional;

import io.dropwizard.hibernate.AbstractDAO;

public class TestEntityDAO extends AbstractDAO<TestEntity> {
    public TestEntityDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<TestEntity> findById(final int id) {
        return Optional.fromNullable(this.get(id));
    }

    public int save(final TestEntity entity) {
        return this.persist(entity).getId();
    }
}