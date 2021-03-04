# minas

MINAS (MultI-class learNing Algorithm for data Streams)
An algorithm to address novelty detection in data streams multi-class problems

Faria, E. R., Gama, J., & Carvalho, A. C. (2013, March). Novelty detection algorithm for data streams multi-class problems. In Proceedings of the 28th annual ACM symposium on applied computing (pp. 795-800).

## Requirements

* Apache Maven 3.6.3 or higher

## Maven Dependencies

* streams 1.0-SNAPSHOT (https://github.com/douglas444/streams)
* pcf-core 1.0-SNAPSHOT (https://github.com/douglas444/pcf)
* junit-jupiter 5.6.2 (available at maven repository)

## Install

```mvn clean install```

## Using it

Once you have installed this project, import it at your maven project by including the following dependency to your pom.xml (edit the version if necessary):

```
<dependency>
  <groupId>br.com.douglas444</groupId>
  <artifactId>minas</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

Once MINAS is added to your project as a dependency, you can use the MINASTest.java test file as an example of how to instantiate the MINASController class and how to execute it.
