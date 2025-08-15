package com.traffic.config.statemachinev3.events;

import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import org.springframework.context.ApplicationEvent;

public class CustomControlEvent extends ApplicationEvent {
    private final SegmentState upSegmentState;
    private final SegmentState downSegmentState;
    private final String sigid;
    public CustomControlEvent(Object source, SegmentState upSegmentState, SegmentState downSegmentState, String sigid) {
        super(source);
        this.upSegmentState = upSegmentState;
        this.downSegmentState = downSegmentState;
        this.sigid = sigid;
    }
    public SegmentState getUpSegmentState() {return upSegmentState;}
    public SegmentState getDownSegmentState() {return downSegmentState;}
    public String getSigid() {return sigid;}
}
