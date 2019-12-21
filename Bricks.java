import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ClassCastException;
import java.lang.String;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class Message {
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
        updateCurrentValues2();
        currentBatteryLevel = nextBatteryLevel;
        currentTimestamp = nextTimestamp;
    }

    protected abstract void handleUpdate2(Message message);

    void handleUpdate(Message message) {
        if (token.equals(message.getStringValue("token"))) {
            nextBatteryLevel = message.getIntegerValue("battery");
            nextTimestamp = message.getDateValue("timestamp");
            handleUpdate2(message);
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

    public void setColor(Color value) {}

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

    public void setColors(Color[] values) {}

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
        // TODO: throw IllegalArgumentException ?
        if (!bricks.containsValue(brick) && 
            !bricks.containsKey(brick.getToken())) {
            bricks.put(brick.getToken(), brick);
        }
    }

    // Updates
    protected final void handleUpdate(Message message) { // thread
        bricksLock.lock();
        try {
            for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                Brick brick = entry.getValue();
                // TODO: check if message is for this brick / token
                brick.handleUpdate(message);
            }
        } finally {
            bricksLock.unlock();
        }
    }

    public final void update() { // blocking
        System.out.print("Backend.update()");
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
                System.out.print(".");
                try {
                    TimeUnit.SECONDS.sleep(1); // TODO: better heuristic
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println();
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
}

/* public */ final class HttpBackend extends Backend {
    HttpBackend(String host, String apiToken) {
        // create
        // run Thread that 
        //  connects to HTTP backend
        //  calls super.handleUpdate(message)?
    }
}

/* public */ final class MqttBackend extends Backend {
    // private MqttConnection connection;

    MqttBackend(String host, String user, String password) {
        // create
        // run Thread that 
        //  connects to MQTT backend
        //  calls super.handleUpdate(message)?
    }
}     

/* public */ final class MockBackend extends Backend implements Runnable {
    int updateFrequencySeconds;

    MockBackend(int updateFrequencySeconds) {
        this.updateFrequencySeconds = updateFrequencySeconds;
        new Thread(this).start(); // TODO: bad style? Move to start() ?
    }

    public void run() {
        while (true) {
            // TODO: get existing tokens, brick types from base class

            Message message0 = new Message();
            message0.addStringValue("token", "TEMP_BRICK_TOKEN");
            message0.addDateValue("timestamp", new Date());
            message0.addIntegerValue("battery", 100);
            message0.addDoubleValue("humidity", 42.0);
            message0.addDoubleValue("temperature", 23.0);
            super.handleUpdate(message0);

            Message message1 = new Message();
            message1.addStringValue("token", "DISPLAY_BRICK_TOKEN");
            message1.addDateValue("timestamp", new Date());
            message1.addIntegerValue("battery", 50);
            super.handleUpdate(message1);

            Message message2 = new Message();
            message2.addStringValue("token", "BUTTON_BRICK_TOKEN");
            message2.addDateValue("timestamp", new Date());
            message2.addIntegerValue("battery", 75);
            message2.addBooleanValue("pressed", true);
            super.handleUpdate(message2);

            try {
                TimeUnit.SECONDS.sleep(updateFrequencySeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public final class Bricks {

    static void runMonitoringSystem(Backend backend) {
        TemperatureBrick tempBrick = TemperatureBrick.connect(backend, "TEMP_BRICK_TOKEN");
        LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(backend, "DISPLAY_BRICK_TOKEN");

        while (true) {
            double temp = tempBrick.getTemperature();
            displayBrick.setDoubleValue(temp);
            backend.update();
        }
    }

    static void runLoggingSystem(Backend backend) {
        TemperatureBrick tempBrick = TemperatureBrick.connect(backend, "TEMP_BRICK_TOKEN");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("log.csv", true); // append
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;
        while (true) {
            double temp = tempBrick.getTemperature();
            Date time = tempBrick.getLastUpdateTimestamp();
            try {
                fileWriter.append(time + ", " + temp + "\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            backend.update();
        }
    }

    static void runDoorBellSystem(Backend backend) {
        ButtonBrick buttonBrick = ButtonBrick.connect(backend, "BUTTON_BRICK_TOKEN");
        LedBrick ledBrick = LedBrick.connect(backend, "LED_BRICK_TOKEN");

        while (true) {
            boolean pressed = buttonBrick.getPressed();
            ledBrick.setColor(pressed ? Color.RED : Color.BLACK);
            backend.update();
        }
    }

    public static void main(String args[]) {
        if (args.length == 2) {
            Backend backend = null;
            if ("http".equals(args[0])) {
                backend = new HttpBackend("HTTP_HOST", "HTTP_API_TOKEN");
            } else if ("mqtt".equals(args[0])) {
                backend = new MqttBackend("MQTT_HOST", "MQTT_USER", "MQTT_PASSWORD");
            } else if ("mock".equals(args[0])) {
                backend = new MockBackend(5); // s
            }

            if ("m".equals(args[1])) {
                runMonitoringSystem(backend);
            } else if ("l".equals(args[1])) {
                runLoggingSystem(backend);
            } else if ("d".equals(args[1])) {
                runDoorBellSystem(backend);
            }
        } else {
            System.out.println("usage: java Bricks http|mqtt|mock m|l|d");
        }
    }
}