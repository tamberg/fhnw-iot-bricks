// IoT Bricks HumiTemp BLE peripheral. Copyright (c) Thomas Amberg, FHNW

// Based on https://github.com/adafruit/Adafruit_nRF52_Arduino
// /tree/master/libraries/Bluefruit52Lib/examples/Peripheral
// Copyright (c) Adafruit.com, all rights reserved.

// Licensed under the MIT license, see LICENSE or
// https://choosealicense.com/licenses/mit/

#include <bluefruit.h>
#include "Adafruit_SHT31.h"

// Custom peripheral, use 128-bit UUIDs
// 807c012c-ecbd-4eb8-8c4e-17097876f8bb GUID =>
// 807cxxxx-ecbd-4eb8-8c4e-17097876f8bb Base UUID =>
// 807c0001-ecbd-4eb8-8c4e-17097876f8bb HumiTemp Service
// 807c0002-ecbd-4eb8-8c4e-17097876f8bb   Humidity Measurement Chr. [R, N]
// 807c0003-ecbd-4eb8-8c4e-17097876f8bb   Temperature Measurement Chr. [R, N]

// The arrays below are ordered "least significant byte first":
uint8_t const iotBrickServiceUuid[] = {
  0x62, 0xbd, 0xde, 0x2c, 0x65, 0x26, 0x40, 0xdb, 
  0xb5, 0x44, 0x5a, 0x73, 0x01, 0x00, 0x2d, 0xbd };
uint8_t const humiTempServiceUuid[] = {
  0xbb, 0xf8, 0x76, 0x78, 0x09, 0x17, 0x4e, 0x8c,
  0xb8, 0x4e, 0xbd, 0xec, 0x01, 0x00, 0x7c, 0x80 };
uint8_t const humidityMeasurementCharacteristicUuid[] = {
  0xbb, 0xf8, 0x76, 0x78, 0x09, 0x17, 0x4e, 0x8c,
  0xb8, 0x4e, 0xbd, 0xec, 0x02, 0x00, 0x7c, 0x80 };
uint8_t const temperatureMeasurementCharacteristicUuid[] = {
  0xbb, 0xf8, 0x76, 0x78, 0x09, 0x17, 0x4e, 0x8c,
  0xb8, 0x4e, 0xbd, 0xec, 0x03, 0x00, 0x7c, 0x80 };

Adafruit_SHT31 sht31 = Adafruit_SHT31();
BLEService iotBrickService = BLEService(iotBrickServiceUuid);
BLEService humiTempService = BLEService(humiTempServiceUuid);
BLECharacteristic humidityMeasurementCharacteristic = BLECharacteristic(humidityMeasurementCharacteristicUuid);
BLECharacteristic temperatureMeasurementCharacteristic = BLECharacteristic(temperatureMeasurementCharacteristicUuid);

void connectedCallback(uint16_t connectionHandle) {
  char centralName[32] = { 0 };
  BLEConnection *connection = Bluefruit.Connection(connectionHandle);
  connection->getPeerName(centralName, sizeof(centralName));
  Serial.print(connectionHandle);
  Serial.print(", connected to ");
  Serial.print(centralName);
  Serial.println();
}

void disconnectedCallback(uint16_t connectionHandle, uint8_t reason) {
  Serial.print(connectionHandle);
  Serial.print(" disconnected, reason = ");
  Serial.println(reason); // see https://github.com/adafruit/Adafruit_nRF52_Arduino
  // /blob/master/cores/nRF5/nordic/softdevice/s140_nrf52_6.1.1_API/include/ble_hci.h
  Serial.println("Advertising ...");
}

void cccdCallback(uint16_t connectionHandle, BLECharacteristic* characteristic, uint16_t cccdValue) {
  if (characteristic->uuid == humidityMeasurementCharacteristic.uuid) {
    Serial.print("Humidity Measurement 'Notify', ");
  } else if (characteristic->uuid == temperatureMeasurementCharacteristic.uuid) {
    Serial.print("Temperature Measurement 'Notify', ");
  }
  Serial.println(characteristic->notifyEnabled() ? "enabled" : "disabled");
}

void setupHumiTempService() {
  humiTempService.begin(); // Must be called before calling .begin() on its characteristics

  humidityMeasurementCharacteristic.setProperties(CHR_PROPS_READ | CHR_PROPS_NOTIFY);
  humidityMeasurementCharacteristic.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  humidityMeasurementCharacteristic.setFixedLen(2);
  humidityMeasurementCharacteristic.setCccdWriteCallback(cccdCallback);  // Optionally capture CCCD updates
  humidityMeasurementCharacteristic.begin();

  temperatureMeasurementCharacteristic.setProperties(CHR_PROPS_READ | CHR_PROPS_NOTIFY);
  temperatureMeasurementCharacteristic.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  temperatureMeasurementCharacteristic.setFixedLen(2);
  temperatureMeasurementCharacteristic.setCccdWriteCallback(cccdCallback);  // Optionally capture CCCD updates
  temperatureMeasurementCharacteristic.begin();
}

void startAdvertising() {
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();

  Bluefruit.Advertising.addService(iotBrickService);
  Bluefruit.Advertising.addService(humiTempService);
  Bluefruit.Advertising.addName();

  // See https://developer.apple.com/library/content/qa/qa1931/_index.html   
  const int fastModeInterval = 32; // * 0.625 ms = 20 ms
  const int slowModeInterval = 244; // * 0.625 ms = 152.5 ms
  const int fastModeTimeout = 30; // s
  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(fastModeInterval, slowModeInterval);
  Bluefruit.Advertising.setFastTimeout(fastModeTimeout);
  // 0 = continue advertising after fast mode, until connected
  Bluefruit.Advertising.start(0);
  Serial.println("Advertising ...");
}

void setup() {
  Serial.begin(115200);
  while (!Serial) { delay(10); } // only if usb connected
  Serial.println("Setup");

  sht31.begin(0x44);
  sht31.heater(true);

  Bluefruit.begin();
  Bluefruit.setName("nRF52840");
  Bluefruit.Periph.setConnectCallback(connectedCallback);
  Bluefruit.Periph.setDisconnectCallback(disconnectedCallback);

  iotBrickService.begin();
  setupHumiTempService();
  startAdvertising();
}

void loop() {
  if (Bluefruit.connected()) {
    float h = sht31.readHumidity();
    int h2 = h * 100.0; // fixed precision
    uint8_t h2HiByte = (uint8_t) (h2 >> 8);
    uint8_t h2LoByte = (uint8_t) h2;
    uint8_t humidityData[2] = { h2HiByte, h2LoByte };
    if (humidityMeasurementCharacteristic.notify(humidityData, sizeof(humidityData))) {
      Serial.print("Notified, humidity = ");
      Serial.println(h);
    } else {
      Serial.println("Notify not set, or not connected");
    }
  }
  delay(1000); // ms
}
