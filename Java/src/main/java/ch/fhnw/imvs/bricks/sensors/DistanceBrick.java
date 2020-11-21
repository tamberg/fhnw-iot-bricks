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

    private volatile short currentDist;

    public int getDistance() { // int is more familiar
        return currentDist;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryLevel(buf.getShort());
        currentDist = buf.getShort();
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            short mockBatt = (short) (Math.random() * 99 + 1);
            short mockDist = (short) (Math.random() * 255 + 1);
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.order(ByteOrder.BIG_ENDIAN); // network byte order
            buf.putShort(mockBatt);
            buf.putShort(mockDist);
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
