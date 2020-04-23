// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.core;

public abstract class Proxy {
    protected abstract void syncBrick(Brick brick); // called by Brick base
    public abstract void connectBrick(Brick brick); // called by Brick factories
    public abstract void waitForUpdate(); // called by client code

    protected byte[] getTargetPayload(Brick brick, boolean mock) { // called by Proxies
    	return brick.getTargetPayload(mock); // package level access
    }

    protected void setPendingPayload(Brick brick, byte[] payload) { // called by Proxies
    	brick.setPendingPayload(payload);  // package level access
    }

    protected boolean tryUpdate(Brick brick) { // called by Proxies
    	return brick.tryUpdate();  // package level access
    }
}
