// Copyright (c) 2023 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.awt.Image;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class ImageBrick extends Brick {
    private ImageBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private Image targetImage = null;

    public void show(Image i) {
        if (!targetImage.equals(i)) {
            targetImage = i;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        assert !mock; // actuator
        ByteBuffer buf = ByteBuffer.allocate(3);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        // TODO
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) { assert false; } // actuator

    public static ImageBrick connect(Proxy proxy, String brickID) {
        ImageBrick brick = new ImageBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
