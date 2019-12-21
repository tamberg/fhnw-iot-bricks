import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ClassCastException;
import java.lang.String;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

// https://www.planttext.com/
//
// @startuml
//
// title Relationships - Class Diagram
//
// class Backend {
//   void add(Brick b)
//   +void update()
// }
//
// class Brick {
//   +void setBackend(Backend b)
// }
//
// class TempBrick {
//   +double getTemp()
//   static +TempBrick create(String token)
// }
// class LedBrick {
//   +setColor(Color c);
//   static +LedBrick create(Backend b, String token)
// }
// class LedStripBrick {
//   +setColors(Color[] c);
//   static +LedStripBrick create(Backend b, String token)
// }
// class Connection
//
// Brick <|-down- TempBrick: Inheritance
// Brick <|-down- LedBrick: Inheritance
// Brick <|-down- LedStripBrick: Inheritance
// Backend "1" -up- "1" Connection: Composition
// Backend "1" *-down- "many" Brick: Composition 
//
// @enduml

final class Message {
    Map<String, Date> dates = new HashMap<String, Date>();
    Map<String, Double> doubles = new HashMap<String, Double>();
    Map<String, Integer> integers = new HashMap<String, Integer>();
    Map<String, String> strings = new HashMap<String, String>();

    void addDateValue(String key, Date value) {
        dates.put(key, value);
    }

    void addDoubleValue(String key, double value) {
        doubles.put(key, value);
    }

    void addIntValue(String key, int value) {
        integers.put(key, value);
    }

    void addStringValue(String key, String value) {
        strings.put(key, value);
    }

    public Date getDateValue(String key) {
        return dates.get(key);
    }

    public double getDoubleValue(String key) {
        return doubles.get(key);
    }

    public int getIntValue(String key) {
        return integers.get(key);
    }

    public String getStringValue(String key) {
        return strings.get(key);
    }
}

abstract class Brick {
    String token;
    Date currentTimestamp = new Date(Long.MIN_VALUE);
    Date nextTimestamp = new Date(Long.MIN_VALUE);
    int currentBatteryLevel = 0;
    int nextBatteryLevel = 0;

    Brick(String token) {
        token = token;
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
            nextBatteryLevel = message.getIntValue("battery");
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

    public Date getLastUpDatestamp() {
        return currentTimestamp;
    }
}

final class ButtonBrick extends Brick {
    ButtonBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public boolean getPressed() { return false; }

    public static ButtonBrick create(Backend backend, String token) {
        ButtonBrick brick = new ButtonBrick(token);
        backend.addBrick(brick);
        return brick;
    }
}

final class LedBrick extends Brick {
    LedBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public void setColor(Color value) {}

    public static LedBrick create(Backend backend, String token) {
        LedBrick brick = new LedBrick(token);
        backend.addBrick(brick);
        return brick;
    }
}

final class LedStripBrick extends Brick {
    private LedStripBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public void setColors(Color[] values) {}

    public static LedStripBrick create(Backend backend, String token) {
        LedStripBrick brick = new LedStripBrick(token);
        backend.addBrick(brick);
        return brick;
    }
}

final class TemperatureBrick extends Brick {
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

    public static TemperatureBrick create(Backend backend, String token) {
        TemperatureBrick brick = new TemperatureBrick(token);
        backend.addBrick(brick);
        return brick;
    }
}

final class LcdDisplayBrick extends Brick {
    double targetValue = 0; // TODO: naming
    double currentValue = 0;
    double nextValue = 0;

    private LcdDisplayBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {
    }

    @Override
    protected final void updateCurrentValues2() {
        currentValue = nextValue;
    }

    public void setValue(double value) {
        // MQTT PUB or HTTP PUT later / now / thread?
        if (targetValue != value) {
            targetValue = value;
        }
    }

    public static LcdDisplayBrick create(Backend backend, String token) {
        LcdDisplayBrick brick = new LcdDisplayBrick(token);
        backend.addBrick(brick);
        return brick;
    }
}

/*
- Ist Bricks.update ein guter Name?
- Macht getBatteryLevel in Brick Sinn? Kann es nicht auch Bricks mit Stromversorgung geben?
- Im TemperatureBrick hat es double Werte, was ja immer etwas heikel ist. Könnte man die auch als long modellieren?
*/

abstract class Backend {
    Map<String, Brick> bricks = new HashMap<String, Brick>();

    /* package */ public void addBrick(Brick brick) {
        // TODO: throws IllegalArgumentException ?
        if (!bricks.containsValue(brick) && 
            !bricks.containsKey(brick.getToken())) {
            bricks.put(brick.getToken(), brick);
        }
    }

    // Updates
    protected final void handleUpdate(Message message) { // thread
        // lock
        for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
            Brick brick = entry.getValue();
            // TODO: check if message is for this brick / token
            brick.handleUpdate(message);
        }
    }

    public final void update() { // blocking
        System.out.print("Backend.update()");
        Date now = new Date();
        // await handleMessage?
        // lock
        // Wait for updates
        boolean updated = false;
        while (!updated) {
            for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
                Brick brick = entry.getValue();
                updated = updated || now.before(brick.getNextTimestamp());
            }
            if (!updated) {
                System.out.print(".");
                try {
                    TimeUnit.SECONDS.sleep(3); // TODO: better heuristic
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // Update
        for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
            Brick brick = entry.getValue();
            brick.updateCurrentValues();
        }
        System.out.println();
    }
}

final class HttpBackend extends Backend {
    HttpBackend(String host, String apiToken) {
        // create
        // run Thread that 
        //  connects to HTTP backend
        //  calls super.handleUpdate(message)?
    }
}

final class MqttBackend extends Backend {
    // private MqttConnection connection;

    MqttBackend(String host, String user, String password) {
        // create
        // run Thread that 
        //  connects to MQTT backend
        //  calls super.handleUpdate(message)?
    }
}     

final class MockBackend extends Backend implements Runnable {
    int updateFrequencySeconds;

    MockBackend(int updateFrequencySeconds) {
        this.updateFrequencySeconds = updateFrequencySeconds;
        new Thread(this).start(); // TODO: bad style? Move to start() ?
    }

    public void run() {
        while (true) {
            Message message0 = new Message();
            // TODO: get existing tokens from base class
            message0.addStringValue("token", "TEMP_BRICK_TOKEN");
            message0.addDateValue("timestamp", new Date());
            message0.addIntValue("battery", 100);
            message0.addDoubleValue("humidity", 42);
            message0.addDoubleValue("temperature", 23);
            super.handleUpdate(message0);

            Message message1 = new Message();
            message1.addStringValue("token", "DISPLAY_BRICK_TOKEN");
            message1.addDateValue("timestamp", new Date());
            message1.addIntValue("battery", 50);
            super.handleUpdate(message1);

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
        TemperatureBrick tempBrick = TemperatureBrick.create(backend, "TEMP_BRICK_TOKEN");
        LcdDisplayBrick displayBrick = LcdDisplayBrick.create(backend, "DISPLAY_BRICK_TOKEN");

        while (true) {
            double temp = tempBrick.getTemperature();
            displayBrick.setValue(temp);
            backend.update();
        }
    }

    static void runLoggingSystem(Backend backend) {
        TemperatureBrick tempBrick = TemperatureBrick.create(backend, "TEMP_BRICK_TOKEN");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("log.csv", true); // append
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;
        while (true) {
            double temp = tempBrick.getTemperature();
            Date time = tempBrick.getLastUpDatestamp();
            try {
                fileWriter.append(time + ", " + temp + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            backend.update();
        }
    }

    static void runDoorBellSystem(Backend backend) {
        ButtonBrick buttonBrick = ButtonBrick.create(backend, "BUTTON_BRICK_TOKEN");
        LedBrick ledBrick = LedBrick.create(backend, "LED_BRICK_TOKEN");

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
                backend = new HttpBackend("TTN_HTTP_HOST", "TTN_HTTP_API_TOKEN");
            } else if ("mqtt".equals(args[0])) {
                backend = new MqttBackend("TTN_MQTT_HOST", "TTN_MQTT_USER", "TTN_MQTT_PASSWORD");
            } else if ("mock".equals(args[0])) {
                backend = new MockBackend(1); // s
            }

            if ("m".equals(args[1])) {
                runMonitoringSystem(backend);
            } else if ("l".equals(args[1])) {
                runLoggingSystem(backend);
            } else if ("d".equals(args[1])) {
                runDoorBellSystem(backend);
            }
        } else {
            System.out.println("usage: java Bricks http|mqtt m|l|d");
        }
    }
}