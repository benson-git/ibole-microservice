# iBole-microservice
===
------
##iBole-microservice design

Design doc:[Design doc](https://github.com/benson-git/ibole-microservice/wiki)



------
##Source Building


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
