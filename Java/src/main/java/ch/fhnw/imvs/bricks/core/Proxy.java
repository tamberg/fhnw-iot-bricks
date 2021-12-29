// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.core;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

public abstract class Proxy {
    protected Proxy() { // called before subclass constructor
        bricks = new ArrayList<Brick>();
    }

    private final List<Brick> bricks;

    protected abstract void connectBrick(Brick brick); // called by Brick base
    protected abstract void syncBrick(Brick brick); // called by Brick base

    protected void addBrick(Brick brick) { // called by Proxies
        bricks.add(brick);
    }

    protected byte[] getTargetPayload(Brick brick, boolean mock) { // called by Proxies
    	return brick.getTargetPayload(mock); // package level access
    }

    protected void setPendingPayload(Brick brick, byte[] payload) { // called by Proxies
    	brick.setPendingPayload(payload); // package level access
    }

    // TODO: waitForAnyUpdate vs. waitForAllUpdates?
    public void waitForUpdate() { // called by client code
        boolean updated = false;
        while (!updated) {
            for (Brick brick : bricks) {
                updated = brick.tryUpdate() || updated; // sequence matters
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100); // ms
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }
}
