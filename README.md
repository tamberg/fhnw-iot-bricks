# FHNW IoT Bricks
> Work in progress. Interested? Contact thomas.amberg@fhnw.ch
## Building blocks for IoT use cases
IoT Bricks enable IoT prototyping in a room, building or city.
## Simple, self-contained, connected
IoT Bricks come with long range connectivity and a simple SDK.
## Hardware example

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

TemperatureBrick b = new TemperatureBrick();
b.getValue();
b.getBattery();
```
