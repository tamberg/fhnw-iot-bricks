// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mock;

import java.util.ArrayList;
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
        super.addBrick(brick);
    }

    @Override
    protected void syncBrick(Brick brick) { // TODO: who calls this?
        byte[] payload = super.getTargetPayload(brick, true); // mock
        super.setPendingPayload(brick, payload);
    }

    public static MockProxy fromConfig(String configHost) {
        return new MockProxy();
    }
}
