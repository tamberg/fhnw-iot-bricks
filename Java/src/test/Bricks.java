// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// $ cd Java
// $ curl -Lo lib/minimal-json-0.9.5.jar https://github.com/ralfstx/minimal-json/\
//   releases/download/0.9.5/minimal-json-0.9.5.jar
// $ curl -Lo lib/org.eclipse.paho.client.mqttv3-1.2.3.jar \
//   https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/\
//   org.eclipse.paho.client.mqttv3/1.2.3/org.eclipse.paho.client.mqttv3-1.2.3.jar
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
import ch.fhnw.imvs.bricks.mqtt.MqttProxy;
import ch.fhnw.imvs.bricks.sensors.ButtonBrick;
import ch.fhnw.imvs.bricks.actuators.BuzzerBrick;
import ch.fhnw.imvs.bricks.sensors.DistanceBrick;
import ch.fhnw.imvs.bricks.sensors.HumiTempBrick;
import ch.fhnw.imvs.bricks.actuators.LedBrick;
import ch.fhnw.imvs.bricks.actuators.LcdDisplayBrick;

public final class Bricks {
    private Bricks() {}

    private static final String BUTTON_ID = "0000-0002";
    private static final String BUZZER_ID = "0000-0006";
    private static final String DISTANCE_ID = "0000-0003";
    private static final String HUMITEMP_ID = "0000-0001";
    private static final String HUMITEMP_0_ID = HUMITEMP_ID;
    private static final String HUMITEMP_1_ID = "0000-0007";
    private static final String HUMITEMP_2_ID = "0000-0004";
    private static final String LCDDISPLAY_ID = "0000-0005";
    private static final String LED_ID = "0000-0000";

    private static void runDoorbellExample(Proxy proxy) {
        ButtonBrick buttonBrick = ButtonBrick.connect(proxy, BUTTON_ID);
        BuzzerBrick buzzerBrick = BuzzerBrick.connect(proxy, BUZZER_ID);
        while (true) {
            boolean pressed = buttonBrick.isPressed();
            String time = buttonBrick.getTimestampIsoUtc();
            System.out.println(time + ", " +  pressed);
            buzzerBrick.setEnabled(pressed);
            proxy.waitForUpdate();
        }
    }

    private static void runLoggingExample(Proxy proxy) {
        HumiTempBrick brick = HumiTempBrick.connect(proxy, HUMITEMP_ID);
        FileWriter fileWriter = null;
        String title = "Timestamp (UTC)\tTemperature\tHumidity\n";
        System.out.print(title);
        try {
            fileWriter = new FileWriter("log.csv", true); // append
            fileWriter.append(title);
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
        bricks[0] = HumiTempBrick.connect(proxy, HUMITEMP_0_ID);
        bricks[1] = HumiTempBrick.connect(proxy, HUMITEMP_1_ID);
        bricks[2] = HumiTempBrick.connect(proxy, HUMITEMP_2_ID);
        while (true) {
            for (HumiTempBrick brick : bricks) {
                String id = brick.getID();
                String time = brick.getTimestampIsoUtc();
                double temp = brick.getTemperature();
                double humi = brick.getHumidity();
                String line = String.format(Locale.US, "%s, %s, %.2f, %.2f", id, time, temp, humi);
                System.out.println(line);
            }
            proxy.waitForUpdate();
        }
    }

    private static void runMonitoringExample(Proxy proxy) {
        HumiTempBrick humiTempBrick = HumiTempBrick.connect(proxy, HUMITEMP_ID);
        LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(proxy, LCDDISPLAY_ID);
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
        DistanceBrick distBrick = DistanceBrick.connect(proxy, DISTANCE_ID);
        LedBrick ledBrick = LedBrick.connect(proxy, LED_ID);
        while (true) {
            int dist = distBrick.getDistance(); // cm
            String time = distBrick.getTimestampIsoUtc();
            Color color = dist < 200 ? Color.RED : Color.GREEN;
            String line = String.format(Locale.US, "%s, %.2d, %s\n", time, dist, color);
            System.out.print(line);
            ledBrick.setColor(color);
            proxy.waitForUpdate();
        }  
    }

    public static void main(String args[]) {
        final String BASE_URL = "https://brick.li";
        final String USAGE = "usage: java Bricks http|mqtt|mock d|l|a|m";
        if (args.length == 2) {
            Proxy proxy = null;
            if ("http".equals(args[0])) {
                proxy = HttpProxy.fromConfig(BASE_URL);
            } else if ("mqtt".equals(args[0])) {
                proxy = MqttProxy.fromConfig(BASE_URL);
            } else if ("mock".equals(args[0])) {
                proxy = MockProxy.fromConfig(BASE_URL);
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