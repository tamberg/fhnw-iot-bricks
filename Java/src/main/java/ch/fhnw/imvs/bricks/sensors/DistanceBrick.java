// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class DistanceBrick extends Brick {
    private DistanceBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile int currentDist;

    public int getDistance() {
        return currentDist;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentDist = Integer.parseInt(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            int mockBatt = (int) (Math.random() * 99 + 1);
            int mockDist = (int) (Math.random() * 255 + 1);
            try {
                String payloadString = 
                    Integer.toString(mockBatt) + SEPARATOR +
                    Integer.toString(mockDist);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    public static DistanceBrick connect(Proxy proxy, String brickID) {
        DistanceBrick brick = new DistanceBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
