package com.traffic.config.websocket.events;

import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import org.springframework.context.ApplicationEvent;

/**
 * 路段状态变化事件
 */
public class SegmentStateChangeEvent extends ApplicationEvent {
    private final int segmentId;
    private final SegmentState oldState;
    private final SegmentState newState;

    public SegmentStateChangeEvent(Object source, int segmentId, SegmentState oldState, SegmentState newState) {
        super(source);
        this.segmentId = segmentId;
        this.oldState = oldState;
        this.newState = newState;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public SegmentState getOldState() {
        return oldState;
    }

    public SegmentState getNewState() {
        return newState;
    }

    @Override
    public String toString() {
        return String.format("SegmentStateChangeEvent{segmentId=%d, oldState=%s, newState=%s}",
                segmentId, oldState, newState);
    }
}
