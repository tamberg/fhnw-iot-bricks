#include <ESP8266WiFi.h> // v2.4.2
#include <ESP8266MQTTClient.h> // v1.0.4
#include "DHTesp.h"

const int dhtPin = 5; // Grove adapter I2C_1 or _2 used as D6
const DHTesp::DHT_MODEL_t dhtModel = DHTesp::DHT11;

DHTesp dht;

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "mqtt://test.mosquitto.org"; // TODO
const char *topicStr = "bricks/0000-0001/actual"; // TODO

MQTTClient client;
volatile int connected = 0;

void handleConnected() {
  Serial.println("Connected to broker");
  connected = 1;
}

void setup() {
  Serial.begin(115200);
  dht.setup(dhtPin, dhtModel);
  Serial.print("\nConnecting to network ");
  Serial.println(ssid);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100); // keeps watchdog happy
  }
  Serial.print("Connected to network, local IP = "); 
  Serial.println(WiFi.localIP());

  client.onConnect(handleConnected);
  client.begin(host);
}

void loop() {
  client.handle();
  if (connected) {
    // Readings take about 250 ms, may be up to 2 s old
    float batt = 3.7; // V, TODO
    float humi = dht.getHumidity(); // %
    float temp = dht.getTemperature(); // *C
    if (!isnan(humi) && !isnan(temp)) {
      int b = batt * 100.0f;
      int h = humi * 100.0f;
      int t = temp * 100.0f;
      char payload[] = { // TODO: fix "null bytes cut off payload"
        highByte(b), lowByte(b),
        highByte(h), lowByte(h),
        highByte(t), lowByte(t),
        '\0'
      };
      printf("measure humi = %f, temp = %f\n", humi, temp);
      printf("publish to %s\n", topicStr);
      client.publish(topicStr, payload);
    }
    delay(1000);
  }
}
