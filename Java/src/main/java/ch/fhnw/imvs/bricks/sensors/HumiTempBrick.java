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
        super.setBatteryLevel(buf.getShort());
        currentHumi = buf.getShort() / 100.0;
        currentTemp = buf.getShort() / 100.0;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            short mockBatt = (short) (Math.random() * 99 + 1);
            short mockHumi = (short) (100 * (Math.random() * 99 + 1));
            short mockTemp = (short) (100 * (Math.random() * 50 + 1)); 
            ByteBuffer buf = ByteBuffer.allocate(6);
            buf.order(ByteOrder.BIG_ENDIAN); // network byte order
            buf.putShort(mockBatt);
            buf.putShort(mockHumi);
            buf.putShort(mockTemp);
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
