// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.sensors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

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
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            //ImageOutputStream imageStream = null;
            try {
                BufferedImage image = ImageIO.read(new File("test.jpg"));
                ImageIO.write(image, "jpg", byteArrayStream);
                //imageStream = ImageIO.createImageOutputStream(byteArrayStream);
                //ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next();
                //ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                //jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                //jpgWriteParam.setCompressionQuality(0.6f);
                //jpgWriter.setOutput(imageStream);
                //IIOImage iioImage = new IIOImage(currentImage, null, null);
                //jpgWriter.write(null, iioImage, jpgWriteParam);
                //jpgWriter.dispose();
                byte[] imageBytes = byteArrayStream.toByteArray();
                ByteBuffer buf = ByteBuffer.allocate(imageBytes.length);
                buf.put(imageBytes);
                payload = buf.array();
            } catch (IOException e) {
                // give up
            } finally {
                try {
                    //imageStream.close();
                    byteArrayStream.close();
                } catch (IOException e) {} // give up
            }
        }
        return payload;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {
        ByteArrayInputStream inputStream =
            new ByteArrayInputStream(payload);
        try {
            currentImage = ImageIO.read(inputStream);
        } catch (IOException e) {} // give up
    }

    public static CameraBrick connect(Proxy proxy, String brickID) {
        CameraBrick brick = new CameraBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
