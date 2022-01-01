// Copyright (c) 2021 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>
#include <TM1637.h>

#define MQTT_CONN_KEEPALIVE 30

#define CLK 5 // Grove adapter I2C_1 or _2 used as D6
#define DIO 4 // Grove D7

TM1637 tm1637(CLK, DIO);

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "test.mosquitto.org"; // TODO
const int port = 8883;
const char *topicStr = "bricks/0000-0005/target"; // TODO
const int buzzerPin = 5;

BearSSL::WiFiClientSecure client;
Adafruit_MQTT_Client mqtt(&client, host, port);
Adafruit_MQTT_Subscribe topic(&mqtt, topicStr);

void displayValue(float f, int d) {
  if (d == 0) {
    int a = f / 100;
    int b = (int) f % 100;
    tm1637.display(0, a / 10);
    tm1637.display(1, a % 10);
    tm1637.display(2, b / 10);
    tm1637.display(3, b % 10);
    tm1637.point(POINT_OFF);
  } else if (d == 2) {
    int a = f;
    int b = (int) (f * 100.0f) % 100;
    tm1637.display(0, a / 10);
    tm1637.display(1, a % 10);
    tm1637.display(2, b / 10);
    tm1637.display(3, b % 10);
    tm1637.point(POINT_ON);
  } else {
    // error
  }
}

void handleMessage(char *buf, uint16_t len) {
  if (len == 3) {
    int d = buf[0];
    short i = (buf[1] << 8) | (buf[2] << 0);
    float f = 1.0f * i / pow(10, d);
    displayValue(f, d);
  }
}

void setup() {
  Serial.begin(115200);
  tm1637.set(BRIGHT_TYPICAL); // or BRIGHT_DARKEST, ...
  tm1637.init();
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
