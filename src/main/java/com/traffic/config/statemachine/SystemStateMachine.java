package com.traffic.config.statemachine;

import com.traffic.config.statemachine.enums.SystemState;

import java.time.Duration;
import java.time.LocalDateTime;

public class SystemStateMachine {
    private SystemState currentState;
    private LocalDateTime stateStartTime;
    private final int transitionTime = 5; // 全红过渡时间（秒）

    public SystemStateMachine() {
        this.currentState = SystemState.ALL_RED_TRANSITION;
        this.stateStartTime = LocalDateTime.now();
        System.out.println("系统初始化，进入全红过渡状态");
    }

    // 检查是否可以从过渡状态切换
    public boolean canExitTransition() {
        Duration duration = Duration.between(stateStartTime, LocalDateTime.now());
        return duration.getSeconds() >= transitionTime;
    }

    // 状态转换
    public void transitionTo(SystemState newState) {
        System.out.printf("系统状态转换: %s -> %s%n", currentState, newState);
        this.currentState = newState;
        this.stateStartTime = LocalDateTime.now();
    }

    // 故障触发，强制进入降级模式
    public void triggerDegradation(String reason) {
        System.out.printf("触发降级: %s%n", reason);
        if (currentState == SystemState.INDUCTIVE_MODE) {
            transitionTo(SystemState.ALL_RED_TRANSITION);
        }
    }

    public SystemState getCurrentState() { return currentState; }
}
