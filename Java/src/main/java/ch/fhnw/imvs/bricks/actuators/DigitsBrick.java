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

    private volatile int currentDecimalPlaces = 2;
    private volatile int targetDecimalPlaces = 2;

    private volatile double currentDoubleValue = Double.MIN_VALUE;
    private volatile double targetDoubleValue = Double.MIN_VALUE;

    //public int getDecimalPlaces() {
    //    return currentDecimalPlaces;
    //}

    public void setDecimalPlaces(int value) {
        if (value != 0 && value != 2) {
            throw new IllegalArgumentException();
        }
        if (targetDecimalPlaces != value) {
            targetDecimalPlaces = value;
            super.sync();
        }
    }

    //public double getDoubleValue() {
    //    return currentDoubleValue;
    //}

    public void setDoubleValue(double value) {
        if (targetDoubleValue != value) {
            targetDoubleValue = value;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        ByteBuffer buf = ByteBuffer.allocate(mock ? 5 : 3);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        if (mock) {
            double mockBatt = Math.random() * 3.7;
            buf.putShort((short) (mockBatt * 100));
        }
        buf.put((byte) targetDecimalPlaces);
        buf.putShort((short) (targetDoubleValue * Math.pow(10, targetDecimalPlaces)));
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0);
        currentDecimalPlaces = buf.get();
        currentDoubleValue = buf.getShort() / Math.pow(10, currentDecimalPlaces);
    }

    public static DigitsBrick connect(Proxy proxy, String brickID) {
        DigitsBrick brick = new DigitsBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
