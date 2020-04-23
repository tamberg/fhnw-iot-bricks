// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class ButtonBrick extends Brick {
    private ButtonBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile boolean currentPressed;

    public boolean isPressed() {
        return currentPressed;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentPressed = Boolean.parseBoolean(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            int mockBatt = (int) (Math.random() * 99 + 1);
            boolean mockPressed = Math.random() < 0.5;
            try {
                String payloadString = 
                    Integer.toString(mockBatt) + SEPARATOR +
                    Boolean.toString(mockPressed);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    public static ButtonBrick connect(Proxy proxy, String brickID) {
        ButtonBrick brick = new ButtonBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
