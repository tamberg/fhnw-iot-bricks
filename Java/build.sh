#!/bin/sh
javac -d target -cp src:target src/main/java/ch/fhnw/imvs/bricks/core/*.java
javac -d target -cp src:target src/main/java/ch/fhnw/imvs/bricks/impl/*.java
javac -d target -cp src:target src/main/java/ch/fhnw/imvs/bricks/http/*.java
javac -d target -cp src:target src/main/java/ch/fhnw/imvs/bricks/mock/*.java
javac -d target -cp src:target:lib/minimal-json-0.9.5.jar:lib/org.eclipse.paho.client.mqttv3-1.2.3.jar src/main/java/ch/fhnw/imvs/bricks/mqtt/*.java
javac -d target -cp src:target src/main/java/ch/fhnw/imvs/bricks/sensors/*.java
javac -d target -cp src:target src/main/java/ch/fhnw/imvs/bricks/actuators/*.java
javac -d target -cp src:target:lib/minimal-json-0.9.5.jar:lib/nrjavaserial-5.2.1.jar src/test/*.java
