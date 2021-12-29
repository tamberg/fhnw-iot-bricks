// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

// TtnMqttProxy knows how to (un)pack TTN LoRaWAN / MQTT payload.

public final class TtnMqttProxy extends Proxy {
    private TtnMqttProxy(MqttConfig config) {
        mqttConfig = config;
        mqttService = new MqttService();
    }

    private final MqttConfig mqttConfig;
    private final MqttService mqttService;

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
                String ttnLoRaPayloadBase64 = TtnMqttProxy.this.getValueOf(
                    ttnMqttPayloadJson, "payload_raw");
                byte[] ttnLoRaPayloadBytes = Base64.getDecoder().decode(ttnLoRaPayloadBase64);
                TtnMqttProxy.this.setPendingPayload(brick, ttnLoRaPayloadBytes);
            }
        };
        mqttService.subscribe(topic, listener);
        super.addBrick(brick);
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

    public static TtnMqttProxy fromConfig(String configHost) {
        MqttConfig config = TtnMqttConfig.fromHost(configHost); // TODO: too early to get config?
        TtnMqttProxy proxy = new TtnMqttProxy(config); // TODO: singleton per configHost?
        proxy.connect();
        return proxy;
    }
}
