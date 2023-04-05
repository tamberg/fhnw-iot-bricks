// Copyright (c) 2021 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

#include <ESP8266WiFi.h>
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"

#define MQTT_CONN_KEEPALIVE 30

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "test.mosquitto.org"; // TODO
const int port = 8883;
const char *topicStr = "bricks/0000-0008/target"; // TODO
const int servoPin = 12; // Grove adapter D12

int oldPos = 0;
BearSSL::WiFiClientSecure client;
Adafruit_MQTT_Client mqtt(&client, host, port);
Adafruit_MQTT_Subscribe topic(&mqtt, topicStr);

// based on https://www.adeept.com/blog/tutorials/control-servo-to-rotate-180-degrees.html
void setPos(int pos) { // 0 - 180
  //Serial.print("setPos, pos = ");
  //Serial.println(pos);
  int w = map(180 - pos, 0, 180, 500, 2348);
  digitalWrite(servoPin,HIGH);
  delayMicroseconds(w);
  digitalWrite(servoPin,LOW);
  //delayMicroseconds(20000 - w);
  delayMicroseconds(3000 - w);
  delay(17); // ms
}

void handleMessage(char *buf, uint16_t len) {
  if (len == 2) {
    int pos = (int) ((buf[0] << 8) | buf[1]);
    Serial.print("handleMessage, pos = ");
    Serial.println(pos);
    pos = min(max(pos, 0), 180); // prevents buzzing
    if (pos < oldPos) {
      for (int i = oldPos; i > pos; i--) {
        setPos(i);
      }
    } else if (oldPos < pos) {
      for (int i = oldPos; i < pos; i++) {
        setPos(i);
      }
    }
    oldPos = pos;
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(servoPin,OUTPUT);
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
    Serial.println("Connected");
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
