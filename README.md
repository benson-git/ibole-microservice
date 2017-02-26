# iBole-microservices

iBole-microservices works with JDK 6. TLS usage typically requires using Java 8. 

[![Build Status](https://travis-ci.org/benson-git/ibole-microservice.svg?branch=master)](https://travis-ci.org/benson-git/ibole-microservice)
------
##iBole Microservice Design

Design document: [Link Here](https://github.com/benson-git/ibole-microservice/wiki)

------
##Source Building

iBole-microservices depends on iBole-infrastructure. Please install iBole-infrastructure first before source building.

1. Checkout the ibole-microservice source code:

    cd ~  
    git clone https://github.com/benson-git/ibole-microservice.git ibole-microservice  

    git checkout master  
    or: git checkout -b -v1.0.4  

2. Import the ibole-microservice source code to eclipse project:

    cd ~/ibole-microservice  
    mvn eclipse:eclipse  
    Eclipse -> Menu -> File -> Import -> Exsiting Projects to Workspace -> Browse -> Finish  

3. Build the ibole-microservice binary package:

    cd ~/ibole-microservice  
    mvn clean install -Dmaven.test.skip  
    cd ibole-microservice/target  
    ls  
