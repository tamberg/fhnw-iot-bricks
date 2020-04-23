// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import java.util.HashMap;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

/* package */ final class MqttConfig { // TODO: rename MqttProxyConfig? Ttn...
    private MqttConfig() {}

    private static final String BUTTON_ID = "0000-0002";
    private static final String BUZZER_ID = "0000-0006";
    private static final String HUMITEMP_ID = "0000-0001";
    private static final String HUMITEMP_0_ID = HUMITEMP_ID;
    private static final String HUMITEMP_1_ID = "0000-0003";
    private static final String HUMITEMP_2_ID = "0000-0004";
    private static final String LCDDISPLAY_ID = "0000-0005";
    private static final String LED_ID = "0000-0000";

    private static final String TTN_APP_ID = "fhnw-iot-bricks";
    private static final String TTN_APP_ACCESS_KEY = "<AppAccessKey>";
    private static final String TTN_HOST = "eu.thethings.network";

    private static final String HOST = "test.mosquitto.org"; // TODO: TTN_HOST
    private static final String USERNAME = null; // TODO: TTN_APP_ID
    private static final String PASSWORD = null; // TODO: TTN_APP_ACCESS_KEY

    HashMap<String, String> pubTopics;
    HashMap<String, String> subTopics;

    public String getHost() {
        return HOST;
    }

    public String getUsername() {
        return USERNAME;
    }

    public String getPassword() {
        return PASSWORD;
    }

    public String getSubscribeTopic(String brickID) { // TODO: move to MqttBrickConfig?
        String topic = subTopics.get(brickID);
        if (topic == null) {
            throw new IllegalArgumentException(brickID);
        }
        return topic;
    }

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
        subTopics.put(BUTTON_ID, TTN_APP_ID + "/devices/" + BUTTON_ID + "/up");
        subTopics.put(BUZZER_ID, TTN_APP_ID + "/devices/" + BUZZER_ID + "/up");
        subTopics.put(HUMITEMP_0_ID, TTN_APP_ID + "/devices/" + HUMITEMP_0_ID + "/up");
        subTopics.put(HUMITEMP_1_ID, TTN_APP_ID + "/devices/" + HUMITEMP_1_ID + "/up");
        subTopics.put(HUMITEMP_2_ID, TTN_APP_ID + "/devices/" + HUMITEMP_2_ID + "/up");
        subTopics.put(LCDDISPLAY_ID, TTN_APP_ID + "/devices/" + LCDDISPLAY_ID + "/up");
        subTopics.put(LED_ID, TTN_APP_ID + "/devices/" + LED_ID + "/up");
        pubTopics = new HashMap<String, String>();
        pubTopics.put(BUZZER_ID, TTN_APP_ID + "/devices/" + BUZZER_ID + "/down");
        pubTopics.put(LCDDISPLAY_ID, TTN_APP_ID + "/devices/" + LCDDISPLAY_ID + "/down");
        pubTopics.put(LED_ID, TTN_APP_ID + "/devices/" + LED_ID + "/down");
    }

    public static MqttConfig fromHost(String configHost) {
        MqttConfig config = new MqttConfig();
        config.init(configHost);
        return config;
    }
}

