package com.github.mtakaki.dropwizard.hibernate;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Optional;

import io.dropwizard.hibernate.AbstractDAO;

public class TestEntityDAO extends AbstractDAO<TestEntity> {
    public TestEntityDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<TestEntity> findById(final int id) {
        final Criteria criteria = this.criteria().add(Restrictions.eq("id", id));
        return Optional.fromNullable(this.uniqueResult(criteria));
    }
}