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
    setHost(string host);
    setUser(string user);
    Brick getBrick(string token);
}
		
class Brick {
    getBattery();
}

class TemperatureBrick extends Brick {
    int getValue();
    int getResolution();
    DateTime getTimestamp();
}

TemperatureBrick b = Backend.getBrick("TOKEN_PRINTED_ON_BRICK");
double temperature = b.getValue() / b.getResolution();
int percent = b.getBattery();
```
