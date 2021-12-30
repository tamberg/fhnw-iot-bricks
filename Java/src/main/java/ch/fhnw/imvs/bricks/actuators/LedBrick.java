// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class LedBrick extends Brick {
    private LedBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile Color currentColor = Color.BLACK;
    private volatile Color targetColor = Color.BLACK;

    public Color getColor() {
        return currentColor;
    }

    public void setColor(Color color) {
        if (!targetColor.equals(color)) {
            targetColor = color;
            super.sync();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        ByteBuffer buf = ByteBuffer.allocate(mock ? 5 : 3);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        if (mock) {
            double mockBatt = Math.random() * 3.7;
            buf.putShort((short) (mockBatt * 100));
        }
        int r = targetColor.getRed();
        int g = targetColor.getGreen();
        int b = targetColor.getBlue();
        buf.put((byte) r);
        buf.put((byte) g);
        buf.put((byte) b);
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0);
        // https://stackoverflow.com/questions/4266756/can-we-make-unsigned-byte-in-java
        int r = buf.get() & (0xff); // or Byte.toUnsignedInt(buf.get()); // Java 8+
        int g = buf.get() & (0xff);
        int b = buf.get() & (0xff);
        currentColor = new Color(r, g, b);
    }

    public static LedBrick connect(Proxy proxy, String brickID) {
        LedBrick brick = new LedBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
