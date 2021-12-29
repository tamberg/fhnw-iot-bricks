// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mock;

import java.lang.Runnable;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class MockProxy extends Proxy implements Runnable {
    private MockProxy() {
        bricks = new ArrayList<Brick>();
    }

    private final List<Brick> bricks;

    @Override
    public void connectBrick(Brick brick) {
        bricks.add(brick);
        super.addBrick(brick);
    }

    @Override
    protected void syncBrick(Brick brick) {
        byte[] payload = super.getTargetPayload(brick, true); // mock
        super.setPendingPayload(brick, payload);
    }

    public void run() { // Runnable interface
        while (true) {
            for (Brick brick : bricks) {
                syncBrick(brick);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1000); // ms
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }

    public static MockProxy fromConfig(String configHost) {
        MockProxy proxy = new MockProxy();
        Thread thread = new Thread(proxy);
        thread.start();
        return proxy;
    }
}
