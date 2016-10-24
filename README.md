# dockds

## Usage

Assuming you have either PostgreSQL or MySQL driver in your depedencies list, just add the the following dependency:
```xml
<dependency>
  <groupId>pl.touk</groupId>
  <artifactId>dockds</artifactId>
  <version>1.0.0-RC1</version>
  <!-- Probably in most cases you will want -->
  <!-- <scope>test</scope> -->
</dependency>
```
Unless you have a datasource url specifified in `application.properties` 
a database instance in a Docker container will be launched along with your applicaton context.

Just as if you had an embedded database (e.g. HSQLDB) driver in your `pom.xml`.
