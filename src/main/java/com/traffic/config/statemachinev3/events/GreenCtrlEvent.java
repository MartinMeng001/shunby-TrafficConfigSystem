package com.traffic.config.statemachinev3.events;

import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.variables.SegmentVariables;

public class GreenCtrlEvent extends SegmentMachineActionEvent {
    public GreenCtrlEvent(Object source, SegmentState currentState,
                          SegmentEvent triggerEvent, SegmentVariables variables) {
        super(source, currentState, triggerEvent, variables);
    }
}
