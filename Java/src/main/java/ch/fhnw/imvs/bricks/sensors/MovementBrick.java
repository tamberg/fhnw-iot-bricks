// Copyright (c) 2024 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.DigitalInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class MovementBrick extends DigitalInputBrick {
    private MovementBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public boolean isActive() {
        return super.isActive();
    }

    public static MovementBrick connect(Proxy proxy, String brickID) {
        MovementBrick brick = new MovementBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
