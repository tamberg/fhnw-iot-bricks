// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public abstract class AnalogInputBrick extends Brick {
    // https://stackoverflow.com/questions/7505991/arduino-map-equivalent-function-in-java
    private static int map(int x, int min, int max, int min2, int max2) {
        return (x - min) * (max2 - min2) / (max - min) + min2;
    }

    protected AnalogInputBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile int currentValue;

    protected int getValue() {
        return currentValue;
    }

    protected int getValueMapped(int from, int to) {
        return map(currentValue, 0, 1024, from, to);
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        assert mock; // sensor
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        double mockBatt = Math.random() * 3.7;
        double mockValue = Math.random() * 1024;
        buf.putShort((short) (mockBatt * 100));
        buf.putShort((short) mockValue);
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0);
        currentValue = buf.getShort();
    }
}
