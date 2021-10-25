# adoptium.net documentation services

TEST PR FOR GITHUB ACTION


## Introduction

The service is in development. It should provide the documentation that is created at [this repository](https://github.com/AdoptOpenJDK/website-adoptium-documentation) on the [adoptium.net website](https://adoptium.net). The service is based on [Eclipse MicroProfile](https://microprofile.io).

## Building and launching this application

The generation of the executable jar file can be performed by issuing the following command

    mvn clean package

This will create an executable jar file **documentationservices-bootable.jar** within the _target_ maven folder. This can be started by executing the following command

    java -jar target/documentationservices-bootable.jar

To launch the test page, open your browser at the following URL

    http://localhost:8080/index.html  

To check the health of the application, use the following command

    curl -s http://localhost:9990/health |jq

