// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// $ cd Java
// $ ./setup.sh
// $ ./clean.sh
// $ ./compile.sh
// $ java -cp ./src:target:lib/minimal-json-0.9.5.jar:lib/org.eclipse.paho.client.mqttv3-1.2.3.jar Bricks

// Design principles:
// - keep it simple to use
//     - physical brick => access
//     - no type casts, no generics 
//     - getValue() remains constant
//       until waitForUpdate()
//     - mock mode for quick prototyping
//     - single package namespace (?)
// - single responsibility
//     - transport x encoding x brick type
// - minimize dependencies
//     - provide a single jar library
//     - use as few libraries as possible
//     - provide server/client certs in code

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

// TODO: import ch.fhnw.imvs.bricks.*;

import ch.fhnw.imvs.bricks.core.Proxy;
import ch.fhnw.imvs.bricks.http.HttpProxy;
import ch.fhnw.imvs.bricks.mock.MockProxy;
import ch.fhnw.imvs.bricks.mqtt.AnyMqttProxy;
import ch.fhnw.imvs.bricks.mqtt.TtnMqttProxy;
import ch.fhnw.imvs.bricks.mqtt.BleMqttProxy;
import ch.fhnw.imvs.bricks.sensors.ButtonBrick;
import ch.fhnw.imvs.bricks.actuators.BuzzerBrick;
import ch.fhnw.imvs.bricks.sensors.DistanceBrick;
import ch.fhnw.imvs.bricks.sensors.HumiTempBrick;
import ch.fhnw.imvs.bricks.actuators.LedBrick;
import ch.fhnw.imvs.bricks.actuators.LcdDisplayBrick;

public final class Bricks {
    private Bricks() {}

    private static final String BUTTON_BRICK_ID = "0000-0002";
    private static final String BUZZER_BRICK_ID = "0000-0006";
    private static final String DISTANCE_BRICK_ID = "0000-0003";
    private static final String HUMITEMP_BRICK_ID = "0000-0001";
    private static final String HUMITEMP_BRICK_0_ID = HUMITEMP_BRICK_ID;
    private static final String HUMITEMP_BRICK_1_ID = "0000-0007";
    private static final String HUMITEMP_BRICK_2_ID = "0000-0004";
    private static final String LCDDISPLAY_BRICK_ID = "0000-0005";
    private static final String LED_BRICK_ID = "0000-0000";

    private static void runDoorbellExample(Proxy proxy) {
        ButtonBrick buttonBrick = ButtonBrick.connect(proxy, BUTTON_BRICK_ID);
        BuzzerBrick buzzerBrick = BuzzerBrick.connect(proxy, BUZZER_BRICK_ID);
        while (true) {
            boolean pressed = buttonBrick.isPressed();
            String time = buttonBrick.getTimestampIsoUtc();
            System.out.println(time + ", " +  pressed);
            buzzerBrick.setEnabled(pressed);
            proxy.waitForUpdate();
        }
    }

    private static void runLoggingExample(Proxy proxy) {
        HumiTempBrick brick = HumiTempBrick.connect(proxy, HUMITEMP_BRICK_ID);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("log.csv", true); // append
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            String time = brick.getTimestampIsoUtc();
            double temp = brick.getTemperature();
            double humi = brick.getHumidity();
            String line = String.format(Locale.US, "%s, %.2f, %.2f\n", time, temp, humi);
            System.out.print(line);
            try {
                fileWriter.append(line);
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            proxy.waitForUpdate();
        }
    }

    private static void runLoggingArrayExample(Proxy proxy) {
        HumiTempBrick[] bricks = new HumiTempBrick[3];
        bricks[0] = HumiTempBrick.connect(proxy, HUMITEMP_BRICK_0_ID);
        bricks[1] = HumiTempBrick.connect(proxy, HUMITEMP_BRICK_1_ID);
        bricks[2] = HumiTempBrick.connect(proxy, HUMITEMP_BRICK_2_ID);
        while (true) {
            for (HumiTempBrick brick : bricks) {
                String id = brick.getID();
                String time = brick.getTimestampIsoUtc();
                float batt = brick.getBatteryLevel();
                double temp = brick.getTemperature();
                double humi = brick.getHumidity();
                String line = String.format(Locale.US, "%s, %s, %d, %.2f, %.2f", id, time, batt, temp, humi);
                System.out.println(line);
            }
            proxy.waitForUpdate();
        }
    }

    private static void runMonitoringExample(Proxy proxy) {
        HumiTempBrick humiTempBrick = HumiTempBrick.connect(proxy, HUMITEMP_BRICK_ID);
        LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(proxy, LCDDISPLAY_BRICK_ID);
        while (true) {
            double temp = humiTempBrick.getTemperature();
            String time = humiTempBrick.getTimestampIsoUtc();
            String line = String.format(Locale.US, "%s, %.2f\n", time, temp);
            System.out.print(line);
            displayBrick.setDoubleValue(temp);
            proxy.waitForUpdate();
        }
    }

    private static void runParkingExample(Proxy proxy) {
        DistanceBrick distBrick = DistanceBrick.connect(proxy, DISTANCE_BRICK_ID);
        LedBrick ledBrick = LedBrick.connect(proxy, LED_BRICK_ID);
        while (true) {
            int dist = distBrick.getDistance(); // cm
            String time = distBrick.getTimestampIsoUtc();
            Color color = dist < 200 ? Color.RED : Color.GREEN;
            String line = String.format(Locale.US, "%s, %d, %s\n", time, dist, color);
            System.out.print(line);
            ledBrick.setColor(color);
            proxy.waitForUpdate();
        }  
    }

    public static void main(String args[]) {
        final String BASE_URL = "https://brick.li";
        final String USAGE = "usage: java Bricks http|mqtt|mock|ttn|ble d|l|a|m|p";
        if (args.length == 2) {
            Proxy proxy = null;
            if ("http".equals(args[0])) {
                proxy = HttpProxy.fromConfig(BASE_URL);
            } else if ("mqtt".equals(args[0])) {
                proxy = AnyMqttProxy.fromConfig(BASE_URL);
            } else if ("mock".equals(args[0])) {
                proxy = MockProxy.fromConfig(BASE_URL);
            } else if ("ttn".equals(args[0])) {
                proxy = TtnMqttProxy.fromConfig(BASE_URL);
            } else if ("ble".equals(args[0])) {
                proxy = BleMqttProxy.fromConfig(BASE_URL);
            } else {
                System.out.println(USAGE);
                System.exit(-1);
            }
            if ("d".equals(args[1])) {
                runDoorbellExample(proxy);
            } else if ("l".equals(args[1])) {
                runLoggingExample(proxy);
            } else if ("a".equals(args[1])) {
                runLoggingArrayExample(proxy);    
            } else if ("m".equals(args[1])) {
                runMonitoringExample(proxy);
            } else if ("p".equals(args[1])) {
                runParkingExample(proxy);    
            } else {
                System.out.println(USAGE);
                System.exit(-1);
            }
        } else {
            System.out.println(USAGE);
            System.exit(-1);
        }
    }
}