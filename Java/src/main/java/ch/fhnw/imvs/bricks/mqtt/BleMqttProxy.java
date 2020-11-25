// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

// BleMqttProxy knows how to (un)pack TTN LoRaWAN / MQTT payload.

public final class BleMqttProxy extends Proxy {
    private BleMqttProxy(MqttConfig config) {
        mqttConfig = config;
        mqttService = new MqttService();
        bricks = new ArrayList<Brick>();
    }

    private final MqttConfig mqttConfig;
    private final MqttService mqttService;
    private final List<Brick> bricks;

    // calLed exactly once
    private void connect() {
        String host = mqttConfig.getHost();
        String username = mqttConfig.getUsername();
        String password = mqttConfig.getPassword();
        mqttService.init(host, username, password);
        mqttService.connect();
    }

    private String getValueOf(String json, String name) {
        int p = json.indexOf(name);
        int q = json.indexOf('"', p + 1);
        q = json.indexOf('"', q + 1);
        int r = json.indexOf('"', q + 1);
        return json.substring(q + 1, r);
    }

    @Override
    public void connectBrick(Brick brick) {
        String topic = mqttConfig.getSubscribeTopic(brick.getID());
        IMqttMessageListener listener = new IMqttMessageListener() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //System.out.printf("messageArrived topic = \"%s\", payload = \"%s\"\n", topic, message);
                byte[] ttnMqttPayloadBytes = message.getPayload();
                String ttnMqttPayloadJson = new String(ttnMqttPayloadBytes, StandardCharsets.UTF_8);
                String ttnLoRaPayloadBase64 = BleMqttProxy.this.getValueOf(
                    ttnMqttPayloadJson, "payload_raw");
                byte[] ttnLoRaPayloadBytes = Base64.getDecoder().decode(ttnLoRaPayloadBase64);
                BleMqttProxy.this.setPendingPayload(brick, ttnLoRaPayloadBytes);
            }
        };
        mqttService.subscribe(topic, listener);
        bricks.add(brick);
    }

    @Override
    protected void syncBrick(Brick brick) {
        byte[] ttnLoRaPayloadBytes = super.getTargetPayload(brick, false); // not a mock
        String ttnLoRaPayloadBase64 = Base64.getEncoder().encodeToString(ttnLoRaPayloadBytes);
        String ttnMqttPayloadJson = 
            "{" +
                "\"port\": 1," +
                "\"confirmed\": false," +
                "\"payload_raw\": \"" + ttnLoRaPayloadBase64 + "\"," +
            "}";
        byte[] ttnMqttPayloadBytes = null;
        try {
            ttnMqttPayloadBytes = ttnMqttPayloadJson.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String topic = mqttConfig.getPublishTopic(brick.getID());
        mqttService.publish(topic, ttnMqttPayloadBytes);
        //System.out.printf("publish topic = \"%s\"\n", topic);
    }

    @Override
    public void waitForUpdate() { // TODO: waitForAnyUpdate vs. waitForAllUpdates
        boolean updated = false;
        while (!updated) {
            for (Brick brick : bricks) {
                updated = updated || super.tryUpdate(brick);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100); // ms
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }

    public static BleMqttProxy fromConfig(String configHost) {
        MqttConfig config = BleMqttConfig.fromHost(configHost); // TODO: too early to get config?
        BleMqttProxy proxy = new BleMqttProxy(config); // TODO: singleton per configHost?
        proxy.connect();
        return proxy;
    }
}
