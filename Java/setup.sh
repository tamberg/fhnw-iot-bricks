#!/bin/sh
mkdir target
mkdir lib
curl -Lo lib/minimal-json-0.9.5.jar https://github.com/ralfstx/minimal-json/releases/download/0.9.5/minimal-json-0.9.5.jar &&
curl -Lo lib/org.eclipse.paho.client.mqttv3-1.2.3.jar https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.client.mqttv3/1.2.3/org.eclipse.paho.client.mqttv3-1.2.3.jar &&
curl -Lo lib/nrjavaserial-5.2.1.jar https://github.com/NeuronRobotics/nrjavaserial/releases/download/5.2.1/nrjavaserial-5.2.1.jar
