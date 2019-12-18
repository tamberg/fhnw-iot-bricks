# FHNW IoT Bricks
> Work in progress. Interested? Contact thomas.amberg@fhnw.ch
## Building blocks for distributed IoT use cases
IoT Bricks enable IoT prototyping in a room, building or city.
## Simple, self-contained, connected
IoT Bricks come with long range connectivity and a simple SDK.
## Hardware example
<img src="IoTBrickTemperature.jpg"/>

## Software example
### Interface
```
public enum UpdateMode { DEMO, LIVE }

public final class Bricks {
    // Backend
    public static void setBackendHost(String host);
    public static void setBackendUser(String user);
    public static void setBackendPassword(String password);
    // Updates
    public static UpdateMode getUpdateMode();
    public static void setUpdateMode(UpdateMode mode);
    public static void update(); // blocking
    // Bricks
    public static ButtonBrick getButtonBrick(String token);
    public static LcdDisplayBrick getLcdDisplayBrick(String token);
    public static LedBrick getLedBrick(String token);
    public static LedStripBrick getLedStripBrick(String token);
    public static TemperatureBrick getTemperatureBrick(String token);
}

public abstract class Brick {
    public int getBatteryLevel();
    public DateTime getLastUpdateTimestamp();
}

public final class ButtonBrick extends Brick {
    public boolean getPressed();
}

public final class LedPixelBrick extends Brick {
    public void setColor(Color value);
}

public final class LedStripBrick extends Brick {
    public void setColors(Color[] values);
}

public final class LcdDisplayBrick extends Brick {
    public void setValue(double value);
}

public final class TemperatureBrick extends Brick {
    public double getHumidity();
    public double getTemperature();
}
```
### Backend & Update Config
```
Bricks.setBackendHost("FHNW_IOT_BRICKS_HOST");
Bricks.setBackendUser("FHNW_IOT_BRICKS_USER");
Bricks.setBackendPassword("FHNW_IOT_BRICKS_PASSWORD");
Bricks.setUpdateMode(UpdateMode.LIVE);
```
### Monitoring System
```
TemperatureBrick tempBrick = Bricks.getTemperatureBrick("TEMP_BRICK_TOKEN");
LcdDisplayBrick displayBrick = Bricks.getLcdDisplayBrick("DISPLAY_BRICK_TOKEN");

while (true) {
    double temp = tempBrick.getTemperature();
    displayBrick.setValue(temp);
    Bricks.update();
}
```

### Logging System
```
TemperatureBrick tempBrick = Bricks.getTemperatureBrick("TEMP_BRICK_TOKEN");
FileWriter fileWriter = new FileWriter("log.csv", true); // append

int i = 0;
while (true) {
    double temp = tempBrick.getTemperature();
    DateTime time = tempBrick.getLastUpdateTimestamp();
    fileWriter.append(stamp + ", " + time + "\n");
    Bricks.update();
}
```

### Door Bell
```
ButtonBrick buttonBrick = Backend.getButtonBrick("BUTTON_BRICK_TOKEN");
LedBrick ledBrick = Backend.getLedBrick("LED_BRICK_TOKEN");

while (true) {
    boolean pressed = buttonBrick.getPressed();
    ledBrick.setColor(pressed ? Color.Red : Color.Black);
    Bricks.update();
}
```
