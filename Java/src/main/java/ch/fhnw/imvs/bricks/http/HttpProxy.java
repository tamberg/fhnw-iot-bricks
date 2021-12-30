// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.http;

import ch.fhnw.imvs.bricks.core.Brick;
import ch.fhnw.imvs.bricks.core.Proxy;

public final class HttpProxy extends Proxy {
    private HttpProxy() {}

    @Override
    public void connectBrick(Brick brick) {
        super.addBrick(brick);
    }

    @Override
    protected void syncBrick(Brick brick) {}

    public static HttpProxy fromConfig(String configHost) {
        return new HttpProxy();
    }
}
