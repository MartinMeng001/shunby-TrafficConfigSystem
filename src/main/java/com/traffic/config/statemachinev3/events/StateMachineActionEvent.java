package com.traffic.config.statemachinev3.events;

import org.springframework.context.ApplicationEvent;
import com.traffic.config.statemachinev3.enums.system.SystemStateV3;
import com.traffic.config.statemachinev3.enums.system.SystemEventV3;
import com.traffic.config.statemachinev3.variables.SystemVariables;

/**
 * 状态机动作事件基类
 */
public abstract class StateMachineActionEvent extends ApplicationEvent {

    private final SystemStateV3 currentState;
    private final SystemEventV3 triggerEvent;
    private final SystemVariables variables;

    public StateMachineActionEvent(Object source, SystemStateV3 currentState,
                                   SystemEventV3 triggerEvent, SystemVariables variables) {
        super(source);
        this.currentState = currentState;
        this.triggerEvent = triggerEvent;
        this.variables = variables;
    }

    // Getters
    public SystemStateV3 getCurrentState() { return currentState; }
    public SystemEventV3 getTriggerEvent() { return triggerEvent; }
    public SystemVariables getVariables() { return variables; }
}
