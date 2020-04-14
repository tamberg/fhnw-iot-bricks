// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

// $ java -cp src Bricks

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

/* package */ final class Message {
    Map<String, Object> attributes = Collections.synchronizedMap(
        new HashMap<String, Object>());

    /* package */ void putBooleanValue(String key, Boolean value) {
        attributes.put(key, value);
    }

    /* package */ void putDateValue(String key, Date value) {
        attributes.put(key, value);
    }

    /* package */ void putDoubleValue(String key, Double value) {
        attributes.put(key, value);
    }

    /* package */ void putIntegerValue(String key, Integer value) {
        attributes.put(key, value);
    }

    /* package */ void putStringValue(String key, String value) {
        attributes.put(key, value);
    }

//    /* package */ Iterator<E> iterator() {
//        return attributes.values().iterator();
//    }

    public Boolean getBooleanValue(String key) {
        return (Boolean) attributes.get(key);
    }

    public Date getDateValue(String key) {
        return (Date) attributes.get(key);
    }

    public Double getDoubleValue(String key) {
        return (Double) attributes.get(key);
    }

    public Integer getIntegerValue(String key) {
        return (Integer) attributes.get(key);
    }

    public String getStringValue(String key) {
        return (String) attributes.get(key);
    }
}

/* public */ abstract class Brick {
    private BackendProxy proxy = null;
    private String token = null;
    private Date currentTimestamp = new Date(0L);
    private Date nextTimestamp = new Date(0L);
    private int currentBatteryLevel = 0;
    private int nextBatteryLevel = 0;

    private TimeZone timeZone;
    private DateFormat formatter;

    Brick(String token) {
        timeZone = TimeZone.getTimeZone("UTC");
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(timeZone);
        this.token = token;
    }

    protected final void setBackendProxy(BackendProxy proxy) {
        this.proxy = proxy;
    }

    // protected final BackendProxy getBackendProxy() {
    //     return proxy;
    // }

    protected final String dateToIsoUtcString(Date date) {
        return formatter.format(date);
    }

    protected abstract void updateCurrentValues2();

    void updateCurrentValues() {
        // System.out.println("Brick.updateCurrentValues(), token = " + token);
        updateCurrentValues2();
        currentBatteryLevel = nextBatteryLevel;
        currentTimestamp = nextTimestamp;
    }

    protected abstract void readMessage2(Message message);

    void readMessage(Message message) {
        if (token.equals(message.getStringValue("token"))) {
            // System.out.println("Brick.readMessage(), token = " + token);
            nextBatteryLevel = message.getIntegerValue("battery");
            nextTimestamp = message.getDateValue("timestamp");
            readMessage2(message);
        } else {
            // System.out.println("Brick.readMessage(), token mismatch");
        }
    }

    protected abstract void writeMessage2(Message message);

    // proxy creates message, calls
    void writeMessage(Message message) {
        message.putStringValue("token", token);
        writeMessage2(message);
        // proxy sends message
    }

    /* package */ Date getNextTimestamp() {
        return nextTimestamp;
    }

    public String getToken() {
        return token;
    }

    public int getBatteryLevel() {
        return currentBatteryLevel;
    }

    public Date getTimestamp() {
        return currentTimestamp;
    }

    public String getTimestampIsoUtc() {
        return dateToIsoUtcString(currentTimestamp);
    }
}

/* public */ final class ButtonBrick extends Brick {
    boolean currentPressed;
    boolean nextPressed;

    ButtonBrick(String token) {
        super(token);
    }

    @Override
    protected final void readMessage2(Message message) {
        nextPressed = message.getBooleanValue("pressed");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentPressed = nextPressed;
    }

    @Override
    protected final void writeMessage2(Message message) {}

    public boolean getPressed() { return currentPressed; }

    public static ButtonBrick connect(BackendProxy proxy, String token) {
        ButtonBrick brick = new ButtonBrick(token);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class BuzzerBrick extends Brick {
    boolean nextEnabled;
    boolean currentEnabled;
    boolean targetEnabled;

    BuzzerBrick(String token) {
        super(token);
    }

    @Override
    protected final void readMessage2(Message message) {
        nextEnabled = message.getBooleanValue("enabled");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentEnabled = nextEnabled;
    }

    @Override
    protected final void writeMessage2(Message message) {
        message.putBooleanValue("enabled", targetEnabled);
    }

    // TODO: rename to triggerAlert(int ms)?
    public void setEnabled(boolean enabled) {
        targetEnabled = enabled;
    }

    public static BuzzerBrick connect(BackendProxy proxy, String token) {
        BuzzerBrick brick = new BuzzerBrick(token);
        brick.setBackendProxy(proxy);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class LedBrick extends Brick {
    LedBrick(String token) {
        super(token);
    }

    @Override
    protected final void readMessage2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    @Override
    protected final void writeMessage2(Message message) {
        //message.putBooleanValue("color", targetEnabled);
    }

    public void setColor(Color value) {
        // TODO
    }

    public static LedBrick connect(BackendProxy proxy, String token) {
        LedBrick brick = new LedBrick(token);
        brick.setBackendProxy(proxy);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class LedStripBrick extends Brick {
    private LedStripBrick(String token) {
        super(token);
    }

    @Override
    protected final void readMessage2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    @Override
    protected final void writeMessage2(Message message) {
        //message.putBooleanValue("colors", targetEnabled);
    }

    public void setColors(Color[] values) {
        // TODO
    }

    public static LedStripBrick connect(BackendProxy proxy, String token) {
        LedStripBrick brick = new LedStripBrick(token);
        brick.setBackendProxy(proxy);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class TemperatureBrick extends Brick {
    double currentTemp;
    double nextTemp;
    double currentHumi;
    double nextHumi;

    private TemperatureBrick(String token) {
        super(token);
    }

    @Override
    protected final void readMessage2(Message message) {
        nextTemp = message.getDoubleValue("temperature");
        nextHumi = message.getDoubleValue("humidity");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentTemp = nextTemp;
        currentHumi = nextHumi;
    }

    @Override
    protected final void writeMessage2(Message message) {}

    public double getHumidity() {
        return currentHumi;
    }

    public double getTemperature() {
        return currentTemp;
    }

    public static TemperatureBrick connect(BackendProxy proxy, String token) {
        TemperatureBrick brick = new TemperatureBrick(token);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class LcdDisplayBrick extends Brick {
    double targetValue = 0; // TODO: naming
    double currentValue = 0;
    double nextValue = 0;

    private LcdDisplayBrick(String token) {
        super(token);
    }

    @Override
    protected final void readMessage2(Message message) {
        nextValue = message.getDoubleValue("value");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentValue = nextValue;
    }

    @Override
    protected final void writeMessage2(Message message) {
        message.putDoubleValue("value", targetValue);
    }

    public void setDoubleValue(double value) { // TODO: rename to showDoubleValue?
        targetValue = value;
    }

    public double getDoubleValue() {
        return currentValue;
    }

    public static LcdDisplayBrick connect(BackendProxy proxy, String token) {
        LcdDisplayBrick brick = new LcdDisplayBrick(token);
        brick.setBackendProxy(proxy);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ abstract class BackendProxy {
    private int updatePollFrequencyMs = 500; // >= 500
    Lock bricksLock = new ReentrantLock(); // TODO
    Map<String, Brick> bricks = Collections.synchronizedMap(
        new HashMap<String, Brick>());

    /* package */ void addBrick(Brick brick) {
        // TODO: throw IllegalArgumentException if token type != brick type?
        bricksLock.lock();
        try {
            if (!bricks.containsValue(brick) && 
                !bricks.containsKey(brick.getToken())) {
                bricks.put(brick.getToken(), brick);
            }
        } finally {
            bricksLock.unlock();
        }
    }

    protected final void setUpdatePollFrequencyMs(int updatePollFrequencyMs) {
        this.updatePollFrequencyMs = updatePollFrequencyMs;
    }

    protected final void readMessage(Message message) { // thread
        //System.out.println("BackendProxy.readMessage()");
        // Message is read by whomever it may concern
        bricksLock.lock();
        try {
            for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                Brick brick = entry.getValue();
                brick.readMessage(message);
            }
        } finally {
            bricksLock.unlock();
        }
    }

    protected final void writeMessages() {
        System.out.println("BackendProxy.writeMessages()");
        bricksLock.lock();
        try {
            for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                Brick brick = entry.getValue();
                Message message = new Message(); // TODO: createMessage()in sublass?
                // TODO: if (brick.isWriteNeeded()) { ...
                brick.writeMessage(message);
                sendMessage(message); // TODO: Thread? Delegate to subclass?
            }
        } finally {
            bricksLock.unlock();
        }
    }

    // TODO ? 
    // waitForNextUpdate();
    // waitForUpdates(5 * 60); // s
    // or collectUpdatesUntil(date);

    public final void waitForUpdate() { // blocking
        //System.out.println("BackendProxy.waitForUpdate()");
        // TODO: prevent unneccessary updates
        Date now = new Date();
        boolean updated = false;
        while (!updated) {
            bricksLock.lock();
            try {
                for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                    Brick brick = entry.getValue();
                    updated = updated || now.before(brick.getNextTimestamp());
                }
            } finally {
                bricksLock.unlock();
            }
            if (!updated) {
                // System.out.println(".");
                try {
                    TimeUnit.MILLISECONDS.sleep(updatePollFrequencyMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        bricksLock.lock();
        try {
            for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                Brick brick = entry.getValue();
                brick.updateCurrentValues();
            }
        } finally {
            bricksLock.unlock();
        }

        writeMessages(); // TODO: move to better place? Let subclass decide?
    }

    /* package */ abstract void sendMessage(Message message);

    public abstract void start(); // TODO: rename to begin?
}

/* public */ final class HttpBackendProxy extends BackendProxy implements Runnable {
    private HttpServer service; // local or via Relay, e.g. Yaler.net
    private HttpClient client;
    private URI backendUri;

    public HttpBackendProxy(String backendHost, String backendApiToken) {
        InetSocketAddress ip = new InetSocketAddress(8080);
        try {
            service = HttpServer.create(ip, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client = HttpClient.newBuilder()
        //  .version(Version.HTTP_1_1)
        //  .followRedirects(HttpClient.Redirect.NORMAL)
        //  .connectTimeout(Duration.ofSeconds(20))
        //  .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
        //  .authenticator(Authenticator.getDefault())
            .build();

        backendUri = URI.create("https://" + backendHost + "/");
    }

    public void run() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    private String toJsonString(Message message) {
        // TODO: enough knowledge?
        //   message.get...
        //   keys, type, sequence
        //   vs. Message subtype TtnHttpJsonMessage 
        //   or even TemperatureTtnHttpJsonMessage
        //   m.toString() ...
        return "";
    }

    @Override
    /* package */ final void sendMessage(Message message) {
        String json = toJsonString(message);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(backendUri)
        //  .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(json))
            .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());  
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        new Thread(this).start();
    }
}

/* public */ final class MqttBackendProxy extends BackendProxy implements Runnable {
    // private MqttClient client; // PUB & SUB

    public MqttBackendProxy(String host, String user, String password) {
        // TODO
    }

    public void run() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    /* package */ final void sendMessage(Message message) {}

    @Override
    public void start() {
        new Thread(this).start();
    }
}     

/* public */ final class MockBackendProxy extends BackendProxy implements Runnable {
    int maxUpdateFrequencyMs;
    Random random = new Random();

    public MockBackendProxy(int maxUpdateFrequencyMs, int updatePollFrequencyMs) {
        this.maxUpdateFrequencyMs = maxUpdateFrequencyMs;
        super.setUpdatePollFrequencyMs(updatePollFrequencyMs);
    }

    Brick getRandomBrick () {
        Brick brick = null;
        bricksLock.lock();
        try {
            Collection<Brick> brickCollection = bricks.values();
            Object[] brickArray = brickCollection.toArray();
            int length = brickArray.length;
            if (length > 0) {
                int i = random.nextInt(length);
                brick = (Brick) brickArray[i];
            }
        } finally {
            bricksLock.unlock();
        }
        return brick; 
    }

    public void run() { // TODO: move inside, to hide from public
        while (true) {
            Brick brick = getRandomBrick();
            if (brick != null) {
                Message message = new Message();
                message.putStringValue("token", brick.getToken());
                message.putDateValue("timestamp", new Date());
                message.putIntegerValue("battery", random.nextInt(100));
                if (brick instanceof ButtonBrick) {
                    message.putBooleanValue("pressed", random.nextInt(2) == 0);
                } else if (brick instanceof BuzzerBrick) {
                    message.putBooleanValue("enabled", random.nextInt(2) == 0);
                } else if (brick instanceof LcdDisplayBrick) {
                    message.putDoubleValue("value", random.nextDouble() * 50.0);
                } else if (brick instanceof TemperatureBrick) {
                    message.putDoubleValue("humidity", random.nextDouble() * 100.0);
                    message.putDoubleValue("temperature", random.nextDouble() * 50.0);
                }
                super.readMessage(message);
            }
            if (maxUpdateFrequencyMs > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(maxUpdateFrequencyMs));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    /* package */ final void sendMessage(Message message) {
        // TODO
        String token = message.getStringValue("token");
        System.out.println(token);
    }

    @Override
    public void start() {
        new Thread(this).start();
    }
}

// TODO: BLE based Bricks (same token)? Here, the machine running this class is the "Backend". 

/* public */ final class BleBackendProxy extends BackendProxy implements Runnable {
    // private BleCentral central;

    public BleBackendProxy() {
        // TODO
    }

    public void run() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    /* package */ final void sendMessage(Message message) {}

    @Override
    public void start() {
        new Thread(this).start();
    }
}

public final class Bricks {
    private Bricks() {}

    static void runDoorbellExample(BackendProxy proxy) {
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

    static void runLoggingExample(BackendProxy proxy) {
        TemperatureBrick tempBrick = TemperatureBrick.connect(proxy, "TEMP_BRICK_TOKEN");
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
            String time = tempBrick.getTimestampIsoUtc();
            double temp = tempBrick.getTemperature();
            double humi = tempBrick.getHumidity();
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

    static void runLoggingArrayExample(BackendProxy proxy) {
        TemperatureBrick[] tempBricks = new TemperatureBrick[32];
        for (int i = 0; i < tempBricks.length; i++) {
            tempBricks[i] = TemperatureBrick.connect(proxy, "TEMP_BRICK_TOKEN_" + i);
        }

        while (true) {
            for (TemperatureBrick tempBrick : tempBricks) {
                String token = tempBrick.getToken();
                String time = tempBrick.getTimestampIsoUtc();
                double temp = tempBrick.getTemperature();
                double humi = tempBrick.getHumidity();
                String line = String.format(Locale.US, "%s, %s, %.2f, %.2f", token, time, temp, humi);
                System.out.println(line);
            }
            proxy.waitForUpdate();
        }
    }

    static void runMonitoringExample(BackendProxy proxy) {
        TemperatureBrick tempBrick = TemperatureBrick.connect(proxy, "TEMP_BRICK_TOKEN");
        LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(proxy, "DISPLAY_BRICK_TOKEN");
        LedBrick ledBrick = LedBrick.connect(proxy, "LED_BRICK_TOKEN");

        while (true) {
            double temp = tempBrick.getTemperature();
            displayBrick.setDoubleValue(temp);
            Color color = temp > 23 ? Color.RED : Color.GREEN;
            String time = tempBrick.getTimestampIsoUtc();
            String line = String.format(Locale.US, "%s, %.2f, %s\n", time, temp, color);
            System.out.print(line);
            ledBrick.setColor(color);
            proxy.waitForUpdate();
        }
    }

    public static void main(String args[]) {
        String usageErrorMessage = "usage: java Bricks http|mqtt|mock d|l|a|m";
        if (args.length == 2) {
            BackendProxy proxy = null;
            if ("http".equals(args[0])) {
                proxy = new HttpBackendProxy("HTTP_HOST", "HTTP_API_TOKEN");
            } else if ("mqtt".equals(args[0])) {
                proxy = new MqttBackendProxy("MQTT_HOST", "MQTT_USER", "MQTT_PASSWORD");
            } else if ("mock".equals(args[0])) {
                // proxy = new MockBackendProxy(10, 320); // $ java Bricks mock a
                //proxy = new MockBackendProxy(1000, 500); // $ java mock d|l|m (fast)
                proxy = new MockBackendProxy(3000, 1000); // $ java mock d|l|m (medium)
                // proxy = new MockBackendProxy(5 * 60 * 1000, 500); // $ java mock d|l|m (slow, LoRaWAN)
            } else if ("ble".equals(args[0])) {
                proxy = new BleBackendProxy();
            } else {
                System.out.println(usageErrorMessage);
                System.exit(-1);
            }
            proxy.start(); // TODO: make implicit, inside waitForUpdate?
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