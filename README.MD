# MQSpringMOM

MQSpringMOM is a standalone JMS Application build on Spring Boot that manages all the incoming messages from an IBM MQ broker and routes them to an ActiveMQ instance.

## Getting Started

Here is a quick overview of how you can install and run the application.

### Prerequisites

  - Java JDK 8
  - Maven
  - **MQ Library**: The project requires the com.ibm.mq.allclient library jar. You can obtain this file from your MQ installation. 
     - For adding it to the project you need to create a local maven repository and add the library:
```sh
    mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id>
        -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>
```
  - Unix environment

### Installation
  Once you have prerequisites ready just run ```mvn clean install``` on the root directory where the pom file is located. The application will be installed in the path: **/opt/apps/MQSpringMOM**

### Usage

Locate the file ```mqspringmom.sh``` on the install location and make it executable by running ```chmod +x mqspringmom.sh``` If you don’t have the $JAVA_HOME variable set up on your system please edit the script and replace the value of the JAVA field with your local Java path.

To start the app simply run ```./mqspringmom.sh start```. To stop it run ```./mqspringmom.sh stop```

The log files are saved in the folder /logs with the name mqspringmom.log'. This file is currently setup to roll every day in the lo4j2 configuration.

A correct initialization will display the following in the logs:

```sh
$ ./mqspringmom.sh start -tail
Starting MqSpringMomApplication: PID set (PID: 26989)
05-01-2018 23:04:36 INFO  MQSpringMOM Application v1.0 - Spring
05-01-2018 23:04:36 INFO  mqQueue: MQ.QUEUE
05-01-2018 23:04:36 INFO  activeMQ Queues: [mqspringqueue1, mqspringqueue2, mqspringqueue3, mqspringqueue4]
05-01-2018 23:04:36 INFO  Configured 200 connections
```

and when a message arrives:

```sh
05-01-2018 23:04:46 INFO  [MqSpringMOMListener] new JMSTextMessage with text: 'Test Message' received from queue: 'MQ.QUEUE' - JMSCorrelationID: 'ID:414d51204d512e4d4752202020202020a85eef5a204e2902'
05-01-2018 23:04:46 INFO  [MqSpringMOMListener] 'ID:414d51204d512e4d4752202020202020a85eef5a204e2902' routed to activeMQ Queue name ='mqspringqueue1'
```

### Todo

 - Implement bidirectional message routing

License
----
Licensed under the MIT License - see the ```LICENSE.md``` file for details
