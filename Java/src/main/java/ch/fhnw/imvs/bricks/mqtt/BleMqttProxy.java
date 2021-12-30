// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class BleMqttProxy extends Proxy {
    private BleMqttProxy(MqttConfig config) {
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

    @Override
    public void connectBrick(Brick brick) {
        String topic = mqttConfig.getSubscribeTopic(brick.getID());
        IMqttMessageListener listener = new IMqttMessageListener() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.printf("messageArrived topic = \"%s\", payload = \"%s\"\n", topic, message);
                byte[] payload = message.getPayload();
                BleMqttProxy.this.setPendingPayload(brick, payload);
            }
        };
        mqttService.subscribe(topic, listener);
        super.addBrick(brick);
    }

    @Override
    protected void syncBrick(Brick brick) {
        byte[] payload = super.getTargetPayload(brick, false); // not a mock
        String topic = mqttConfig.getPublishTopic(brick.getID());
        mqttService.publish(topic, payload);
        System.out.printf("publish topic = \"%s\"\n", topic);
    }

    public static BleMqttProxy fromConfig(String configHost) {
        MqttConfig config = BleMqttConfig.fromHost(configHost); // TODO: too early to get config?
        BleMqttProxy proxy = new BleMqttProxy(config); // TODO: singleton per configHost?
        proxy.connect();
        return proxy;
    }
}
