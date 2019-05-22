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
class Backend {
    void setHost(string host);
    void setUser(string user);
    Brick getBrick(string token);
}

class Brick {
    int getBatteryLevel();
}

class TemperatureBrick extends Brick {
    double getValue();
    DateTime getTimestamp();
}

class DisplayBrick extends Brick {
    void setValue(double Value);
}
```

```
TemperatureBrick tempBrick = (TemperatureBrick) Backend.getBrick("TOKEN_PRINTED_ON_TEMP_BRICK");
DisplayBrick displayBrick = (DisplayBrick) Backend.getBrick("TOKEN_PRINTED_ON_DIPLAY_BRICK");
while (min(tempBrick.getBatteryLevel(), displayBrick.getBatteryLevel()) > 20) {
    double temp = tempBrick.getValue();
    displayBrick.setValue(temp);
    TimeUnit.MINUTES.sleep(1);
}
```
