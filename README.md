# adoptium.net documentation services

## Introduction

MicroProfile application to provide dynamic documentation on the adoptium.net website.

## Building and launching this application

The generation of the executable jar file can be performed by issuing the following command

    mvn clean package

This will create an executable jar file **documentationservices-bootable.jar** within the _target_ maven folder. This can be started by executing the following command

    java -jar target/documentationservices-bootable.jar

To launch the test page, open your browser at the following URL

    http://localhost:8080/index.html  





