// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import ch.fhnw.imvs.bricks.impl.DigitalOutputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class RelayBrick extends DigitalOutputBrick {
    private RelayBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public void setEnabled(boolean enabled) {
        super.setActive(enabled);
    }

    public static RelayBrick connect(Proxy proxy, String brickID) {
        RelayBrick brick = new RelayBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
