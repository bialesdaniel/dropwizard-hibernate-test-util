### Status
[![CircleCI](https://circleci.com/gh/mtakaki/dropwizard-hibernate-test-util/tree/master.svg?style=svg)](https://circleci.com/gh/mtakaki/dropwizard-hibernate-test-util/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/mtakaki/dropwizard-hibernate-test-util/badge.svg?branch=master)](https://coveralls.io/github/mtakaki/dropwizard-hibernate-test-util?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7ea79e3b4a874bb2b45310eac68f9a08)](https://www.codacy.com/app/mitsuotakaki/dropwizard-hibernate-test-util?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mtakaki/dropwizard-hibernate-test-util&amp;utm_campaign=Badge_Grade)
[![Download](https://maven-badges.herokuapp.com/maven-central/com.github.mtakaki/dropwizard-hibernate-test-util/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mtakaki/dropwizard-hibernate-test-util)
[![Javadoc](http://javadoc.io/badge/com.github.mtakaki/dropwizard-hibernate-test-util.svg)](http://www.javadoc.io/doc/com.github.mtakaki/dropwizard-hibernate-test-util)
![License](https://img.shields.io/github/license/mtakaki/dropwizard-hibernate-test-util.svg)

# dropwizard-hibernate-test-util

Hibernate utility class for writing integration tests for `AbstractDAO` classes in dropwizard applications. It uses an in-memory database ([HSQLDB](http://hsqldb.org/)) and uses hibernate functionality to auto-create the tables on startup.

This library follows what is described in this blog post: http://www.petrikainulainen.net/programming/testing/writing-tests-for-data-access-code-unit-tests-are-waste/ Bugs often slips through the unit tests with mocks. Using an in-memory database allows you to actually run the SQL scripts and remove the effort of writing mocks.

Supported versions:

| Dropwizard  |  Hibernate test util |
|---|---|
| 0.9.3  | 0.0.1  |
| 0.9.3  | 0.0.2  |
| 1.3.8  | 1.3.8  |
 
## Maven

The library is available at the maven central, so just add the dependency to `pom.xml` with scope set to `test`:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.mtakaki</groupId>
        <artifactId>dropwizard-hibernate-test-util</artifactId>
        <version>1.3.8</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Example

The class `HibernateDAOTestUtil` provides a jUnit Rule, so it can be simply used like this:

```java
public class TestEntityDAOTest {
    @Rule
    public HibernateDAOTestUtil testUtil = new HibernateDAOTestUtil(TestEntity.class);
    
    private TestEntityDAO dao;

    public void setup() {
        this.dao = new TestEntityDAO(this.testUtil.getSessionFactory());
    }

    @Test
    public void testSaveAndQuery() {
        // We have a session opened and ready to be used.
        final Session session = this.testUtil.getSession();
        final TestEntity entity = TestEntity.builder().body("testing writing").build();
        session.save(entity);

        final Optional<TestEntity> foundEntityOptional = this.dao.findById(entity.getId());
        assertThat(foundEntityOptional).isPresent().contains(entity);
    }
}
```

The schema is completely dropped after each test, guaranteeing test isolation. So no need to clean up after yourself, just let the database be destroyed after the test.
