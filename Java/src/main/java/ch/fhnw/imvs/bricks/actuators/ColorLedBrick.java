// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class ColorLedBrick extends Brick {
    private ColorLedBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile Color targetColor = Color.BLACK;

    public void setColor(Color color) {
        if (!targetColor.equals(color)) {
            targetColor = color;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        assert !mock; // actuator
        ByteBuffer buf = ByteBuffer.allocate(3);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        int r = targetColor.getRed();
        int g = targetColor.getGreen();
        int b = targetColor.getBlue();
        buf.put((byte) r);
        buf.put((byte) g);
        buf.put((byte) b);
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) { assert false; } // actuator

    public static ColorLedBrick connect(Proxy proxy, String brickID) {
        ColorLedBrick brick = new ColorLedBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
