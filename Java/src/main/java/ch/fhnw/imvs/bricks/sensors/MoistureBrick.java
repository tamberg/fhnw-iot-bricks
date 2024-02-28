// Copyright (c) 2024 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.AnalogInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class MoistureBrick extends AnalogInputBrick {
    private MoistureBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public int getMoisture() {
        return super.getValueMapped(0, 100);
    }

    public static MoistureBrick connect(Proxy proxy, String brickID) {
        MoistureBrick brick = new MoistureBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
