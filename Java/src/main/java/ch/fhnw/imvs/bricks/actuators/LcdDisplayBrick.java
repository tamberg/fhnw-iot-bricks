// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class LcdDisplayBrick extends Brick {
    private LcdDisplayBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
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
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentValue = Double.parseDouble(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        try {
            int mockBatt = (int) (Math.random() * 99 + 1);
            String payloadString = 
                (mock ? Integer.toString(mockBatt) + SEPARATOR : "") +
                Double.toString(targetValue);
            payload = payloadString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static LcdDisplayBrick connect(Proxy proxy, String brickID) {
        LcdDisplayBrick brick = new LcdDisplayBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
