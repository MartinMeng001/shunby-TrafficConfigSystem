package com.traffic.config.statemachinev3.events;

import com.traffic.config.statemachinev3.enums.system.SystemStateV3;
import com.traffic.config.statemachinev3.enums.system.SystemEventV3;
import com.traffic.config.statemachinev3.variables.SystemVariables;

public class AllRedCtrlEvent extends StateMachineActionEvent {

    public AllRedCtrlEvent(Object source, SystemStateV3 currentState,
                           SystemEventV3 triggerEvent, SystemVariables variables) {
        super(source, currentState, triggerEvent, variables);
    }
}
