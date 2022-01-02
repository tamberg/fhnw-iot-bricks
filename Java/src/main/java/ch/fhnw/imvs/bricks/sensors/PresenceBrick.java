// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.DigitalInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class PresenceBrick extends DigitalInputBrick {
    private PresenceBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public boolean isActive() {
        return super.isActive();
    }

    public static PresenceBrick connect(Proxy proxy, String brickID) {
        PresenceBrick brick = new PresenceBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
