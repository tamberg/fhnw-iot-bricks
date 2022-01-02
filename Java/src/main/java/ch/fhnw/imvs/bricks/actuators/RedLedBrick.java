// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import ch.fhnw.imvs.bricks.impl.DigitalOutputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class RedLedBrick extends DigitalOutputBrick {
    private RedLedBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

//    public boolean isEnabled() {
//        return super.isActive();
//    }

    public void setEnabled(boolean enabled) {
        super.setActive(enabled);
    }

    public static RedLedBrick connect(Proxy proxy, String brickID) {
        RedLedBrick brick = new RedLedBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
