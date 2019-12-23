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
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.TimeZone;
import com.sun.net.httpserver.HttpServer;

/* package */ final class Message {
    Map<String, Boolean> booleans = new HashMap<String, Boolean>();
    Map<String, Date> dates = new HashMap<String, Date>();
    Map<String, Double> doubles = new HashMap<String, Double>();
    Map<String, Integer> integers = new HashMap<String, Integer>();
    Map<String, String> strings = new HashMap<String, String>();

    void addBooleanValue(String key, Boolean value) {
        booleans.put(key, value);
    }

    void addDateValue(String key, Date value) {
        dates.put(key, value);
    }

    void addDoubleValue(String key, Double value) {
        doubles.put(key, value);
    }

    void addIntegerValue(String key, Integer value) {
        integers.put(key, value);
    }

    void addStringValue(String key, String value) {
        strings.put(key, value);
    }

    public Boolean getBooleanValue(String key) {
        return booleans.get(key);
    }

    public Date getDateValue(String key) {
        return dates.get(key);
    }

    public Double getDoubleValue(String key) {
        return doubles.get(key);
    }

    public Integer getIntegerValue(String key) {
        return integers.get(key);
    }

    public String getStringValue(String key) {
        return strings.get(key);
    }
}

/* public */ abstract class Brick {
    String token = null;
    Date currentTimestamp = new Date(0L);
    Date nextTimestamp = new Date(0L);
    int currentBatteryLevel = 0;
    int nextBatteryLevel = 0;

    TimeZone timeZone;
    DateFormat formatter;

    Brick(String token) {
        timeZone = TimeZone.getTimeZone("UTC");
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(timeZone);
        this.token = token;
    }

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

    protected abstract void handleUpdate2(Message message);

    void handleUpdate(Message message) {
        if (token.equals(message.getStringValue("token"))) {
            // System.out.println("Brick.handleUpdate(), token = " + token);
            nextBatteryLevel = message.getIntegerValue("battery");
            nextTimestamp = message.getDateValue("timestamp");
            handleUpdate2(message);
        } else {
            // System.out.println("Brick.handleUpdate(), token mismatch");
        }
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
    protected final void handleUpdate2(Message message) {
        nextPressed = message.getBooleanValue("pressed");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentPressed = nextPressed;
    }

    public boolean getPressed() { return currentPressed; }

    public static ButtonBrick connect(BackendProxy proxy, String token) {
        ButtonBrick brick = new ButtonBrick(token);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class BuzzerBrick extends Brick {
    BuzzerBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public void setEnabled(boolean enabled) { // TODO: rename to triggerAlert(int ms)?
        // TODO
    }

    public static BuzzerBrick connect(BackendProxy proxy, String token) {
        BuzzerBrick brick = new BuzzerBrick(token);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class LedBrick extends Brick {
    LedBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public void setColor(Color value) {
        // TODO
    }

    public static LedBrick connect(BackendProxy proxy, String token) {
        LedBrick brick = new LedBrick(token);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ final class LedStripBrick extends Brick {
    private LedStripBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public void setColors(Color[] values) {
        // TODO
    }

    public static LedStripBrick connect(BackendProxy proxy, String token) {
        LedStripBrick brick = new LedStripBrick(token);
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
    protected final void handleUpdate2(Message message) {
        nextTemp = message.getDoubleValue("temperature");
        nextHumi = message.getDoubleValue("humidity");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentTemp = nextTemp;
        currentHumi = nextHumi;
    }

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
    protected final void handleUpdate2(Message message) {
        nextValue = message.getDoubleValue("value");
    }

    @Override
    protected final void updateCurrentValues2() {
        currentValue = nextValue;
    }

    public void setDoubleValue(double value) { // TODO: rename to showDoubleValue?
        if (targetValue != value) {
            targetValue = value;
            // TODO
        }
    }

    public double getDoubleValue() {
        return currentValue;
    }

    public static LcdDisplayBrick connect(BackendProxy proxy, String token) {
        LcdDisplayBrick brick = new LcdDisplayBrick(token);
        proxy.addBrick(brick);
        return brick;
    }
}

/* public */ abstract class BackendProxy {
    private int updatePollFrequencyMs = 500; // >= 500
    Lock bricksLock = new ReentrantLock(); // TODO
    Map<String, Brick> bricks = new HashMap<String, Brick>();

    /* package */ public void addBrick(Brick brick) {
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

    protected final void handleUpdate(Message message) { // thread
        // System.out.println("BackendProxy.handleUpdate()");
        bricksLock.lock();
        try {
            for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                Brick brick = entry.getValue();
                brick.handleUpdate(message);
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
        // System.out.println("BackendProxy.waitForUpdate()");
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
        return ""; // TODO: enough knowledge?
    }

    @Override
    /* package */ final void sendMessage(Message message) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(backendUri)
        //  .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(toJsonString(message)))
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
                message.addStringValue("token", brick.getToken());
                message.addDateValue("timestamp", new Date());
                message.addIntegerValue("battery", random.nextInt(100));
                if (brick instanceof TemperatureBrick) {
                    message.addDoubleValue("humidity", random.nextDouble() * 100.0);
                    message.addDoubleValue("temperature", random.nextDouble() * 50.0);
                } else if (brick instanceof LcdDisplayBrick) {
                    message.addDoubleValue("value", random.nextDouble() * 50.0);
                } else if (brick instanceof ButtonBrick) {
                    message.addBooleanValue("pressed", random.nextInt(2) == 0);
                }
                super.handleUpdate(message);
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
    /* package */ final void sendMessage(Message message) {}

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
                proxy = new MockBackendProxy(10, 320); // $ java Bricks mock a
                // proxy = new MockBackendProxy(1000, 500); // $ java mock d|l|m (fast)
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