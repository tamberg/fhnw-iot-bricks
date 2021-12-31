// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

/* package */ final class MqttConfig {
    private MqttConfig() {}

    private static final String HOST = "test.mosquitto.org";
    private static final String USERNAME = null;
    private static final String PASSWORD = null;

    public String getHost() { return HOST; }
    public String getUsername() { return USERNAME; }
    public String getPassword() { return PASSWORD; }

    public String getSubscribeTopic(String brickID) {
        return "bricks/" + brickID + "/actual";
    }

    public String getPublishTopic(String brickID) {
        return "bricks/" + brickID + "/target";
    }

    public static MqttConfig fromHost(String configHost) {
        MqttConfig config = new MqttConfig();
        // TODO: get from host or use generic pattern
        return config;
    }
}
