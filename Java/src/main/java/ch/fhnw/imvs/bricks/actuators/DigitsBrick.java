// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class DigitsBrick extends Brick {
    private DigitsBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile int targetDecimalPlaces = 2;
    private volatile double targetDoubleValue = Double.MIN_VALUE;

    public void setDecimalPlaces(int value) {
        if (value != 0 && value != 2) {
            throw new IllegalArgumentException();
        }
        if (targetDecimalPlaces != value) {
            targetDecimalPlaces = value;
            super.sync();
        }
    }

    public void setDoubleValue(double value) {
        if (targetDoubleValue != value) {
            targetDoubleValue = value;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        assert !mock; // actuator
        ByteBuffer buf = ByteBuffer.allocate(3);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        buf.put((byte) targetDecimalPlaces);
        buf.putShort((short) (targetDoubleValue * Math.pow(10, targetDecimalPlaces)));
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) { assert false; } // actuator

    public static DigitsBrick connect(Proxy proxy, String brickID) {
        DigitsBrick brick = new DigitsBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
