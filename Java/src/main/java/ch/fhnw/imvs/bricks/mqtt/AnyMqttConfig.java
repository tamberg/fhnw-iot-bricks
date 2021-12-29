// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

/* package */ final class AnyMqttConfig extends MqttConfig {
    private AnyMqttConfig() {}

    private static final String HOST = "test.mosquitto.org";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getUsername() {
        return USERNAME;
    }

    @Override
    public String getPassword() {
        return PASSWORD;
    }

    @Override
    public String getSubscribeTopic(String brickID) {
        return "bricks/" + brickID + "/actual";
    }

    @Override
    public String getPublishTopic(String brickID) {
        return "bricks/" + brickID + "/target";
    }

    public static MqttConfig fromHost(String configHost) {
        AnyMqttConfig config = new AnyMqttConfig();
        // TODO: get from host or use generic pattern
        return config;
    }
}
