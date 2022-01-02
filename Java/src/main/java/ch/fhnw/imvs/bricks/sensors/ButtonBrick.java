// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import ch.fhnw.imvs.bricks.impl.DigitalInputBrick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class ButtonBrick extends DigitalInputBrick {
    private ButtonBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    public boolean isPressed() {
        return super.isActive();
    }

    public static ButtonBrick connect(Proxy proxy, String brickID) {
        ButtonBrick brick = new ButtonBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
