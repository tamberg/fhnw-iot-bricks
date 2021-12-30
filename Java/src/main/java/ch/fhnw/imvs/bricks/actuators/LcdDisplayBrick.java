// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class LcdDisplayBrick extends Brick {
    private LcdDisplayBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile double currentValue = 0.0;
    private volatile double targetValue = 0.0;

    public double getDoubleValue() {
        return currentValue;
    }

    public void setDoubleValue(double value) {
        if (targetValue != value) {
            targetValue = value;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        ByteBuffer buf = ByteBuffer.allocate(mock ? 4 : 2);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        if (mock) {
            float mockBatt = (float) (Math.random() * 3.7 + 1);
            buf.putShort((short) (mockBatt * 100.0f));
        }
        buf.putShort((short) (targetValue * 100.0f));
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryLevel(buf.getShort() / 100.0f);
        currentValue = buf.getShort() / 100.0f;
    }

    public static LcdDisplayBrick connect(Proxy proxy, String brickID) {
        LcdDisplayBrick brick = new LcdDisplayBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
