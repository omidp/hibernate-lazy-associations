## What is Hibernate Lazy Migrator

The Hibernate recommendation is to statically mark all associations lazy and to use dynamic fetching strategies for eagerness.

This is unfortunately at odds with the Jakarta Persistence specification which defines that all one-to-one and many-to-one associations should be eagerly fetched by default.

Hibernate, as a Jakarta Persistence provider, honors that default.

This openrewrite project helps you to refactor all your EAGER associations to LAZY.

for example : 

From 

```
@ManyToOne
private Object manyToOne;
```

To 

```
@ManyToOne(fetch = FetchType.LAZY)
private Object manyToOne;
```

## How to build

- Clone and build the project 

```
mvn clean install
```

## How to use

- Add the open rewrite plugin to your maven pom file 

```
<plugin>
   <groupId>org.openrewrite.maven</groupId>
   <artifactId>rewrite-maven-plugin</artifactId>
   <version>5.15.4</version>
   <configuration>
      <activeRecipes>
         <recipe>com.omidp.app.LazyAssociations</recipe>
      </activeRecipes>
   </configuration>
   <dependencies>
      <!-- This module isn't packaged with OpenRewrite -->
      <dependency>
         <groupId>com.omidp.app</groupId>
         <artifactId>lazy-migrator</artifactId>
         <version>1.0-SNAPSHOT</version>
      </dependency>
   </dependencies>
</plugin>
```

- Execute the plugin 

```
mvn rewrite:run
```

Or depends on your POM configuration

```
mvn org.openrewrite.maven:rewrite-maven-plugin:run
```
