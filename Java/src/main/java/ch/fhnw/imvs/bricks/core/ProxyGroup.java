// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.core; // must be same as Proxy

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

public final class ProxyGroup {
    public ProxyGroup() {
        proxies = new ArrayList<Proxy>();
    }

    private final List<Proxy> proxies;

    public void addProxy(Proxy proxy) { // called by client code
        proxies.add(proxy);
    }

    public void waitForUpdate() { // called by client code
        boolean updated = false;
        while (!updated) {
            for (Proxy proxy : proxies) {
                updated = proxy.tryUpdate() || updated; // sequence matters
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100); // ms
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }
}
