#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>
#include <TM1637.h>

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

void displayValue(float f) {
  int a = f;
  int a1 = a / 10;
  int a0 = a % 10;
  int b = f * 100.0f;
  b = b % 100;
  int b1 = b / 10;
  int b0 = b % 10;
  tm1637.point(POINT_ON); // or POINT_OFF
  tm1637.display(0, a1);
  tm1637.display(1, a0);
  tm1637.display(2, b1);
  tm1637.display(3, b0);
}

void handleMessage(char *buf, uint16_t len) {
  if (len == 2) {
    short i = (buf[0] << 8) | (buf[1] << 0);
    float f = i / 100.0f;
    displayValue(f);
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
