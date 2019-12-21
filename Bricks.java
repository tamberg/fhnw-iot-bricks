import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ClassCastException;
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    Date currentTimestamp = new Date(Long.MIN_VALUE);
    Date nextTimestamp = new Date(Long.MIN_VALUE);
    int currentBatteryLevel = 0;
    int nextBatteryLevel = 0;

    Brick(String token) {
        this.token = token;
    }

    protected abstract void updateCurrentValues2();

    void updateCurrentValues() {
        System.out.println("Brick.updateCurrentValues(), token = " + token);
        updateCurrentValues2();
        currentBatteryLevel = nextBatteryLevel;
        currentTimestamp = nextTimestamp;
    }

    protected abstract void handleUpdate2(Message message);

    void handleUpdate(Message message) {
        if (token.equals(message.getStringValue("token"))) {
            System.out.println("Brick.handleUpdate(), token = " + token);
            nextBatteryLevel = message.getIntegerValue("battery");
            nextTimestamp = message.getDateValue("timestamp");
            handleUpdate2(message);
        } else {
            System.out.println("Brick.handleUpdate(), token mismatch");
        }
    }

    Date getNextTimestamp() {
        return nextTimestamp;
    }

    public String getToken() {
        return token;
    }

    public int getBatteryLevel() {
        return currentBatteryLevel;
    }

    public Date getLastUpdateTimestamp() {
        return currentTimestamp;
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

    public static ButtonBrick connect(Backend backend, String token) {
        ButtonBrick brick = new ButtonBrick(token);
        backend.addBrick(brick);
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
        System.out.println(value); // TODO
    }

    public static LedBrick connect(Backend backend, String token) {
        LedBrick brick = new LedBrick(token);
        backend.addBrick(brick);
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
        System.out.println(values); // TODO
    }

    public static LedStripBrick connect(Backend backend, String token) {
        LedStripBrick brick = new LedStripBrick(token);
        backend.addBrick(brick);
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

    public static TemperatureBrick connect(Backend backend, String token) {
        TemperatureBrick brick = new TemperatureBrick(token);
        backend.addBrick(brick);
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

    public void setDoubleValue(double value) {
        if (targetValue != value) {
            targetValue = value;
            System.out.println(value); // TODO
        }
    }

    public double getDoubleValue() {
        return currentValue;
    }

    public static LcdDisplayBrick connect(Backend backend, String token) {
        LcdDisplayBrick brick = new LcdDisplayBrick(token);
        backend.addBrick(brick);
        return brick;
    }
}

/* public */ abstract class Backend {
    Lock bricksLock = new ReentrantLock(); // TODO
    Map<String, Brick> bricks = new HashMap<String, Brick>();

    /* package */ public void addBrick(Brick brick) {
        bricksLock.lock();
        try {
            if (!bricks.containsValue(brick) && 
                !bricks.containsKey(brick.getToken())) {
                bricks.put(brick.getToken(), brick);
            }
        } finally {
            bricksLock.unlock();
        }
        // TODO: throw IllegalArgumentException ?
    }

    // Updates
    protected final void handleUpdate(Message message) { // thread
        //System.out.println("Backend.handleUpdate()");
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

    public final void waitForUpdate() { // blocking
        //System.out.println("Backend.waitForUpdate()");
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
                System.out.println(".");
                try {
                    TimeUnit.MILLISECONDS.sleep(500); // >= 500ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // Update
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

    public abstract void start();
}

/* public */ final class HttpBackend extends Backend implements Runnable {
    HttpBackend(String host, String apiToken) {
        // create
        // run Thread that 
        //  connects to HTTP backend
        //  calls super.handleUpdate(message)
    }

    public void run() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void start() {
        new Thread(this).start();
    }
}

/* public */ final class MqttBackend extends Backend implements Runnable {
    // private MqttConnection connection;

    MqttBackend(String host, String user, String password) {
        // create
        // run Thread that 
        //  connects to MQTT backend
        //  calls super.handleUpdate(message)
    }

    public void run() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void start() {
        new Thread(this).start();
    }
}     

/* public */ final class MockBackend extends Backend implements Runnable {
    int maxUpdateFrequencySeconds;
    Random random = new Random();

    MockBackend(int maxUpdateFrequencySeconds) {
        this.maxUpdateFrequencySeconds = maxUpdateFrequencySeconds;
    }

    private void randomSleep() {
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(maxUpdateFrequencySeconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() { // TODO: move inside, to hide from public
        while (true) {
            // TODO: get existing tokens, brick types from base class

            Message message0 = new Message();
            message0.addStringValue("token", "TEMP_BRICK_TOKEN");
            message0.addDateValue("timestamp", new Date());
            message0.addIntegerValue("battery", random.nextInt(100));
            message0.addDoubleValue("humidity", random.nextDouble() * 100.0);
            message0.addDoubleValue("temperature", random.nextDouble() * 50.0);
            super.handleUpdate(message0);
            randomSleep();

            Message message1 = new Message();
            message1.addStringValue("token", "DISPLAY_BRICK_TOKEN");
            message1.addDateValue("timestamp", new Date());
            message1.addIntegerValue("battery", 50);
            message1.addDoubleValue("value", 23.0);
            super.handleUpdate(message1);
            randomSleep();

            Message message2 = new Message();
            message2.addStringValue("token", "BUTTON_BRICK_TOKEN");
            message2.addDateValue("timestamp", new Date());
            message2.addIntegerValue("battery", 75);
            message2.addBooleanValue("pressed", random.nextInt(2) == 0);
            super.handleUpdate(message2);
            randomSleep();
        }
    }

    @Override
    public void start() {
        new Thread(this).start();
    }
}

public final class Bricks {

    static void runMonitoringSystem(Backend backend) {
        TemperatureBrick tempBrick = TemperatureBrick.connect(backend, "TEMP_BRICK_TOKEN");
        LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(backend, "DISPLAY_BRICK_TOKEN");

        while (true) {
            double temp = tempBrick.getTemperature();
            displayBrick.setDoubleValue(temp);
            backend.waitForUpdate();
        }
    }

    static void runLoggingSystem(Backend backend) {
        TemperatureBrick tempBrick = TemperatureBrick.connect(backend, "TEMP_BRICK_TOKEN");
        FileWriter fileWriter = null;
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        format.setTimeZone(timeZone);
        try {
            fileWriter = new FileWriter("log.csv", true); // append
            fileWriter.append("Timestamp\tTemperature\tHumidity\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;
        while (true) {
            Date timestamp = tempBrick.getLastUpdateTimestamp();
            String isoTimestamp = format.format(timestamp);
            double temp = tempBrick.getTemperature();
            double humi = tempBrick.getHumidity();
            try {
                fileWriter.append(String.format("%s\t%.2f\t%.2f\n", isoTimestamp, temp, humi));
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            backend.waitForUpdate();
        }
    }

    static void runDoorBellSystem(Backend backend) {
        ButtonBrick buttonBrick = ButtonBrick.connect(backend, "BUTTON_BRICK_TOKEN");
        LedBrick ledBrick = LedBrick.connect(backend, "LED_BRICK_TOKEN");

        while (true) {
            boolean pressed = buttonBrick.getPressed();
            ledBrick.setColor(pressed ? Color.RED : Color.BLACK);
            backend.waitForUpdate();
        }
    }

    public static void main(String args[]) {
        String usageErrorMessage = "usage: java Bricks http|mqtt|mock m|l|d";
        if (args.length == 2) {
            Backend backend = null;
            if ("http".equals(args[0])) {
                backend = new HttpBackend("HTTP_HOST", "HTTP_API_TOKEN");
            } else if ("mqtt".equals(args[0])) {
                backend = new MqttBackend("MQTT_HOST", "MQTT_USER", "MQTT_PASSWORD");
            } else if ("mock".equals(args[0])) {
                backend = new MockBackend(3); // s
            } else {
                System.out.println(usageErrorMessage);
                System.exit(-1);
            }
            backend.start(); // TODO: move to end, implement setup(), loop() as upcalls? :)
            if ("m".equals(args[1])) {
                runMonitoringSystem(backend);
            } else if ("l".equals(args[1])) {
                runLoggingSystem(backend);
            } else if ("d".equals(args[1])) {
                runDoorBellSystem(backend);
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