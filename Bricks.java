import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

enum UpdateMode { DEMO, LIVE }

final class Message {
    public Date getDateValue(String key) {
        return new Date();
    }

    public double getDoubleValue(String key) {
        return 0.0;
    }

    public int getIntValue(String key) {
        return 0;
    }

    public String getStringValue(String key) {
        return "";
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
}

final class LedStripBrick extends Brick {
    LedStripBrick(String token) {
        super(token);
    }

    @Override
    protected final void handleUpdate2(Message message) {}

    @Override
    protected final void updateCurrentValues2() {}

    public void setColors(Color[] values) {}
}

final class TemperatureBrick extends Brick {
    double currentTemp;
    double nextTemp;
    double currentHumi;
    double nextHumi;

    TemperatureBrick(String token) {
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
}

final class LcdDisplayBrick extends Brick {
    double targetValue = 0; // TODO: naming
    double currentValue = 0;
    double nextValue = 0;

    LcdDisplayBrick(String token) {
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
}

public final class Bricks {
    static Map<String, Brick> bricks = new HashMap<String, Brick>();
    static UpdateMode updateMode = UpdateMode.DEMO;

    // Backend
    public static void setBackendHost(String host) {}
    public static void setBackendUser(String user) {}
    public static void setBackendPassword(String password) {}

    // Updates
    public static UpdateMode getUpdateMode() {
        return updateMode;
    }

    public static void setUpdateMode(UpdateMode mode) {
        updateMode = mode;
    }

    public static void handleUpdate(Message message) { // thread
        // lock
        for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
            Brick brick = entry.getValue();
            brick.handleUpdate(message);
        }
    }

    public static void update() { // blocking
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
                int interval = (updateMode == UpdateMode.DEMO) ? 1 : 5;
                try {
                    TimeUnit.MINUTES.sleep(interval);
                } catch (InterruptedException e) {}
            }
        }
        // Update
        for (Map.Entry<String, Brick> entry : bricks.entrySet()) {
            Brick brick = entry.getValue();
            brick.updateCurrentValues(); // updates 
        }
    }

    // Bricks
    public static ButtonBrick getButtonBrick(String token) {
        ButtonBrick result;
        Brick brick = bricks.get(token);
        if (brick == null) {
            //BrickConfig config = null; // TODO: get config via token
            //if (config.getType() == "TemperatureBrick") {
            result = new ButtonBrick(token);
            bricks.put(token, result);
            //} else {
            //    result = null;
            //}
        } else {
            try {
                result = (ButtonBrick) brick;
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    public static LcdDisplayBrick getLcdDisplayBrick(String token) {
        LcdDisplayBrick result;
        Brick brick = bricks.get(token);
        if (brick == null) {
            //BrickConfig config = null; // TODO: get config via token
            //if (config.getType() == "TemperatureBrick") {
            result = new LcdDisplayBrick(token);
            bricks.put(token, result);
            //} else {
            //    result = null;
            //}
        } else {
            try {
                result = (LcdDisplayBrick) brick;
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    public static LedBrick getLedBrick(String token) {
        LedBrick result;
        Brick brick = bricks.get(token);
        if (brick == null) {
            //BrickConfig config = null; // TODO: get config via token
            //if (config.getType() == "TemperatureBrick") {
            result = new LedBrick(token);
            bricks.put(token, result);
            //} else {
            //    result = null;
            //}
        } else {
            try {
                result = (LedBrick) brick;
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }
    
    public static LedStripBrick getLedStripBrick(String token) {
        LedStripBrick result;
        Brick brick = bricks.get(token);
        if (brick == null) {
            //BrickConfig config = null; // TODO: get config via token
            //if (config.getType() == "TemperatureBrick") {
            result = new LedStripBrick(token);
            bricks.put(token, result);
            //} else {
            //    result = null;
            //}
        } else {
            try {
                result = (LedStripBrick) brick;
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    public static TemperatureBrick getTemperatureBrick(String token) {
        TemperatureBrick result;
        Brick brick = bricks.get(token);
        if (brick == null) {
            //BrickConfig config = null; // TODO: get config via token
            //if (config.getType() == "TemperatureBrick") {
            result = new TemperatureBrick(token);
            bricks.put(token, result);
            //} else {
            //    result = null;
            //}
        } else {
            try {
                result = (TemperatureBrick) brick;
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    public static void runMonitoringSystem() {
        TemperatureBrick tempBrick = Bricks.getTemperatureBrick("TEMP_BRICK_TOKEN");
        LcdDisplayBrick displayBrick = Bricks.getLcdDisplayBrick("DISPLAY_BRICK_TOKEN");

        while (true) {
            double temp = tempBrick.getTemperature();
            displayBrick.setValue(temp);
            Bricks.update();
        }
    }

    public static void runLoggingSystem() {
        TemperatureBrick tempBrick = Bricks.getTemperatureBrick("TEMP_BRICK_TOKEN");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("log.csv", true); // append
        } catch (IOException e) {}

        int i = 0;
        while (true) {
            double temp = tempBrick.getTemperature();
            Date time = tempBrick.getLastUpDatestamp();
            try {
                fileWriter.append(time + ", " + temp + "\n");
            } catch (IOException e) {}
            Bricks.update();
        }
    }

    public static void runDoorBellSystem() {
        ButtonBrick buttonBrick = Bricks.getButtonBrick("BUTTON_BRICK_TOKEN");
        LedBrick ledBrick = Bricks.getLedBrick("LED_BRICK_TOKEN");

        while (true) {
            boolean pressed = buttonBrick.getPressed();
            ledBrick.setColor(pressed ? Color.RED : Color.BLACK);
            Bricks.update();
        }
    }

    public static void main(String args[]) {
        System.out.println("Bricks.main()");
        if (args.length == 1) {
            Bricks.setBackendHost("FHNW_IOT_BRICKS_HOST");
            Bricks.setBackendUser("FHNW_IOT_BRICKS_USER");
            Bricks.setBackendPassword("FHNW_IOT_BRICKS_PASSWORD");
            Bricks.setUpdateMode(UpdateMode.LIVE);

            if ("m".equals(args[0])) {
                runMonitoringSystem();
            } else if ("l".equals(args[0])) {
                runLoggingSystem();
            } else if ("d".equals(args[0])) {
                runDoorBellSystem();
            }
        }
    }
}