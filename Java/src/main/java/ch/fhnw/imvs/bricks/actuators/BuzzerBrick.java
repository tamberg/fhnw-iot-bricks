// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import ch.fhnw.imvs.bricks.impl.DigitalOutputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class BuzzerBrick extends DigitalOutputBrick {
    private BuzzerBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

//    public boolean isEnabled() {
//        return super.isActive();
//    }

    public void setEnabled(boolean enabled) {
        super.setActive(enabled);
    }

    public static BuzzerBrick connect(Proxy proxy, String brickID) {
        BuzzerBrick brick = new BuzzerBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
