// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.http;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class HttpProxy extends Proxy {
    private HttpProxy() {
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
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public static HttpProxy fromConfig(String configHost) {
        return new HttpProxy();
    }
}
