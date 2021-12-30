// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class ButtonBrick extends Brick {
    private ButtonBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile boolean currentPressed;

    public boolean isPressed() {
        return currentPressed;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryLevel(buf.getShort() / 100.0f);
        currentPressed = buf.getShort() != 0;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            ByteBuffer buf = ByteBuffer.allocate(3);
            buf.order(ByteOrder.BIG_ENDIAN); // network byte order
            float mockBatt = (float) (Math.random() * 3.7 + 1);
            boolean mockPressed = Math.random() < 0.5;
            buf.putShort((short) (mockBatt * 100.0f));
            buf.put((byte) (mockPressed ? 1 : 0));
            payload = buf.array();
        }
        return payload;
    }

    public static ButtonBrick connect(Proxy proxy, String brickID) {
        ButtonBrick brick = new ButtonBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
