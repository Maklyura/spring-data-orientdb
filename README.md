**This is my fork for [spring-data-orientdb](https://github.com/orientechnologies/spring-data-orientdb) in which I try to realize graph functionality of OrientDB.**

**Authors's README:**

# Spring Data OrientDB

The primary goal of the [Spring Data](http://projects.spring.io/) project is to make it easier to build Spring-powered applications that use new data access technologies such as non-relational databases, map-reduce frameworks, and cloud based data services.

The SpringData OrientDB project will implement easy to use APIs for using OrientDB as a Document database and as a Graph database. 

The document module is based on the [Spring Data MongoDB](http://projects.spring.io/spring-data-mongodb/) project. 

To include OrientDB Spring Data in your Java project via Maven, put one of the snippets below in your pom.xml or build.gradle


**My README:** 

## Dependency management

Maven
```xml
<dependency>
    <groupId>com.orientechnologies</groupId>
    <artifactId>spring-data-orientdb</artifactId>
    <version>0.14-3.0.15-SNAPSHOT</version>
</dependency>
```

Gradle:
```groovy
    group: 'com.orientechnologies', name: 'spring-data-orientdb', version: '0.14-3.0.15-SNAPSHOT'
```

## Changes:

### Hierarchical structure of the library

First of all, I united all modules in one. I did it because I want to use OrientDB as multi-model DB, so I need all functionality, but hierarchical structure of the library does not allow two modules dependent on one to see each other. I need way to choose between repositories, but the main idea of hierarchical structure of the library was opposite: commons module did not know about other modules, and modules couldn't see each other. So, I united all modules and placed logic which chooses which repository to use in *commons* PACKAGE, which now can see *object*, *graph* and *document* PACKAGES.
This logic is created by combining my code, code from commons OrientRepositoryFactory and code from OrientObjectRepositoryFactory. *Graph* and *document* modules in spring-data-orientdb did not have any <>RepositoryFactory.
Target SimpleOrientGraphRepository will be returned by OrientRepositoryFactory if user's repository interface extends OrientGraphRepository. OrientRepositoryFactory, in turn, will be returned by OrientRepositoryFactoryBean. So, if you want use OrientGraphRepository you should use
 
@EnableOrientRepositories(basePackages =  "",  repositoryFactoryBeanClass = "")

with OrientRepositoryFactoryBean.class as repositoryFactoryBeanClass.

**Note: there is no specific repository for Vertex at the moment, but you can load and save vertex by OrientObjectRepository.** 

### Edge Repository and Entity

Then, I added some changes in *graph* package and *common* package. These changes allow to save (which means: create, or update if already exists) and delete POJO (Entity) as OEdge – by executing "CREATE EDGE UPSERT" and "DELETE EDGE". As a result, these changes allow to have vertex classes in database with autocreated "in" and "out" metadata, which, as a result, allows to execute match() and traverse() command.

1. If you want to create classes as edges, you should use OrientGraphRepository. It requires entity which meets the following conditions:
2. It must have String type in, out and id field.
3. It must implement EdgeInterface (must have setters and getters for in, out and id field, 'cos repository calls setters and getters for in, out, and id field and given entity mast be type which have such methods, or NPE will occur);
When you pass entity to the "save" method it should have "in" and "out" fields (in POJO) filled with valid OrientDB RID strings (starting with sharp sign followed by cluster and class id separated by colon). For example, "#49:57";
4. Entity must have in and out properties in edge class in Orient DB. And, for correct creation, it requires UNIQUE INDEX on out,in (in this order exactly) (Documentation: "UPSERT (since v 3.0.1) allows to skip the creation of edges that already exist between two vertices (ie. a unique edge for a couple of vertices). This works only if the edge class has a UNIQUE index on out, in fields, otherwise the statement fails.);

### Creating and Deleting Edges under the hood

All "save" and "delete" methods are overridden in my custom SimpleOrientGraphRepository which extends SimpleOrientRepository.  Under the hood, "save" is realized through executing OrientOperations.objectCommand() with SQL string and "in" and "out" from given entity as a parameters. Before execution "in" and "out" values will be converted into ORecordId which implements ORID, and after executing  OrientOperations.command() will be called  OrientOperations.save() with command result as a parameter. This is the easiest way to make rid permanent, cos "CREATE EDGE" returns Entity with temporary rid. And "delete" realized through executing OrientOperations.command() with SQL string "DELETE EDGE"  and entity id as a parameter.
I added objectCommand() method to OrientOperations to use it for that kind of command which return Object result, so in repository I can cast &lt;S&gt; on command result ( where S is type of Entity) and expect that there will be List&lt;Object&gt; instead of OResultSet. For all others command I use OrientOperations.command() and do not care about result type.

**Important note: AbstractOrientOperations in spring-data-orientdb use deprecated ODatabaseDocumentTx and wrapper for it OObjectDatabaseTx. In @deprecated comment in orientechnologies there is an advice "Use OrientDB instead", but OrientDB does not have "query()" or "command()" methods, for example.**
