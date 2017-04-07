# iBole-microservices

iBole-microservices works with JDK 6. TLS usage typically requires using Java 8. 

[![Build Status](https://travis-ci.org/benson-git/ibole-microservice.svg?branch=master)](https://travis-ci.org/benson-git/ibole-microservice)
[![Coverage Status](https://coveralls.io/repos/github/benson-git/ibole-microservice/badge.svg?branch=master)](https://coveralls.io/github/benson-git/ibole-microservice?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ibole/microservice-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ibole/microservice-all)

------

Design
--------

Design document: [Link](https://github.com/benson-git/ibole-microservice/wiki)

Example
--------
iBole-microservices example: [Link](https://github.com/benson-git/ibole-microservice-example)


Download
--------

Download [the JARs](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22microservice-all%22). Or for Maven, add to your `pom.xml`:
```xml
<dependency>
    <groupId>com.github.ibole</groupId>
    <artifactId>microservice-all</artifactId>
    <version>1.0.6</version>
</dependency>
```

Or for Gradle with non-Android, add to your dependencies:
```gradle
compile 'com.github.ibole:microservice-all:1.0.6'
```
------

Source Building
--------

iBole-microservices depends on iBole-infrastructure. Please install iBole-infrastructure first before source building.

1. Checkout the ibole-microservice source code:
    ```
    cd ~  
    git clone https://github.com/benson-git/ibole-microservice.git ibole-microservice  

    git checkout master  
    or: git checkout -b -v1.0.6  
    ```
2. Import the ibole-microservice source code to eclipse project:
    ```
    cd ~/ibole-microservice  
    mvn eclipse:eclipse  
    Eclipse -> Menu -> File -> Import -> Exsiting Projects to Workspace -> Browse -> Finish  
    ```
3. Build the ibole-microservice binary package:
    ```
    cd ~/ibole-microservice  
    mvn clean install -Dmaven.test.skip  
    cd ibole-microservice/target  
    ls  
    ``` 
  