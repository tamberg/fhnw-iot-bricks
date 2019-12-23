# FHNW IoT Bricks
> Work in progress. Interested? Contact thomas.amberg@fhnw.ch
## Building blocks for distributed IoT use cases
IoT Bricks enable IoT prototyping in a room, building or city.
## Simple, self-contained, connected
IoT Bricks come with long range connectivity and a simple SDK.
## Hardware example
<img src="IoTBrickTemperature.jpg"/>

[IoT Brick Temperature](https://www.thingiverse.com/thing:3638252) on Thingiverse.

## Software example
### Interface
```
public abstract class Brick {
    public String getToken();
    public int getBatteryLevel();
    public Date getTimestamp();
    public String getTimestampIsoUtc();
}

public final class ButtonBrick extends Brick {
    public boolean getPressed();
    public static ButtonBrick connect(BackendProxy backend, String token);
}

public final class LedBrick extends Brick {
    public void setColor(Color value);
    public static LedBrick connect(BackendProxy backend, String token);
}

public final class LedStripBrick extends Brick {
    public void setColors(Color[] values);
    public static LedStripBrick connect(BackendProxy backend, String token);
}

public final class TemperatureBrick extends Brick {
    public double getHumidity();
    public double getTemperature();
    public static TemperatureBrick connect(BackendProxy backend, String token);
}

public final class LcdDisplayBrick extends Brick;
    public void setDoubleValue(double value);
    public double getDoubleValue();
    public static LcdDisplayBrick connect(BackendProxy backend, String token);
}

public abstract class BackendProxy {
    public final void waitForUpdate();
}

public final class HttpBackendProxy extends BackendProxy {
    public HttpBackendProxy(String host, String apiToken);
}

public final class MqttBackendProxy extends BackendProxy {
    public MqttBackendProxy(String host, String user, String password);
}     

public final class MockBackendProxy extends BackendProxy {
    public MockBackendProxy(int updateFrequencySeconds);
}
```
### BackendProxy Config
```
// BackendProxy proxy = new HttpBackendProxy("HTTP_HOST", "HTTP_API_TOKEN");
// BackendProxy proxy = new MqttBackendProxy("MQTT_HOST", "MQTT_USER", "MQTT_PASSWORD");
BackendProxy proxy = new MockBackendProxy(5); // s
proxy.start();
```
### Monitoring System
```
TemperatureBrick tempBrick = TemperatureBrick.connect(proxy, "TEMP_BRICK_TOKEN");
LcdDisplayBrick displayBrick = LcdDisplayBrick.connect(proxy, "LCD_DISPLAY_BRICK_TOKEN");
LedBrick ledBrick = LedBrick.connect(proxy, "LED_BRICK_TOKEN");

while (true) {
    double temp = tempBrick.getTemperature();
    displayBrick.setDoubleValue(temp);
    Color color = temp > 23 ? Color.RED : Color.GREEN;
    ledBrick.setColor(color);
    proxy.waitForUpdate();
}
```

### Logging System
```
TemperatureBrick tempBrick = TemperatureBrick.connect(proxy, "TEMP_BRICK_TOKEN");
FileWriter fileWriter = null;
try {
    fileWriter = new FileWriter("log.csv", true); // append
} catch (IOException e) {
    e.printStackTrace();
}

while (true) {
    double temp = tempBrick.getTemperature();
    String time = tempBrick.getTimestampIsoUtc();
    try {
        fileWriter.append(time + ", " + temp + "\n");
        fileWriter.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
    proxy.waitForUpdate();
}
```

### Door Bell
```
ButtonBrick buttonBrick = ButtonBrick.connect(proxy, "BUTTON_BRICK_TOKEN");
BuzzerBrick buzzerBrick = BuzzerBrick.connect(proxy, "BUZZER_BRICK_TOKEN");

while (true) {
    boolean pressed = buttonBrick.getPressed();
    buzzerBrick.setEnabled(pressed);
    proxy.waitForUpdate();
}
```
