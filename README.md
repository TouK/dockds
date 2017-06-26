# dockds
[![Build Status](https://travis-ci.org/TouK/dockds.svg?branch=master)](https://travis-ci.org/TouK/dockds)

With a little help of Docker embed PostgreSQL or MySQL in your Spring Boot application or test
just as you can do with H2 or HSQLDB.

## Usage

Assuming you have either PostgreSQL or MySQL driver in your depedencies list, just add yet the following dependency:
```xml
<dependency>
  <groupId>pl.touk</groupId>
  <artifactId>dockds</artifactId>
  <version>1.1.0</version>
  <!-- Probably in most cases you will want -->
  <!-- <scope>test</scope> -->
</dependency>
```

Unless you have a datasource url specifified in `application.properties` 
a database instance in a Docker container will be launched along with your applicaton context.

Just as if you had an embedded database (e.g. HSQLDB) driver in your `pom.xml`.

If you have tests using an embedded database with `@DataJpaTest` annotation, you substitute it with `@DockerizedDataJpaTest`. This way you will be able to use native queries in your JPA repositories.

## Getting Started
1. Complete guide [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
2. Add the above dependency to `pom.xml` or `build.gradle`
3. Replace H2 with PostgreSQL or MySQL in the `pom.xml` or `build.gradle`
4. Replace `@DataJpaTest` with `@DockerizedDataJpaTest` in `CustomerRepositoryTests` (which is not described in the guide but is available in the provided source repository)
