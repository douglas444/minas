# minas

MINAS (MultI-class learNing Algorithm for data Streams)
An algorithm to address novelty detection in data streams multi-class problems

Faria, E. R., Gama, J., & Carvalho, A. C. (2013, March). Novelty detection algorithm for data streams multi-class problems. In Proceedings of the 28th annual ACM symposium on applied computing (pp. 795-800).

This implementation is compatible with pcf's Interceptable (https://github.com/douglas444/pcf).

## Requirements

* Apache Maven 3.6.3 or higher

## Maven Dependencies

* streams 1.0-SNAPSHOT (https://github.com/douglas444/streams)
* pcf-core 1.0-SNAPSHOT (https://github.com/douglas444/pcf)
* junit-jupiter 5.6.2 (available at maven repository)

## Install

From the project root, execute the following command line:

```mvn clean install```

Once the process is finished, the project will be installed at the local maven repository.

## Using it as maven dependency

Once you have installed minas, import it at your maven project by including the following dependency to your pom.xml (edit the version if necessary):

```
<dependency>
  <groupId>br.com.douglas444</groupId>
  <artifactId>minas</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

Once minas is added to your project as a dependency, you can use the MINASTest.java test file as an example of how to instantiate the MINASController class and how to execute it.


## Build the JAR

To build without the dependencies, execute the following command line:

```mvn clean install```

To build with the dependencies included, execute the following command line: 

```mvn clean install assembly:single```

Once the process is finished, the JAR will be available at the ```target``` folder as 
```minas.jar``` or ```minas-jar-with-dependencies.jar```.

### Observations

1. We configured the build process in a way that, even if you choose to build with the dependencies included, the pcf-core dependency will not be included. 
The reason is that the pcf-core dependency is already provided by the pcf-gui when the JAR is loaded through the interface.

2. If you choose to build the project without the dependencies included, make sure to load all the JAR dependencies individually at the pcf-gui interface. 
There is no need to load the pcf-core dependency though, since it is already provided by the pcf-gui.

## Using it at pcf-gui

Once you have the JAR, load it in classpath section of the pcf-gui, after that, the class MINASInterceptable should be listed at the interface.
