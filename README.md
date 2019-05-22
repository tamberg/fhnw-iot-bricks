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
    public static void setHost(string host);
    public static void setUser(string user);
    public static void setPassword(string password);
    public static Brick getBrick(string token);
}

public abstract class Brick {
    public int getBatteryLevel();
}

public final class TemperatureBrick extends Brick {
    public double getValue();
    public DateTime getTimestamp();
}

public final class DisplayBrick extends Brick {
    public void setValue(double Value);
}
```

```
Backend.setHost("FHNW_IOT_BRICKS_HOST");
Backend.setUser("FHNW_IOT_BRICKS_USER");
Backend.setPassword("FHNW_IOT_BRICKS_PASSWORD");

TemperatureBrick tempBrick = (TemperatureBrick) Backend.getBrick("TOKEN_PRINTED_ON_TEMP_BRICK");
DisplayBrick displayBrick = (DisplayBrick) Backend.getBrick("TOKEN_PRINTED_ON_DIPLAY_BRICK");

while (Math.min(tempBrick.getBatteryLevel(), displayBrick.getBatteryLevel()) > 20) {
    double temp = tempBrick.getValue();
    displayBrick.setValue(temp);
    TimeUnit.MINUTES.sleep(1);
}
```
