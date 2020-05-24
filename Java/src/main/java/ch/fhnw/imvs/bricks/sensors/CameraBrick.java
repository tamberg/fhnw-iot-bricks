// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.awt.Image;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class CameraBrick extends Brick {
    private CameraBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile Image currentImage;

    public Image getImage() {
        return currentImage;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentImage = null; // TODO: Base64.decode(parts[1]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            int mockBatt = (int) (Math.random() * 99 + 1);
            Image mockImage = null; // TODO
            try {
                String payloadString = 
                    Integer.toString(mockBatt) + SEPARATOR +
                    ""; // TODO Base64.encode(mockImage);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    public static CameraBrick connect(Proxy proxy, String brickID) {
        CameraBrick brick = new CameraBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
