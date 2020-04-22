// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// $ cd Java
// $ curl -Lo lib/minimal-json-0.9.5.jar https://github.com/ralfstx/minimal-json/\
//   releases/download/0.9.5/minimal-json-0.9.5.jar
// $ curl -Lo lib/org.eclipse.paho.client.mqttv3-1.2.3.jar \
//   https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/\
//   org.eclipse.paho.client.mqttv3/1.2.3/org.eclipse.paho.client.mqttv3-1.2.3.jar
// $ javac -cp .:src:lib/org.eclipse.paho.client.mqttv3-1.2.3.jar:lib/minimal-json-0.9.5.jar src/Bricks.java
// $ java -ea -cp src:lib/org.eclipse.paho.client.mqttv3-1.2.3.jar:lib/minimal-json-0.9.5.jar Bricks

// Design principles:
// - keep it simple to use
//     - physical brick => access
//     - no type casts, no generics 
//     - getValue() remains constant
//       until waitForUpdate()
//     - mock mode for quick prototyping
// - single responsibility
//     - transport x encoding x brick type
// - minimize dependencies
//     - provide a single jar library
//     - use as few libraries as possible
//     - provide server/client certs in code

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.UnsupportedOperationException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

// TODO
// package ch.fhnw.imvs.bricks.core; // Proxy, Brick
// package ch.fhnw.imvs.bricks.mock; // MockProxy (keep in core?)
// package ch.fhnw.imvs.bricks.mqtt; // MqttProxy, MqttConfig, MqttService
// package ch.fhnw.imvs.bricks.sensors; // HumiTempBrick, ButtonBrick, ...
// package ch.fhnw.imvs.bricks.actuators; // BuzzerBrick, LedBrick, ...

/* public */ final class MqttService {
    public MqttService() {}

    private IMqttClient client = null;

    public void init(String host, String username, String password) {
        try {
            String hostURI = "tcp://" + host;
            String clientID = ""; // defaults to mqtt-client-PROCESS_ID
            client = new MqttClient(hostURI, clientID, new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }    
    }

    public void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) {
        try {
            System.out.println("subscribe: topic = " + topic);
            client.subscribe(topic, 1, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, payload, 1, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

/* public */ final class MqttConfig { // TODO: rename MqttProxyConfig? Ttn...
    private MqttConfig() {}

    private static final String BUTTON_ID = "0000-0002";
    private static final String BUZZER_ID = "0000-0006";
    private static final String HUMITEMP_ID = "0000-0001";
    private static final String HUMITEMP_0_ID = HUMITEMP_ID;
    private static final String HUMITEMP_1_ID = "0000-0003";
    private static final String HUMITEMP_2_ID = "0000-0004";
    private static final String LCDDISPLAY_ID = "0000-0005";
    private static final String LED_ID = "0000-0000";

    private static final String TTN_APP_ID = "fhnw-iot-bricks";
    private static final String TTN_APP_ACCESS_KEY = "<AppAccessKey>";
    private static final String TTN_HOST = "eu.thethings.network";

    private static final String HOST = "test.mosquitto.org"; // TODO: TTN_HOST
    private static final String USERNAME = null; // TODO: TTN_APP_ID
    private static final String PASSWORD = null; // TODO: TTN_APP_ACCESS_KEY

    HashMap<String, String> pubTopics;
    HashMap<String, String> subTopics;

    public String getHost() {
        return HOST;
    }

    public String getUsername() {
        return USERNAME;
    }

    public String getPassword() {
        return PASSWORD;
    }

    public String getSubscribeTopic(String brickID) { // TODO: move to MqttBrickConfig?
        String topic = subTopics.get(brickID);
        if (topic == null) {
            throw new IllegalArgumentException(brickID);
        }
        return topic;
    }

    public String getPublishTopic(String brickID) {
        String topic = pubTopics.get(brickID);
        if (topic == null) {
            throw new IllegalArgumentException(brickID);
        }
        return topic;
    }

    private void init(String configHost) {
        // TODO: get from host or use generic pattern
        subTopics = new HashMap<String, String>();
        subTopics.put(BUTTON_ID, TTN_APP_ID + "/devices/" + BUTTON_ID + "/up");
        subTopics.put(BUZZER_ID, TTN_APP_ID + "/devices/" + BUZZER_ID + "/up");
        subTopics.put(HUMITEMP_0_ID, TTN_APP_ID + "/devices/" + HUMITEMP_0_ID + "/up");
        subTopics.put(HUMITEMP_1_ID, TTN_APP_ID + "/devices/" + HUMITEMP_1_ID + "/up");
        subTopics.put(HUMITEMP_2_ID, TTN_APP_ID + "/devices/" + HUMITEMP_2_ID + "/up");
        subTopics.put(LCDDISPLAY_ID, TTN_APP_ID + "/devices/" + LCDDISPLAY_ID + "/up");
        subTopics.put(LED_ID, TTN_APP_ID + "/devices/" + LED_ID + "/up");
        pubTopics = new HashMap<String, String>();
        pubTopics.put(BUZZER_ID, TTN_APP_ID + "/devices/" + BUZZER_ID + "/down");
        pubTopics.put(LED_ID, TTN_APP_ID + "/devices/" + LED_ID + "/down");
    }

    public static MqttConfig fromHost(String configHost) {
        MqttConfig config = new MqttConfig();
        config.init(configHost);
        return config;
    }
}

/* public */ abstract class Proxy {
    abstract protected void sync(Brick brick);
    abstract void connectBrick(Brick brick);
    abstract public void waitForUpdate();
}

/* public */ final class HttpProxy extends Proxy {
    // TODO: implementation
    private HttpProxy() {
        bricks = new ArrayList<Brick>();
    }

    private final List<Brick> bricks;

    @Override
    void connectBrick(Brick brick) {
        bricks.add(brick);
    }

    @Override
    protected void sync(Brick brick) {}

    @Override
    public void waitForUpdate() {
        throw new UnsupportedOperationException();
    }

    public static HttpProxy fromConfig(String configHost) {
        return new HttpProxy();
    }
}

/* public */ final class MockProxy extends Proxy {
    private MockProxy() {
        bricks = new ArrayList<Brick>();
    }

    private final List<Brick> bricks;

    @Override
    void connectBrick(Brick brick) {
        bricks.add(brick);
    }

    @Override
    protected void sync(Brick brick) {}

    @Override
    public void waitForUpdate() {
        for (Brick brick : bricks) {
            byte[] payload = brick.getTargetPayload(true); // mock
            if (payload != null) {
                brick.setPendingPayload(payload);
                brick.tryUpdate(); // ignore result
            }
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1000); // ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static MockProxy fromConfig(String configHost) {
        return new MockProxy();
    }
}

/* public */ final class MqttProxy extends Proxy {
    private MqttProxy(MqttConfig config) {
        mqttConfig = config;
        mqttService = new MqttService();
        bricks = new ArrayList<Brick>();
    }

    private final MqttConfig mqttConfig;
    private final MqttService mqttService;
    private final List<Brick> bricks;

    // calLed exactly once
    private void connect() {
        String host = mqttConfig.getHost();
        String username = mqttConfig.getUsername();
        String password = mqttConfig.getPassword();
        mqttService.init(host, username, password);
        mqttService.connect();
    }

    @Override
    void connectBrick(Brick brick) {
        String topic = mqttConfig.getSubscribeTopic(brick.getID());
        IMqttMessageListener listener = new IMqttMessageListener() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.printf("messageArrived topic = \"%s\", payload = \"%s\"\n", topic, message);
                byte[] payload = message.getPayload();
                brick.setPendingPayload(payload);
            }
        };
        mqttService.subscribe(topic, listener);
        bricks.add(brick);
    }

    @Override
    protected void sync(Brick brick) {
        byte[] payload = brick.getTargetPayload(false); // not a mock
        String topic = mqttConfig.getPublishTopic(brick.getID());
        mqttService.publish(topic, payload);
        System.out.printf("publish topic = \"%s\"\n", topic);
    }

    @Override
    public void waitForUpdate() {
        // for (Brick brick : bricks) {
        //     if (brick.isTargetSyncPending()) { // TODO: && ...
        //         byte[] payload = brick.getTargetPayload(false); // not a mock
        //         String topic = mqttConfig.getPublishTopic(brick.getID());
        //         mqttService.publish(topic, payload);
        //         System.out.printf("publish topic = \"%s\"\n", topic);
        //     }
        // }
        boolean updated = false;
        while (!updated) {
            for (Brick brick : bricks) {
                updated = updated || brick.tryUpdate();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100); // ms
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }

    public static MqttProxy fromConfig(String configHost) {
        MqttConfig config = MqttConfig.fromHost(configHost);
        MqttProxy proxy = new MqttProxy(config); // TODO: singleton per configHost
        proxy.connect();
        return proxy;
    }
}

/* public */ abstract class Brick {
    protected Brick(Proxy proxy, String brickID) {
        this.proxy = proxy;
        this.brickID = brickID;
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(timeZone);
    }

    private final String brickID;
    private final Proxy proxy;
    private final DateFormat formatter;
    private int currentBatteryLevel = 0;
    private Date currentTimestamp = new Date(0L);
    private Date pendingTimestamp = new Date(0L);
    private byte[] pendingPayload = null;

    public String getID() {
        return brickID;
    }

    public int getBatteryLevel() {
        return currentBatteryLevel;
    }

    public Date getTimestamp() {
        return currentTimestamp;
    }

    public String getTimestampIsoUtc() {
        return formatter.format(currentTimestamp);
    }

    protected void setBatteryLevel(int level) {
        currentBatteryLevel = level;
    }

    protected void sync() {
        proxy.sync(this);
    }

    // abstract protected boolean isTargetSyncPending();
    abstract protected byte[] getTargetPayload(boolean mock);
    abstract protected void setCurrentPayload(byte[] payload);

    /* package */ void setPendingPayload(byte[] payload) {
        pendingTimestamp = new Date();
        pendingPayload = payload;
    }

    /* package */ boolean tryUpdate() {
        boolean updated;
        if (currentTimestamp.before(pendingTimestamp)) {
            currentTimestamp = pendingTimestamp;
            setCurrentPayload(pendingPayload);
            updated = true;
        } else {
            updated = false;
        }
        return updated;
    }
}

/* public */ final class ButtonBrick extends Brick {
    private ButtonBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile boolean currentPressed;

    public boolean isPressed() {
        return currentPressed;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentPressed = Boolean.parseBoolean(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // protected boolean isTargetSyncPending() { return false; }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            int mockBatt = (int) (Math.random() * 99 + 1);
            boolean mockPressed = Math.random() < 0.5;
            try {
                String payloadString = 
                    Integer.toString(mockBatt) + SEPARATOR +
                    Boolean.toString(mockPressed);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    public static ButtonBrick connect(Proxy proxy, String brickID) {
        ButtonBrick brick = new ButtonBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class BuzzerBrick extends Brick {
    private BuzzerBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile boolean currentEnabled = false;
    private volatile boolean targetEnabled = false;

    public boolean isEnabled() {
        return currentEnabled;
    }

    // TODO: rename to triggerAlert(int ms)?
    public void setEnabled(boolean enabled) {
        System.out.println("setEnabled = " + enabled);
        targetEnabled = enabled;
        super.sync();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentEnabled = Boolean.parseBoolean(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // protected boolean isTargetSyncPending() {
    //     boolean pending = targetEnabled != currentEnabled;
    //     System.out.println("isTargetSyncPending = " + pending);
    //     return pending;
    // }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        int mockBatt = (int) (Math.random() * 99 + 1);
        try {
            String payloadString = 
                (mock ? Integer.toString(mockBatt) + SEPARATOR : "") +
                Boolean.toString(targetEnabled);
            payload = payloadString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static BuzzerBrick connect(Proxy proxy, String brickID) {
        BuzzerBrick brick = new BuzzerBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class HumiTempBrick extends Brick {
    private HumiTempBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile double currentHumi;
    private volatile double currentTemp;

    public double getHumidity() {
        return currentHumi;
    }

    public double getTemperature() {
        return currentTemp;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentHumi = Double.parseDouble(parts[1]);
            currentTemp = Double.parseDouble(parts[2]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // protected boolean isTargetSyncPending() { return false; }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            int mockBatt = (int) (Math.random() * 99 + 1);
            double mockHumi = Math.random() * 99 + 1;
            double mockTemp = Math.random() * 50 + 1;
            try {
                String payloadString = 
                    Integer.toString(mockBatt) + SEPARATOR +
                    Double.toString(mockHumi) + SEPARATOR + 
                    Double.toString(mockTemp);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    public static HumiTempBrick connect(Proxy proxy, String brickID) {
        // TODO: proxy.getEncoding(brickID) { return mqttConfig.getEncoging(brickID); }
        // => ProtobufHumiTempBrick(), LppHumiTempBrick()
        HumiTempBrick brick = new HumiTempBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class LedBrick extends Brick {
    private LedBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile Color currentColor;
    private volatile Color targetColor;

    public Color getColor() {
        return currentColor;
    }

    public void setColor(Color color) {
        targetColor = color;
        super.sync();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentColor = Color.decode(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // protected boolean isTargetSyncPending() {
    //     return !targetColor.equals(currentColor);
    // }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        try {
            int mockBatt = (int) (Math.random() * 99 + 1);
            int r = targetColor.getRed();
            int g = targetColor.getGreen();
            int b = targetColor.getBlue();
            String payloadString = 
                (mock ? Integer.toString(mockBatt) + SEPARATOR : "") +
                String.format("#%02x%02x%02x", r, g, b);
            payload = payloadString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static LedBrick connect(Proxy proxy, String brickID) {
        LedBrick brick = new LedBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class LcdDisplayBrick extends Brick {
    private LcdDisplayBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile double currentValue = 0.0;
    private volatile double targetValue = 0.0;

    public double getDoubleValue() {
        return currentValue;
    }

    public void setDoubleValue(double value) {
        targetValue = value;
        super.sync();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentValue = Double.parseDouble(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // protected boolean isTargetSyncPending() {
    //     return targetValue != currentValue;
    // }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        try {
            int mockBatt = (int) (Math.random() * 99 + 1);
            String payloadString = 
                (mock ? Integer.toString(mockBatt) + SEPARATOR : "") +
                Double.toString(targetValue);
            payload = payloadString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static LcdDisplayBrick connect(Proxy proxy, String brickID) {
        LcdDisplayBrick brick = new LcdDisplayBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

public final class Bricks {
    private Bricks() {}

    private static final String BUTTON_ID = "0000-0002";
    private static final String BUZZER_ID = "0000-0006";
    private static final String HUMITEMP_ID = "0000-0001";
    private static final String HUMITEMP_0_ID = HUMITEMP_ID;
    private static final String HUMITEMP_1_ID = "0000-0003";
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
        LedBrick ledBrick = LedBrick.connect(proxy, LED_ID);
        while (true) {
            double temp = humiTempBrick.getTemperature();
            Color color = temp > 23 ? Color.RED : Color.GREEN;
            String time = humiTempBrick.getTimestampIsoUtc();
            String line = String.format(Locale.US, "%s, %.2f, %s\n", time, temp, color);
            System.out.print(line);
            displayBrick.setDoubleValue(temp);
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