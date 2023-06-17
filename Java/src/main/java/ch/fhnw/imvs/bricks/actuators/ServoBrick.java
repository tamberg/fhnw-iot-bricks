// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class ServoBrick extends Brick {
    private ServoBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile int currentPosition = 0;
    private volatile int targetPosition = 0;

    public int getPosition() {
        return currentPosition;
    }

    public void setPosition(int position) { // degree
        if (position < 0 || position > 180) {
            throw new IllegalArgumentException();
        }
        if (targetPosition != position) {
            targetPosition = position;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        ByteBuffer buf = ByteBuffer.allocate(mock ? 4 : 2);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        if (mock) {
            double mockBatt = Math.random() * 3.7;
            buf.putShort((short) (mockBatt * 100));
        }
        buf.putShort((short) targetPosition);
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0);
        currentPosition = buf.getShort();
    }

    public static ServoBrick connect(Proxy proxy, String brickID) {
        ServoBrick brick = new ServoBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
