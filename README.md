This is a custom implementation of MINAS, intended to be used alongise with PCF (https://github.com/douglas444/pcf). If you don't need to use the PCF and prefer to use a MINAS' implementation more close to the original material, check out this repository: https://github.com/douglas444/minas-reference-implementation .

# minas

MINAS (MultI-class learNing Algorithm for data Streams)
An algorithm to address novelty detection in data streams multi-class problems

Faria, E. R., Gama, J., & Carvalho, A. C. (2013, March). 
Novelty detection algorithm for data streams multi-class problems. 
In Proceedings of the 28th annual ACM symposium on applied computing (pp. 795-800).

This implementation is compatible with *pcf*'s 
*Interceptable* interface (https://github.com/douglas444/pcf).

## Requirements

* Apache Maven 3.6.3 or higher
* Java 8

## Maven Dependencies

* streams 1.0-SNAPSHOT (https://github.com/douglas444/streams)
* pcf-core 1.0-SNAPSHOT (https://github.com/douglas444/pcf)
* JUnit Jupiter API 5.6.2 (available at *Maven Central Repository*)

## How to use *minas* as a *Maven* dependency

First you need to install *minas* at your *Maven Local Repository*. 
This can be done by executing the following command line from the root folder: 

```
mvn clean install
```

Once you have installed *minas*, import it at your 
*Maven* project by including the following dependency 
to your project's pom.xml file (edit the version if necessary):

```xml
<dependency>
  <groupId>br.com.douglas444</groupId>
  <artifactId>minas</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

For an example of how to use *minas* in your code, check out the file 
[```src/test/java/br/com/douglas444/minas/MINASTest.java```](src/test/java/br/com/douglas444/minas/MINASTest.java).

## How to use *minas* with *pcf-gui*

First of all you need to build the project's JAR.
This can be done by executing the following command line from the root folder:

```
mvn clean package
```

If you want to build the JAR with the dependencies included, 
execute the following command line instead:

```
mvn clean package assembly:single
```

Once the process is finished, the JAR will be available at the ```target``` folder as 
```minas.jar``` or ```minas-jar-with-dependencies.jar```.

Once you have the JAR, load it in the classpath section of the *pcf-gui*. After that, 
the class *MINASInterceptable* should be listed in the graphical interface.

### Observations:

* We configured the JAR's build process in a way that, 
even if you choose to build with the dependencies included, 
the *pcf-core* dependency will not be included. 
The reason is that the *pcf-core* dependency is already provided 
by the *pcf-gui* when the JAR is loaded through the graphical interface.

* If you choose to build the project without the dependencies 
included, make sure to load all the dependencies' JAR
individually at the *pcf-gui* graphical interface. There is no need to load the *pcf-core*
dependency though, since it is already provided by the *pcf-gui*.
