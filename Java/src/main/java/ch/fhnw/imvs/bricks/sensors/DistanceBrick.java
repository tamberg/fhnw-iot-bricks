// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class DistanceBrick extends Brick {
    private DistanceBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile int currentDist;

    public int getDistance() { // cm
        return currentDist;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryLevel(buf.getShort() / 100.0f);
        currentDist = buf.getInt();
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            ByteBuffer buf = ByteBuffer.allocate(6);
            buf.order(ByteOrder.BIG_ENDIAN); // network byte order
            float mockBatt = (float) (Math.random() * 3.7);
            int mockDist = (int) (Math.random() * 350);
            buf.putShort((short) (mockBatt * 100.0f));
            buf.putInt(mockDist);
            payload = buf.array();
        }
        return payload;
    }

    public static DistanceBrick connect(Proxy proxy, String brickID) {
        DistanceBrick brick = new DistanceBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
