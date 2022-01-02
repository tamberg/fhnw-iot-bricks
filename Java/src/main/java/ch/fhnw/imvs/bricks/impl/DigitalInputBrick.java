// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public abstract class DigitalInputBrick extends Brick {
    protected DigitalInputBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile boolean currentActive;

    protected boolean isActive() {
        return currentActive;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            ByteBuffer buf = ByteBuffer.allocate(3);
            buf.order(ByteOrder.BIG_ENDIAN); // network byte order
            double mockBatt = Math.random() * 3.7;
            boolean mockActive = Math.random() < 0.5;
            buf.putShort((short) (mockBatt * 100));
            buf.put((byte) (mockActive ? 1 : 0));
            payload = buf.array();
        }
        return payload;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0);
        currentActive = buf.get() != 0;
    }
}
