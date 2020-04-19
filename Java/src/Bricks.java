// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// $ cd Java
// $ curl -o lib/org.eclipse.paho.client.mqttv3-1.2.3.jar \
//   https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/\
//   org.eclipse.paho.client.mqttv3/1.2.3/org.eclipse.paho.client.mqttv3-1.2.3.jar
// $ javac -cp src:lib/org.eclipse.paho.client.mqttv3-1.2.3.jar src/Bricks.java
// $ java -ea -cp src:lib/org.eclipse.paho.client.mqttv3-1.2.3.jar Bricks

// Design principles:
// - keep it simple to use
//     - physical brick => access
//     - no type casts, no generics 
//     - getValue() remains constant
//       until waitForUpdates()
//     - mock mode for quick prototyping
// - single responsibility
//     - transport x encoding x brick type
// - minimize dependencies
//     - provide a single jar library
//     - use as few libraries as possible
//     - provide server/client certs in code

/*
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ClassCastException;
import java.lang.String;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.TimeZone;
import com.sun.net.httpserver.HttpServer;
*/

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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

/* public */ final class MqttConfig {
    private MqttConfig() {}

    public String getHost() {
        // TODO: read mqtt config file
        return "test.mosquitto.org";
    }

    public String getUsername() {
        return null;
    }

    public String getPassword() {
        return null;
    }

    public String getSubscribeTopic(String brickID) {
        // TODO: read bick config file
        String topic;
        if ("0000-0001".equals(brickID)) {
            topic = "bricks/0000-0001/temp/up";
        } else if ("0000-0002".equals(brickID)) {
            topic = "bricks/0000-0002/temp/up";
        } else if ("0000-0003".equals(brickID)) {
            topic = "bricks/0000-0003/temp/up";
        } else if ("0000-0004".equals(brickID)) {
            topic = "bricks/0000-0004/led/up";
        } else {
            topic = null;
        }
        return topic;
    }

    public String getPublishTopic(String brickID) {
        // TODO: read bick config file
        String topic;
        if ("0000-0004".equals(brickID)) {
            topic = "bricks/0000-0004/led/down";
        } else {
            topic = null;
        }
        return topic;
    }

    public static MqttConfig fromHost(String configHost) {
        // TODO: set config host
        return new MqttConfig();
    }
}

/* public */ abstract class Proxy {
    abstract void connectBrick(Brick brick);
    abstract public void waitForUpdate();

        // waitForNextUpdate();
    // waitForUpdates(5 * 60); // s
    // or collectUpdatesUntil(date);

    // public final void waitForUpdate() { // blocking
    //     Date now = new Date();
    //     boolean updated = false;
    //     while (!updated) {
    //         bricksLock.lock();
    //         try {
    //             for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
    //                 Brick brick = entry.getValue();
    //                 updated = updated || now.before(brick.getNextTimestamp());
    //             }
    //         } finally {
    //             bricksLock.unlock();
    //         }
    //         if (!updated) {
    //             // System.out.println(".");
    //             try {
    //                 TimeUnit.MILLISECONDS.sleep(updatePollFrequencyMs);
    //             } catch (InterruptedException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }

    //     bricksLock.lock();
    //     try {
    //         for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
    //             Brick brick = entry.getValue();
    //             brick.updateCurrentValues();
    //         }
    //     } finally {
    //         bricksLock.unlock();
    //     }

    //     writeMessages(); // TODO: move to better place? Let subclass decide?
    // }
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
    public void waitForUpdate() {}

    // /* public */ final class HttpBackendProxy extends BackendProxy implements Runnable {
    //     private HttpServer service; // local or via Relay, e.g. Yaler.net
    //     private HttpClient client;
    //     private URI backendUri;

    //     public HttpBackendProxy(String backendHost, String backendApiToken) {
    //         InetSocketAddress ip = new InetSocketAddress(8080);
    //         try {
    //             service = HttpServer.create(ip, 0);
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }

    //         client = HttpClient.newBuilder()
    //         //  .version(Version.HTTP_1_1)
    //         //  .followRedirects(HttpClient.Redirect.NORMAL)
    //         //  .connectTimeout(Duration.ofSeconds(20))
    //         //  .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
    //         //  .authenticator(Authenticator.getDefault())
    //             .build();

    //         backendUri = URI.create("https://" + backendHost + "/");
    //     }

    //     @Override
    //     /* package */ final void sendMessage(Message message) {
    //         String json = toJsonString(message);
    //         HttpRequest request = HttpRequest.newBuilder()
    //             .uri(backendUri)
    //         //  .timeout(Duration.ofMinutes(2))
    //             .header("Content-Type", "application/json")
    //             .POST(BodyPublishers.ofString(json))
    //             .build();

    //         try {
    //             HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    //             System.out.println(response.statusCode());
    //             System.out.println(response.body());  
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         } catch (InterruptedException e) {
    //             e.printStackTrace();
    //         }
    //     }

    //     @Override
    //     public void start() {
    //         new Thread(this).start();
    //     }
    // }

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
    public void waitForUpdate() {
        for (Brick brick : bricks) {
            byte[] payload = brick.getTargetPayload(true); // mock
            if (payload != null) {
                brick.setCurrentPayload(payload);
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
                System.out.printf("topic = \"%s\", payload = \"%s\"\n", topic, message);
                byte[] payload = message.getPayload();
                brick.setCurrentPayload(payload); // setPendingPayload() ?
            }
        };
        mqttService.subscribe(topic, listener);
        bricks.add(brick);
    }

    @Override
    public void waitForUpdate() {
        for (Brick brick : bricks) {
            String topic = mqttConfig.getPublishTopic(brick.getID());
            byte[] payload = brick.getTargetPayload(false); // not a mock
            if (payload != null) {
                mqttService.publish(topic, payload);
            }
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1000); // ms
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    protected Brick(String brickID) {
        this.brickID = brickID;
        // TODO: move to Proxy?
        timeZone = TimeZone.getTimeZone("UTC");
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(timeZone);
    }

    private final String brickID;
    
    public String getID() {
        return brickID;
    }

    private int currentEnergyLevel = 0;
    private Date currentTimestamp = new Date(0L);
    
    private TimeZone timeZone;
    private DateFormat formatter;

    public int getEnergyLevel() {
        return currentEnergyLevel;
    }

    public Date getTimestamp() {
        return currentTimestamp;
    }

    public String getTimestampIsoUtc() {
        return formatter.format(currentTimestamp);
    }

    abstract protected void setCurrentPayload(byte[] payload);
    abstract protected byte[] getTargetPayload(boolean mock);
}

/* public */ final class ButtonBrick extends Brick {
    private ButtonBrick(String brickID) {
        super(brickID);
    }

    public boolean getPressed() { return false; }
    public void setPressed(boolean pressed) {}

    @Override
    protected void setCurrentPayload(byte[] payload) {}

    @Override
    protected byte[] getTargetPayload(boolean mock) { return null; }

    public static ButtonBrick connect(Proxy proxy, String brickID) {
        ButtonBrick brick = new ButtonBrick(brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class BuzzerBrick extends Brick {
    private BuzzerBrick(String brickID) {
        super(brickID);
    }

    // TODO: rename to triggerAlert(int ms)?
    public void setEnabled(boolean enabled) {}

    @Override
    protected void setCurrentPayload(byte[] payload) {}

    @Override
    protected byte[] getTargetPayload(boolean mock) { return null; }

    public static BuzzerBrick connect(Proxy proxy, String brickID) {
        BuzzerBrick brick = new BuzzerBrick(brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class HumiTempBrick extends Brick {
    private HumiTempBrick(String brickID) {
        super(brickID);
    }

    private volatile double currentHumi;
    private volatile double currentTemp;

    public double getHumidity() {
        return currentTemp;
    }

    public double getTemperature() {
        return currentTemp;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split("|");
            currentHumi = Double.parseDouble(parts[0]);
            currentTemp = Double.parseDouble(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        if (mock) {
            double targetHumi = Math.random() * 99 + 1;
            double targetTemp = Math.random() * 50 + 1;
            try {
                String payloadString = 
                    Double.toString(targetHumi) + "|" + 
                    Double.toString(targetTemp);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                payload = null;
            }
        } else {
            payload = null;
        }
        return payload;
    }

    public static HumiTempBrick connect(Proxy proxy, String brickID) {
        // TODO: proxy.getEncoding(brickID) { return mqttConfig.getEncoging(brickID); }
        // => ProtobufHumiTempBrick(), LppHumiTempBrick()
        HumiTempBrick brick = new HumiTempBrick(brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class LedBrick extends Brick {
    private LedBrick(String brickID) {
        super(brickID);
    }

    private volatile Color currentColor;
    private volatile Color targetColor;

    public Color getColor() {
        return currentColor;
    }

    public void setColor(Color color) {
        targetColor = color;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            // TODO: decode real format
            String message = new String(payload, StandardCharsets.UTF_8);
            currentColor = Color.decode(message);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        // ignore mock flag
        byte[] payload;
        try {
            int r = targetColor.getRed();
            int g = targetColor.getGreen();
            int b = targetColor.getBlue();
            String colorString = 
                String.format("#%02x%02x%02x", r, g, b);  
            payload = colorString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static LedBrick connect(Proxy proxy, String brickID) {
        // TODO: proxy.getEncoding(brickID) { return mqttConfig.getEncoging(brickID); }
        // => ProtobufLedBrick(), LppLedBrick()
        LedBrick brick = new LedBrick(brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class LedStripBrick extends Brick {
    private LedStripBrick(String brickID) {
        super(brickID);
    }

    public void setColors(Color[] values) {}

    @Override
    protected void setCurrentPayload(byte[] payload) {}

    @Override
    protected byte[] getTargetPayload(boolean mock) { return null; }

    public static LedStripBrick connect(Proxy proxy, String brickID) {
        LedStripBrick brick = new LedStripBrick(brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

/* public */ final class LcdDisplayBrick extends Brick {
    private LcdDisplayBrick(String brickID) {
        super(brickID);
    }

    public void setDoubleValue(double value) {}

    public double getDoubleValue() {
        return 0.0;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {}

    @Override
    protected byte[] getTargetPayload(boolean mock) { return null; }

    public static LcdDisplayBrick connect(Proxy proxy, String brickID) {
        LcdDisplayBrick brick = new LcdDisplayBrick(brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}

public final class Bricks {
    private Bricks() {}

    static void runDoorbellExample(Proxy proxy) {
        ButtonBrick buttonBrick = ButtonBrick.connect(proxy, "BUTTON_BRICK_TOKEN");
        BuzzerBrick buzzerBrick = BuzzerBrick.connect(proxy, "BUZZER_BRICK_TOKEN");
        while (true) {
            boolean pressed = buttonBrick.getPressed();
            String time = buttonBrick.getTimestampIsoUtc();
            System.out.println(time + ", " +  pressed);
            buzzerBrick.setEnabled(pressed);
            proxy.waitForUpdate();
        }
    }

    static void runLoggingExample(Proxy proxy) {
        HumiTempBrick brick = HumiTempBrick.connect(proxy, "TEMP_BRICK_TOKEN");
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

    static void runLoggingArrayExample(Proxy proxy) {
        HumiTempBrick[] bricks = new HumiTempBrick[32];
        for (int i = 0; i < bricks.length; i++) {
            bricks[i] = HumiTempBrick.connect(proxy, "TEMP_BRICK_TOKEN_" + i);
        }
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

    static void runMonitoringExample(Proxy proxy) {
        HumiTempBrick humiTempBrick = HumiTempBrick.connect(proxy, "TEMP_BRICK_TOKEN");
        LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(proxy, "DISPLAY_BRICK_TOKEN");
        LedBrick ledBrick = LedBrick.connect(proxy, "LED_BRICK_TOKEN");
        while (true) {
            double temp = humiTempBrick.getTemperature();
            displayBrick.setDoubleValue(temp);
            Color color = temp > 23 ? Color.RED : Color.GREEN;
            String time = humiTempBrick.getTimestampIsoUtc();
            String line = String.format(Locale.US, "%s, %.2f, %s\n", time, temp, color);
            System.out.print(line);
            ledBrick.setColor(color);
            proxy.waitForUpdate();
        }
    }

    public static void main(String args[]) {
        String usageErrorMessage = "usage: java Bricks http|mqtt|mock d|l|a|m";
        if (args.length == 2) {
            Proxy proxy = null;
            if ("http".equals(args[0])) {
                proxy = HttpProxy.fromConfig("brick.li");
            } else if ("mqtt".equals(args[0])) {
                proxy = MqttProxy.fromConfig("brick.li");
            } else if ("mock".equals(args[0])) {
                proxy = MockProxy.fromConfig("brick.li");
            } else {
                System.out.println(usageErrorMessage);
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
                System.out.println(usageErrorMessage);
                System.exit(-1);
            }
        } else {
            System.out.println(usageErrorMessage);
            System.exit(-1);
        }
    }
}