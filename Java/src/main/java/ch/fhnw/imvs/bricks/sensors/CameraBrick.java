// Copyright (c) 2023 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import ch.fhnw.imvs.bricks.core.Proxy;
import ch.fhnw.imvs.bricks.core.Brick;

public final class CameraBrick extends Brick {
    private CameraBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private BufferedImage currentImage = null;

    public Image getImage() {
        return currentImage;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        byte[] payload = null;
        if (mock) {
            try {
                Path filePath = Paths.get("test.jpg");
                byte[] imageBytes = Files.readAllBytes(filePath);
                ByteBuffer buf = ByteBuffer.allocate(imageBytes.length + 6);
                buf.order(ByteOrder.BIG_ENDIAN); // network byte order
                double mockBatt = Math.random() * 3.7;
                buf.putShort((short) (mockBatt * 100)); // 2 bytes
                buf.putInt(imageBytes.length); // 4 bytes
                buf.put(imageBytes);
                payload = buf.array();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return payload;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteBuffer buf = ByteBuffer.wrap(payload);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        super.setBatteryVoltage(buf.getShort() / 100.0f); // 2 bytes
        int imageLength = buf.getInt(); // 4 bytes
        byte[] imageBytes = new byte[imageLength];
        buf.get(imageBytes); // imageBytes.length bytes
        ByteArrayInputStream inputStream =
            new ByteArrayInputStream(imageBytes);
        try {
            currentImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CameraBrick connect(Proxy proxy, String brickID) {
        CameraBrick brick = new CameraBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
