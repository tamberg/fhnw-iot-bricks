// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// LoRaWAN-connected DHT11 temperature sensor

// https://github.com/tamberg/fhnw-iot/wiki/Feather-nRF52840-Express
// https://github.com/tamberg/fhnw-iot/wiki/FeatherWing-RFM95W
// https://github.com/tamberg/fhnw-iot/wiki/Grove-Sensors#ultrasonic-ranger
// https://github.com/tamberg/fhnw-iot/wiki/Grove-Adapters#pinout

// DHT11-specific code based on example code written by ladyada, public domain

// LoRaWAN-specific code based on LMIC example code
// Copyright (c) 2018 Terry Moore, MCCI
// Copyright (c) 2015 Thomas Telkamp and Matthijs Kooijman

// Permission is hereby granted, free of charge, to anyone obtaining
// a copy of this document and accompanying files, to do whatever 
// they want with them without any restriction, including, but not 
// limited to, copying, modification and redistribution.
// NO WARRANTY OF ANY KIND IS PROVIDED.

#include <lmic.h>
#include <hal/hal.h>
#include <SPI.h>
#include "Ultrasonic.h"

#define ULTRASONIC_PIN A4

Ultrasonic ultrasonic(ULTRASONIC_PIN);

// https://console.ttn.opennetworkinfrastructure.org/applications/fhnw-iot/devices/fhnw-iot-4

// LoRaWAN NwkSKey, network session key
static const u1_t PROGMEM NWKSKEY[16] = // TODO: change
  { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
// LoRaWAN AppSKey, application session key
static const u1_t PROGMEM APPSKEY[16] = // TODO: change
  { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

// LoRaWAN end-device address (DevAddr), see http://thethingsnetwork.org/wiki/AddressSpace
static const u4_t DEVADDR = 0x00000000; // TODO: change
                            
// Or DISABLE_JOIN in arduino-lmic/project_config/lmic_project_config.h
void os_getArtEui (u1_t* buf) { }
void os_getDevEui (u1_t* buf) { }
void os_getDevKey (u1_t* buf) { }

static uint8_t message[2];
static osjob_t sendjob;

// Subject to duty cycle limitations
const unsigned TX_INTERVAL = 5 * 60;

// Pin mapping, see https://github.com/tamberg/fhnw-iot/wiki/FeatherWing-RFM95W
// Feather nRF52840 Express with FeatherWing RFM95W
const lmic_pinmap lmic_pins = {
  .nss = 5, // E = CS
  .rxtx = LMIC_UNUSED_PIN,
  .rst = 6, // D = RST
  .dio = {
    10, // B = DIO0 = IRQ 
    9, // C = DIO1
    LMIC_UNUSED_PIN
  },
};

void updateMeasurement() {
  int range = ultrasonic.MeasureInCentimeters();
  printf("%d cm\n", range);
  if (isnan(range)) {
    message[0] = 0x00;
    message[1] = 0x00;
  } else {
    message[0] = highByte(range);
    message[1] = lowByte(range);
  }
}

void scheduleNextSend();

void sendMessage(osjob_t* j){
  if ((LMIC.opmode & OP_TXRXPEND) == 0) {
    updateMeasurement();
    LMIC_setTxData2(1, message, sizeof(message), 0);
  } else {
    scheduleNextSend();
  }
}

void scheduleNextSend() {
  os_setTimedCallback(&sendjob, os_getTime() + sec2osticks(TX_INTERVAL), sendMessage);
}

void onEvent (ev_t ev) {
  if (ev == EV_TXCOMPLETE) {
    Serial.println(F("EV_TXCOMPLETE"));
    scheduleNextSend();
  }
  // TODO: error handling?
}

void setup() {
  Serial.begin(115200);

  // Init LMIC
  delay(100); // Per sample code on RF_95 test
  os_init(); // LMIC specific
  LMIC_reset();

  // Copy keys from PROGMEM to RAM
  uint8_t appskey[sizeof(APPSKEY)];
  uint8_t nwkskey[sizeof(NWKSKEY)];
  memcpy_P(appskey, APPSKEY, sizeof(APPSKEY));
  memcpy_P(nwkskey, NWKSKEY, sizeof(NWKSKEY));
  LMIC_setSession (0x13, DEVADDR, nwkskey, appskey);

  // EU region specific
  LMIC_setupChannel(0, 868100000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(1, 868300000, DR_RANGE_MAP(DR_SF12, DR_SF7B), BAND_CENTI); // g-band
  LMIC_setupChannel(2, 868500000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(3, 867100000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(4, 867300000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(5, 867500000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(6, 867700000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(7, 867900000, DR_RANGE_MAP(DR_SF12, DR_SF7),  BAND_CENTI); // g-band
  LMIC_setupChannel(8, 868800000, DR_RANGE_MAP(DR_FSK,  DR_FSK),  BAND_MILLI); // g2-band

  LMIC_setLinkCheckMode(0);
  LMIC.dn2Dr = DR_SF9;
  LMIC_setDrTxpow(DR_SF7,14);

  sendMessage(&sendjob);
}

void loop() {
  os_runloop_once(); // LMIC specific
}
