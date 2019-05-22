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
    int getValue();
    int getResolution();
    DateTime getTimestamp();
}

TemperatureBrick brick = Backend.getBrick("TOKEN_PRINTED_ON_BRICK");
double temperature = brick.getValue() / brick.getResolution();
int percent = brick.getBatteryLevel();
```
