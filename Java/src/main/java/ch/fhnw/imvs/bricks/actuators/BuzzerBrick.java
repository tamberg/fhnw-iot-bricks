// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class BuzzerBrick extends Brick {
    private BuzzerBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile boolean currentEnabled = false;
    private volatile boolean targetEnabled = false;

    public boolean isEnabled() {
        return currentEnabled;
    }

    // TODO: rename to triggerAlert(int ms)?
    public void setEnabled(boolean enabled) {
        if (targetEnabled != enabled) {
            targetEnabled = enabled;
            super.sync();
        }
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentEnabled = Boolean.parseBoolean(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload;
        int mockBatt = (int) (Math.random() * 99 + 1);
        try {
            String payloadString = 
                (mock ? Integer.toString(mockBatt) + SEPARATOR : "") +
                Boolean.toString(targetEnabled);
            payload = payloadString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            payload = null;
        }
        return payload;
    }

    public static BuzzerBrick connect(Proxy proxy, String brickID) {
        BuzzerBrick brick = new BuzzerBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
