// Copyright (c) 2021 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>

const int buttonPin = 5; // Grove adapter I2C_1 or _2 used as D6

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "test.mosquitto.org"; // TODO
const int port = 8883;
const char *topicStr = "bricks/0000-0002/actual"; // TODO

BearSSL::WiFiClientSecure client;
Adafruit_MQTT_Client mqtt(&client, host, port);

void setup() {
  Serial.begin(115200);
  pinMode(buttonPin, INPUT);
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
}

long t0 = 0;
int oldPressed = -1;

void loop() {
  if (mqtt.connected()) {
    int pressed = digitalRead(buttonPin);
    if (pressed != oldPressed || (millis() - t0) > 10000) {
      oldPressed = pressed;
      t0 = millis();
      float batt = 3.7; // V, TODO
      int b = batt * 100.0f;
      uint8_t payload[] = {
        highByte(b), lowByte(b),
        (uint8_t) pressed
      };
      printf("batt = %.2f, pressed = %d\n", batt, pressed);
      printf("publish to %s\n", topicStr);
      mqtt.publish(topicStr, payload, sizeof(payload));
    }
    delay(1);
  } else {
    int result = mqtt.connect(); // calls client.connect()
    if (result != 0) {
      Serial.println(mqtt.connectErrorString(result));
      delay(3000);
    }
  }
}
