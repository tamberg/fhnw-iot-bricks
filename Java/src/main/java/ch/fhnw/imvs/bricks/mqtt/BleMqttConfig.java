// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import java.util.HashMap;

/* package */ final class BleMqttConfig extends MqttConfig { // TODO: get IDs, keys from config files
    private BleMqttConfig() {}

    private static final String BUTTON_ID = "0000-0002";
    private static final String BUZZER_ID = "0000-0006";
    private static final String DISTANCE_ID = "0000-0003";
    private static final String HUMITEMP_ID = "0000-0001";
    private static final String HUMITEMP_0_ID = HUMITEMP_ID;
    private static final String HUMITEMP_1_ID = "0000-0007";
    private static final String HUMITEMP_2_ID = "0000-0004";
    private static final String LCDDISPLAY_ID = "0000-0005";
    private static final String LED_ID = "0000-0000";

    private static final String TTN_APP_ID = "fhnw-iot";
    private static final String TTN_APP_ACCESS_KEY = "ttn-account-v2.2XTAHf5qX7E6xGbMOkRTgagGSK2RljdNzWe5HNSL1P8"; // read only
    private static final String TTN_HOST = "eu.thethings.network";

    private static final String HOST = TTN_HOST;
    private static final String USERNAME = TTN_APP_ID;
    private static final String PASSWORD = TTN_APP_ACCESS_KEY;

    HashMap<String, String> pubTopics;
    HashMap<String, String> subTopics;

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
    public String getSubscribeTopic(String brickID) { // TODO: move to MqttBrickConfig?
        String topic = subTopics.get(brickID);
        if (topic == null) {
            throw new IllegalArgumentException(brickID);
        }
        return topic;
    }

    @Override
    public String getPublishTopic(String brickID) {
        String topic = pubTopics.get(brickID);
        if (topic == null) {
            throw new IllegalArgumentException(brickID);
        }
        return topic;
    }

    private void init(String configHost) {
        // TODO: get from host or use generic pattern
        subTopics = new HashMap<String, String>();
        subTopics.put(BUTTON_ID, TTN_APP_ID + "/devices/fhnw-iot-5/up");
        subTopics.put(BUZZER_ID, TTN_APP_ID + "/devices/fhnw-iot-6/up");
        subTopics.put(DISTANCE_ID, TTN_APP_ID + "/devices/fhnw-iot-7/up");
        subTopics.put(HUMITEMP_0_ID, TTN_APP_ID + "/devices/fhnw-iot-0/up");
        subTopics.put(HUMITEMP_1_ID, TTN_APP_ID + "/devices/fhnw-iot-1/up");
        subTopics.put(HUMITEMP_2_ID, TTN_APP_ID + "/devices/fhnw-iot-2/up");
        subTopics.put(LCDDISPLAY_ID, TTN_APP_ID + "/devices/fhnw-iot-3/up");
        subTopics.put(LED_ID, TTN_APP_ID + "/devices/fhnw-iot-4/up");
        pubTopics = new HashMap<String, String>();
        pubTopics.put(BUZZER_ID, TTN_APP_ID + "/devices/fhnw-iot-6/down");
        pubTopics.put(LCDDISPLAY_ID, TTN_APP_ID + "/devices/fhnw-iot-3/down");
        pubTopics.put(LED_ID, TTN_APP_ID + "/devices/fhnw-iot-4/down");
    }

    public static MqttConfig fromHost(String configHost) {
        BleMqttConfig config = new BleMqttConfig();
        config.init(configHost);
        return config;
    }
}
