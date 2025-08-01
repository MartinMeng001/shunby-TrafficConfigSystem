package com.traffic.config.statemachine.items;

import com.traffic.config.statemachine.SystemStateMachine;
import com.traffic.config.statemachine.enums.ClearanceDecision;
import com.traffic.config.statemachine.enums.SegmentPhase;
import com.traffic.config.statemachine.enums.SystemState;
import com.traffic.config.statemachine.states.SegmentState;

import java.util.HashMap;
import java.util.Map;

public class TrafficController {
    private SystemStateMachine systemStateMachine;
    private Map<String, SegmentState> segments;

    public TrafficController() {
        this.systemStateMachine = new SystemStateMachine();
        this.segments = new HashMap<>();

        // 初始化路段
        segments.put("segment1", new SegmentState());
        segments.put("segment2", new SegmentState());
    }

    // 主要的控制循环
    public void controlLoop() {
        SystemState currentSystemState = systemStateMachine.getCurrentState();

        switch (currentSystemState) {
            case ALL_RED_TRANSITION:
                handleAllRedTransition();
                break;
            case INDUCTIVE_MODE:
                handleInductiveMode();
                break;
            case DEGRADED_MODE:
                handleDegradedMode();
                break;
        }
    }

    private void handleAllRedTransition() {
        if (systemStateMachine.canExitTransition()) {
            // 根据之前的状态决定下一步
            systemStateMachine.transitionTo(SystemState.INDUCTIVE_MODE);
            System.out.println("全红过渡完成，进入感应模式");
        }
    }

    private void handleInductiveMode() {
        // 检查所有路段是否有故障条件
        for (Map.Entry<String, SegmentState> entry : segments.entrySet()) {
            String segmentId = entry.getKey();
            SegmentState segment = entry.getValue();

            // 检查错误计数器
            if (segment.getErrorCounters().shouldExitInductiveMode()) {
                systemStateMachine.triggerDegradation("路段 " + segmentId + " 错误次数超限");
                return;
            }

            // 检查超时
            if (segment.isRedPhaseTimeout()) {
                systemStateMachine.triggerDegradation("路段 " + segmentId + " 清空超时");
                return;
            }

            // 处理路段状态机
            handleSegmentStateMachine(segmentId, segment);
        }
    }

    private void handleSegmentStateMachine(String segmentId, SegmentState segment) {
        SegmentPhase currentPhase = segment.getCurrentPhase();

        switch (currentPhase) {
            case UPSTREAM_GREEN:
                // 模拟：检查是否需要切换方向
                boolean hasDownstreamRequest = Math.random() < 0.3; // 30%概率有下行请求
                boolean hasUpstreamFlow = segment.hasVehicles();

                if (segment.canSwitchToRed(hasDownstreamRequest, hasUpstreamFlow)) {
                    segment.transitionTo(SegmentPhase.ALL_RED_CLEAR);
                }
                break;

            case DOWNSTREAM_GREEN:
                // 类似逻辑，方向相反
                boolean hasUpstreamRequest = Math.random() < 0.3;
                boolean hasDownstreamFlow = segment.hasVehicles();

                if (segment.canSwitchToRed(hasUpstreamRequest, hasDownstreamFlow)) {
                    segment.transitionTo(SegmentPhase.ALL_RED_CLEAR);
                }
                break;

            case ALL_RED_CLEAR:
                ClearanceDecision decision = segment.checkClearance();
                handleClearanceDecision(segmentId, segment, decision);
                break;
        }
    }

    private void handleClearanceDecision(String segmentId, SegmentState segment, ClearanceDecision decision) {
        switch (decision) {
            case CLEARED_SAFE:
            case CLEARED_WITH_WARNING:
                // 切换到下一个绿灯状态（简化：随机选择方向）
                SegmentPhase nextPhase = Math.random() < 0.5 ?
                        SegmentPhase.UPSTREAM_GREEN : SegmentPhase.DOWNSTREAM_GREEN;
                segment.transitionTo(nextPhase);

                if (decision == ClearanceDecision.CLEARED_WITH_WARNING) {
                    System.out.printf("路段 %s 清空完成但有数据不一致警告%n", segmentId);
                }
                break;

            case CONSERVATIVE_CLEAR:
                System.out.printf("路段 %s 启动保守清空机制%n", segmentId);
                // 在实际实现中，这里会启动基于时间的强制清空
                break;

            case NOT_CLEARED:
                // 继续等待
                System.out.printf("路段 %s 等待清空%n", segmentId);
                break;
        }
    }

    private void handleDegradedMode() {
        // 简化：模拟条件恢复检查
        if (Math.random() < 0.1) { // 10%概率条件恢复
            System.out.println("检测到运行条件恢复");
            systemStateMachine.transitionTo(SystemState.ALL_RED_TRANSITION);
        }
    }

    // 模拟车辆事件
    public void simulateVehicleEvent(String segmentId, String vehicleId, boolean isEntering) {
        SegmentState segment = segments.get(segmentId);
        if (segment != null) {
            if (isEntering) {
                segment.vehicleEnter(vehicleId);
            } else {
                segment.vehicleExit(vehicleId);
            }
        }
    }

    public void printStatus() {
        System.out.println("\n=== 系统状态 ===");
        System.out.println("系统模式: " + systemStateMachine.getCurrentState());

        for (Map.Entry<String, SegmentState> entry : segments.entrySet()) {
            String segmentId = entry.getKey();
            SegmentState segment = entry.getValue();
            System.out.printf("%s: %s, 车辆数: %d, 计数器: IN=%d, OUT=%d%n",
                    segmentId, segment.getCurrentPhase(),
                    segment.getVehicleIds().size(),
                    segment.getInCounter(), segment.getOutCounter());
        }
        System.out.println();
    }
}
