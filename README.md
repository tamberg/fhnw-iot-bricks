# FHNW IoT Bricks
> Work in progress. Interested? Contact thomas.amberg@fhnw.ch
## Building blocks for IoT use cases
IoT Bricks enable IoT prototyping in a room, building or city.
## Simple, self-contained, connected
IoT Bricks come with long range connectivity and a simple SDK.
## Hardware example
<img src="IoTBrickTemperature.jpg"/>

## Software example

```
public final class Backend {
    // Config
    public static void setHost(string host);
    public static void setUser(string user);
    public static void setPassword(string password);
    // Bricks
    public static ButtonBrick createButtonBrick(string token);
    public static LcdDisplayBrick createLcdDisplayBrick(string token);
    public static LedBrick createLed(string token);
    public static LedStripBrick createLedStripBrick(string token);
    public static TemperatureBrick createTemperatureBrick(string token);
}

public abstract class Brick {
    public int getBatteryLevel();
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
    public void setValue(double Value);
}

public final class TemperatureBrick extends Brick {
    public double getHumidity();
    public double getTemperature();
    public DateTime getTimestamp();
}
```

```
Backend.setHost("FHNW_IOT_BRICKS_HOST");
Backend.setUser("FHNW_IOT_BRICKS_USER");
Backend.setPassword("FHNW_IOT_BRICKS_PASSWORD");

TemperatureBrick tempBrick = Backend.createTemperatureBrick("TOKEN_PRINTED_ON_TEMP_BRICK");
LcdDisplayBrick displayBrick = Backend.createLcdDisplayBrick("TOKEN_PRINTED_ON_DISPLAY_BRICK");

while (true) {
    double temp = tempBrick.getTemperature();
    displayBrick.setValue(temp);
    TimeUnit.MINUTES.sleep(1);
}
```
