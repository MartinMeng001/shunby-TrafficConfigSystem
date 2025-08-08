package com.traffic.config.statemachinev3.events;

import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import org.springframework.context.ApplicationEvent;

public class SegmentMachineActionEvent extends ApplicationEvent {
    private final SegmentState currentState;
    private final SegmentEvent triggerEvent;
    private final SegmentVariables variables;

    public SegmentMachineActionEvent(Object source, SegmentState currentState, SegmentEvent triggerEvent, SegmentVariables variables) {
        super(source);
        this.currentState = currentState;
        this.triggerEvent = triggerEvent;
        this.variables = variables;
    }
    public SegmentState getCurrentState() { return currentState; }
    public SegmentEvent getTriggerEvent() { return triggerEvent; }
    public SegmentVariables getVariables() { return variables; }
}
