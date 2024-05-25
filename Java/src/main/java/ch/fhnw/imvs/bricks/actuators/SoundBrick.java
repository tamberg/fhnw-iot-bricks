// Copyright (c) 2024 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.actuators;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class SoundBrick extends Brick {
    private SoundBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

    private volatile int targetTrack;
    private volatile int targetVolume;

    public void play(int track, int volume) {
        if ((track < 0 || track > 255) ||
            (volume < 0 || volume > 10)) 
        {
            throw new IllegalArgumentException();
        }
        //if (targetTrack != track || 
        //    targetVolume != volume) 
        //{
        targetTrack = track;
        targetVolume = volume;
        super.sync();
        //}
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) {
        assert !mock; // actuator
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        buf.putShort((short) targetTrack);
        buf.putShort((short) targetVolume);
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) { assert false; } // actuator

    public static SoundBrick connect(Proxy proxy, String brickID) {
        SoundBrick brick = new SoundBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
