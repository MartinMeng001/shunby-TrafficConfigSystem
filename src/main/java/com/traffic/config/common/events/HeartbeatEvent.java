package com.traffic.config.common.events;

import com.traffic.config.cardetector.model.HeartbeatData;
import org.springframework.context.ApplicationEvent;

public class HeartbeatEvent extends ApplicationEvent {
    private final HeartbeatData heartbeatData;

    public HeartbeatEvent(Object source, HeartbeatData heartbeatData) {
        super(source);
        this.heartbeatData = heartbeatData;
    }

    public HeartbeatData getHeartbeatData() {
        return heartbeatData;
    }
}
