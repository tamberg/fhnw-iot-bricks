// Copyright (c) 2023 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.AnalogInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class RotAngleBrick extends AnalogInputBrick {
    // https://stackoverflow.com/questions/7505991/arduino-map-equivalent-function-in-java
    private static int map(int x, int min, int max, int min2, int max2) {
        return (x - min) * (max2 - min2) / (max - min) + min2;
    }

    private RotAngleBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public int getAngle() { // degrees
        int value = map(super.getValue(), 0, 1024, 0, 360); // TODO: config?
        return value;
    }

    public static RotAngleBrick connect(Proxy proxy, String brickID) {
        RotAngleBrick brick = new RotAngleBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
