package com.github.mtakaki.dropwizard.hibernate;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;

/**
 * This configuration is needed just so we can use the hibernate bundle.
 *
 * @author mtakaki
 *
 */
@Getter
class HibernateDAOTestUtilConfiguration extends Configuration {
    private final DataSourceFactory database = new DataSourceFactory();
}
