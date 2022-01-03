// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>
#include <ChainableLED.h>

#define NUM_LEDS  1
#define MQTT_CONN_KEEPALIVE 30

ChainableLED led(5, 4, NUM_LEDS); // Grove I2C_1 or _2

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "test.mosquitto.org"; // TODO
const int port = 8883;
const char *topicStr = "bricks/0000-0000/target"; // TODO

BearSSL::WiFiClientSecure client;
Adafruit_MQTT_Client mqtt(&client, host, port);
Adafruit_MQTT_Subscribe topic(&mqtt, topicStr);

void handleMessage(char *buf, uint16_t len) {
  if (len == 3) {
    char r = buf[0];
    char g = buf[1];
    char b = buf[2];
    led.setColorRGB(0, r, g, b);
  }
}

void setup() {
  Serial.begin(115200);
  led.setColorRGB(0, 0, 0, 0);
  Serial.print("\nConnecting to network ");
  Serial.println(ssid);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100); // keeps watchdog happy
  }
  Serial.print("Connected to network, local IP = "); 
  Serial.println(WiFi.localIP());
  client.setInsecure(); // no cert validation
  topic.setCallback(handleMessage);
  mqtt.subscribe(&topic);
}

void loop() {
  if (mqtt.connected()) {
    Serial.println("Connected (still)");
    mqtt.processPackets(10000); // ms, calls callbacks
    if (!mqtt.ping()) {
      mqtt.disconnect();  
    }
  } else {
    int result = mqtt.connect(); // calls client.connect()
    if (result != 0) {
      Serial.println(mqtt.connectErrorString(result));
      delay(3000);
    }
  }
}
