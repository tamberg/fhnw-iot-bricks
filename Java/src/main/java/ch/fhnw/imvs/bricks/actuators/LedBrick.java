// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class LedBrick extends Brick {
    private LedBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile Color currentColor = Color.BLACK;
    private volatile Color targetColor = Color.BLACK;

    public Color getColor() {
        return currentColor;
    }

    public void setColor(Color color) {
        if (!targetColor.equals(color)) {
            targetColor = color;
            super.sync();
        }
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentColor = Color.decode(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        try {
            int mockBatt = (int) (Math.random() * 99 + 1);
            int r = targetColor.getRed();
            int g = targetColor.getGreen();
            int b = targetColor.getBlue();
            String payloadString = 
                (mock ? Integer.toString(mockBatt) + SEPARATOR : "") +
                String.format("#%02x%02x%02x", r, g, b);
            payload = payloadString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static LedBrick connect(Proxy proxy, String brickID) {
        LedBrick brick = new LedBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}
