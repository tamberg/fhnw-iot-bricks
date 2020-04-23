// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mock;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class MockProxy extends Proxy {
    private MockProxy() {
        bricks = new ArrayList<Brick>();
    }

    private final List<Brick> bricks;

    @Override
    public void connectBrick(Brick brick) {
        bricks.add(brick);
    }

    @Override
    protected void syncBrick(Brick brick) {}

    @Override
    public void waitForUpdate() {
        for (Brick brick : bricks) {
            byte[] payload = super.getTargetPayload(brick, true); // mock
            if (payload != null) {
                super.setPendingPayload(brick, payload);
                super.tryUpdate(brick); // ignore result
            }
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1000); // ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static MockProxy fromConfig(String configHost) {
        return new MockProxy();
    }
}
