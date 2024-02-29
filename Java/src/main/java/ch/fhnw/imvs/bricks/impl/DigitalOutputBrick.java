// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public abstract class DigitalOutputBrick extends Brick {
    protected DigitalOutputBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile boolean targetActive = false;

    protected void setActive(boolean value) {
        if (targetActive != value) {
            targetActive = value;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        assert !mock; // actuator
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        buf.put((byte) (targetActive ? 1 : 0));
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) { assert false; } // actuator
}
