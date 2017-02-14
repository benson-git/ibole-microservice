# ibole-microservice


================================================================
Source Building
================================================================

1. Checkout the ibole-microservice source code:

    cd ~
    git clone https://github.com/benson-git/ibole-microservice.git ibole-microservice

    git checkout master
    or: git checkout -b -0.0.1

2. Import the ibole-microservice source code to eclipse project:

    cd ~/practices-microservice
    mvn eclipse:eclipse
    Eclipse -> Menu -> File -> Import -> Exsiting Projects to Workspace -> Browse -> Finish

3. Build the practices-microservice binary package:

    cd ~/ibole-microservice
    mvn clean install -Dmaven.test.skip
    cd ibole-microservice/target
    ls
