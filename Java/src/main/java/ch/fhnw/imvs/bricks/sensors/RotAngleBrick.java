// Copyright (c) 2023 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.AnalogInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class RotAngleBrick extends AnalogInputBrick {
    private RotAngleBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public int getAngle() { // degrees
        return super.getValueMapped(0, 360);
    }

    public static RotAngleBrick connect(Proxy proxy, String brickID) {
        RotAngleBrick brick = new RotAngleBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
