// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

/* package */ abstract class MqttConfig {
    protected MqttConfig() {}

    public abstract String getHost();
    public abstract String getUsername();
    public abstract String getPassword();
    public abstract String getSubscribeTopic(String brickID);
    public abstract String getPublishTopic(String brickID);
}
