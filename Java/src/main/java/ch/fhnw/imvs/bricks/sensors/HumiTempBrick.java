// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class HumiTempBrick extends Brick {
    private HumiTempBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";
    private volatile double currentHumi;
    private volatile double currentTemp;

    public double getHumidity() {
        return currentHumi;
    }

    public double getTemperature() {
        return currentTemp;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            currentHumi = Double.parseDouble(parts[1]);
            currentTemp = Double.parseDouble(parts[2]);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            int mockBatt = (int) (Math.random() * 99 + 1);
            double mockHumi = Math.random() * 99 + 1;
            double mockTemp = Math.random() * 50 + 1;
            try {
                String payloadString = 
                    Integer.toString(mockBatt) + SEPARATOR +
                    Double.toString(mockHumi) + SEPARATOR + 
                    Double.toString(mockTemp);
                payload = payloadString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    public static HumiTempBrick connect(Proxy proxy, String brickID) {
        HumiTempBrick brick = new HumiTempBrick(proxy, brickID);
        proxy.connectBrick(brick);
        return brick;
    }
}
