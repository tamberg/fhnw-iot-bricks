// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class MqttProxy extends Proxy {
    private MqttProxy(MqttConfig config) {
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

    @Override
    public void connectBrick(Brick brick) {
        String topic = mqttConfig.getSubscribeTopic(brick.getID());
        IMqttMessageListener listener = new IMqttMessageListener() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.printf("messageArrived topic = \"%s\", payload = \"%s\"\n", topic, message);
                byte[] payload = message.getPayload();
                MqttProxy.this.setPendingPayload(brick, payload);
            }
        };
        mqttService.subscribe(topic, listener);
        bricks.add(brick);
    }

    @Override
    protected void syncBrick(Brick brick) {
        byte[] payload = super.getTargetPayload(brick, false); // not a mock
        String topic = mqttConfig.getPublishTopic(brick.getID());
        mqttService.publish(topic, payload);
        System.out.printf("publish topic = \"%s\"\n", topic);
    }

    @Override
    public void waitForUpdate() {
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

    public static MqttProxy fromConfig(String configHost) {
        MqttConfig config = MqttConfig.fromHost(configHost);
        MqttProxy proxy = new MqttProxy(config); // TODO: singleton per configHost
        proxy.connect();
        return proxy;
    }
}
