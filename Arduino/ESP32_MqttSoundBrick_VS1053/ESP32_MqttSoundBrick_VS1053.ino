// Copyright (c) 2024 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// Based on https://github.com/adafruit/Adafruit_VS1053_Library licensed under BSD license.

#include <SPI.h>
#include <SD.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>
#include <Adafruit_VS1053.h>

#define VS1053_RESET -1 // not used
#define VS1053_CS 32 // chip select
#define VS1053_DCS 33 // data or command select
#define CARDCS 14 // card chip select
#define VS1053_DREQ 15 // data request

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "test.mosquitto.org"; // TODO
const int port = 8883;
const char *topicStr = "bricks/0000-0000/target"; // TODO

Adafruit_VS1053_FilePlayer musicPlayer =
  Adafruit_VS1053_FilePlayer(VS1053_RESET, VS1053_CS, VS1053_DCS, VS1053_DREQ, CARDCS);

WiFiClientSecure client;
Adafruit_MQTT_Client mqtt(&client, host, port);
Adafruit_MQTT_Subscribe topic(&mqtt, topicStr);
//Adafruit_MQTT_Publish topic = Adafruit_MQTT_Publish(&mqtt, topicStr);

void printDirectory(File dir, int numTabs) {
  File entry =  dir.openNextFile();
  while(entry != 0) {
    for (uint8_t i = 0; i < numTabs; i++) {
      Serial.print('\t');
    }
    Serial.print(entry.name());
    if (entry.isDirectory()) {
      Serial.println("/");
      printDirectory(entry, numTabs+1);
    } else {
      Serial.print("\t\t");
      Serial.println(entry.size(), DEC);
    }
    entry.close();
    entry =  dir.openNextFile();
  }
}

void handleMessage(char *buf, uint16_t len) {
  Serial.print("handleMessage");
  if (len == 4) {
    short track = (buf[0] << 8) | buf[1];
    short vol = (buf[2] << 8) | buf[3]; //min(max(0, buf[1]), 10);
    char track_name[8 + 1]; // "/002.mp3"
    int len = sprintf(track_name, "/%03d.mp3", track);
    track_name[len] = '\0';
    musicPlayer.setVolume(10 - vol, 10 - vol); // lower = louder
    musicPlayer.playFullFile(track_name);
    Serial.print(", played ");
    Serial.println(track_name);
  } else {
    Serial.print(", unexpected payload, len = ");
    Serial.println(len);
  }
}

void setup() {
  Serial.begin(115200);
  //pinMode(8, INPUT_PULLUP); // disable BLE / LoRa
  if (!musicPlayer.begin()) {
      Serial.println(F("Couldn't find VS1053."));
      while (1) {}
  }
  Serial.println(F("VS1053 found"));
  
  if (!SD.begin(CARDCS)) {
    Serial.println(F("SD failed, or not present"));
    while (1) {}
  }
  Serial.println("SD OK!");
  //printDirectory(SD.open("/"), 0);

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
  //client.setCACert(mqtt_broker_cert_pem);
  topic.setCallback(handleMessage);
  if (!mqtt.subscribe(&topic)) {
    Serial.println(F("Couldn't subscribe."));
    while (1) {}
  }
  Serial.print(F("Subscribed to "));
  Serial.println(topicStr);
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
