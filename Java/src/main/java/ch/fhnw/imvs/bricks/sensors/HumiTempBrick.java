// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class HumiTempBrick extends Brick {
    protected HumiTempBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile double currentHumi;
    private volatile double currentTemp;

    public double getHumidity() { return currentHumi; }
    public double getTemperature() { return currentTemp; }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryLevel(buf.getShort() / 100.0f);
        currentHumi = buf.getShort() / 100.0;
        currentTemp = buf.getShort() / 100.0;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            ByteBuffer buf = ByteBuffer.allocate(6);
            buf.order(ByteOrder.BIG_ENDIAN); // network byte order
            float mockBatt = (float) (Math.random() * 3.7 + 1);
            float mockHumi = (float) (Math.random() * 99 + 1);
            float mockTemp = (float) (Math.random() * 50 + 1);
            buf.putShort((short) (mockBatt * 100.0f));
            buf.putShort((short) (mockHumi * 100.0f));
            buf.putShort((short) (mockTemp * 100.0f));
            payload = buf.array();
        }
        return payload;
    }

    public static HumiTempBrick connect(Proxy proxy, String brickID) {
        HumiTempBrick brick = new HumiTempBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
