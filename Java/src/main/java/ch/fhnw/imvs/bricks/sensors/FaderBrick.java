// Copyright (c) 2024 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.AnalogInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class FaderBrick extends AnalogInputBrick {
    private FaderBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public int getPosition() {
        return super.getValueMapped(0, 10);
    }

    public static FaderBrick connect(Proxy proxy, String brickID) {
        FaderBrick brick = new FaderBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
