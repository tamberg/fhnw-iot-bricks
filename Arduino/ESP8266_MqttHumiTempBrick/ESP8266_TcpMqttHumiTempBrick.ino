#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>
#include <DHTesp.h>

const int dhtPin = 5; // Grove adapter I2C_1 or _2 used as D6
const DHTesp::DHT_MODEL_t dhtModel = DHTesp::DHT11;

DHTesp dht;

const char *ssid = "MY_SSID"; // TODO
const char *password = "MY_PASSWORD"; // TODO
const char *host = "test.mosquitto.org"; // TODO
const int port = 8883;
const char *topicStr = "bricks/0000-0001/actual"; // TODO

BearSSL::WiFiClientSecure client;
Adafruit_MQTT_Client mqtt(&client, host, port);

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
  client.setInsecure(); // no cert validation
}

void loop() {
  if (mqtt.connected()) {
    // Readings take about 250 ms, may be up to 2 s old
    float batt = 3.7; // V, TODO
    float humi = dht.getHumidity(); // %
    float temp = dht.getTemperature(); // *C
    if (!isnan(humi) && !isnan(temp)) {
      int b = batt * 100.0f;
      int h = humi * 100.0f;
      int t = temp * 100.0f;
      uint8_t payload[] = {
        highByte(b), lowByte(b),
        highByte(h), lowByte(h),
        highByte(t), lowByte(t)
      };
      printf("batt = %.2f, humi = %.2f, temp = %.2f\n", batt, humi, temp);
      printf("publish to %s\n", topicStr);
      mqtt.publish(topicStr, payload, sizeof(payload));
    }
    delay(1000);
  } else {
    int result = mqtt.connect(); // calls client.connect()
    if (result != 0) {
      Serial.println(mqtt.connectErrorString(result));
      delay(3000);
    }
  }
}
