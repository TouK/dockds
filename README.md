# dockds

## Usage

Assuming you have either PostgreSQL or MySQL driver in your depedencies list, just add yet the following dependency:
```xml
<dependency>
  <groupId>pl.touk</groupId>
  <artifactId>dockds</artifactId>
  <version>1.0.0-RC1</version>
  <!-- Probably in most cases you will want -->
  <!-- <scope>test</scope> -->
</dependency>
```
and the following repository:
```xml
<repository>
  <id>touk public releases</id>
  <name>touk public releases</name>
  <url>https://philanthropist.touk.pl/nexus/content/repositories/releases/</url>
</repository>
```

Unless you have a datasource url specifified in `application.properties` 
a database instance in a Docker container will be launched along with your applicaton context.

Just as if you had an embedded database (e.g. HSQLDB) driver in your `pom.xml`.
