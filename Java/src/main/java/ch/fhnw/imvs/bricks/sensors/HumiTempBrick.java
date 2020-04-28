// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Base64;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

// /* package */ final class ThingSpeakHumiTempBrick extends HumiTempBrick {}

/* package */ final class TtnBasicHumiTempBrick extends HumiTempBrick {
    /* package */ TtnBasicHumiTempBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private String getValueOf(String json, String name) {
        // TODO: error handling
        int p = json.indexOf(name);
        int q = json.indexOf('"', p + 1);
        q = json.indexOf('"', q + 1);
        int r = json.indexOf('"', q + 1);
        return json.substring(q + 1, r);
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        String mqttPayload = new String(payload, StandardCharsets.UTF_8);
        String ttnPayloadBase64 = getValueOf(mqttPayload, "payload_raw");
        byte[] ttnPayload = Base64.getDecoder().decode(ttnPayloadBase64);
        ByteBuffer buf = ByteBuffer.wrap(ttnPayload);
        super.setBatteryLevel(100); // TODO
        super.setHumidity(buf.getShort() / 100.0);
        super.setTemperature(buf.getShort() / 100.0);
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            // TODO: move to getMock...() in base class?
            short mockBatt = (short) (Math.random() * 99 + 1);
            short mockHumi = (short) (100 * (Math.random() * 99 + 1));
            short mockTemp = (short) (100 * (Math.random() * 50 + 1)); 
            ByteBuffer buf = ByteBuffer.allocate(4);
            // TODO: put mockBatt
            buf.putShort(mockHumi);
            buf.putShort(mockTemp);
            byte[] ttnPayload = buf.array();
            String ttnPayloadBase64 = Base64.getEncoder().encodeToString(ttnPayload);
            String mqttPayload = "{\"payload_raw\":\"" + ttnPayloadBase64 + "\"}";     
            try {
                payload = mqttPayload.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }
}

/* package */ final class Utf8HumiTempBrick extends HumiTempBrick {
    /* package */ Utf8HumiTempBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private final String SEPARATOR = ";";

    @Override
    protected void setCurrentPayload(byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            String[] parts = message.split(SEPARATOR);
            super.setBatteryLevel(Integer.parseInt(parts[0]));
            super.setHumidity(Double.parseDouble(parts[1]));
            super.setTemperature(Double.parseDouble(parts[2]));
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            // TODO: move to getMock...() in base class?
            int mockBatt = (short) (Math.random() * 99 + 1);
            double mockHumi = Math.random() * 99 + 1;
            double mockTemp = Math.random() * 50 + 1; 
            try {
                String utf8Payload = 
                     Integer.toString(mockBatt) + SEPARATOR +
                     Double.toString(mockHumi) + SEPARATOR + 
                     Double.toString(mockTemp);
                payload = utf8Payload.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }
}

// Disadvantage: this class has to be public, but it's also not final.

public abstract class HumiTempBrick extends Brick {
    protected HumiTempBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile double currentHumi;
    private volatile double currentTemp;

    protected void setHumidity(double humi) { currentHumi = humi; }
    protected void setTemperature(double temp) { currentTemp = temp; }

    public double getHumidity() { return currentHumi; }
    public double getTemperature() { return currentTemp; }

    public static HumiTempBrick connect(Proxy proxy, String brickID) {
        //HumiTempBrick brick = new TtnBasicHumiTempBrick(proxy, brickID); // TODO: dynamic
        HumiTempBrick brick = new Utf8HumiTempBrick(proxy, brickID); // TODO: dynamic
        brick.connect();
        return brick;
    }
}
