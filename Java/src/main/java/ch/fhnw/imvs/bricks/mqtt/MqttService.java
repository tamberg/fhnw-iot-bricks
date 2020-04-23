// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/* package */ final class MqttService {
    public MqttService() {}

    private IMqttClient client = null;

    public void init(String host, String username, String password) {
        try {
            String hostURI = "tcp://" + host;
            String clientID = ""; // defaults to mqtt-client-PROCESS_ID
            client = new MqttClient(hostURI, clientID, new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }    
    }

    public void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) {
        try {
            System.out.println("subscribe: topic = " + topic);
            client.subscribe(topic, 1, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, payload, 1, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
