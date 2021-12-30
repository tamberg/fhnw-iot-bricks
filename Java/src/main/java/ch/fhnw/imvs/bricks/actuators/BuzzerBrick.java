// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class BuzzerBrick extends Brick {
    private BuzzerBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

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
    protected byte[] getTargetPayload(boolean mock) {
        ByteBuffer buf = ByteBuffer.allocate(mock ? 3 : 1);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        if (mock) {
            double mockBatt = Math.random() * 3.7;
            buf.putShort((short) (mockBatt * 100));
        }
        buf.put((byte) (targetEnabled ? 1 : 0));
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0f);
        currentEnabled = buf.get() != 0;
    }

    public static BuzzerBrick connect(Proxy proxy, String brickID) {
        BuzzerBrick brick = new BuzzerBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
