package com.traffic.config.statemachine.states;

import com.traffic.config.statemachine.enums.ClearanceDecision;
import com.traffic.config.statemachine.enums.SegmentPhase;
import com.traffic.config.statemachine.items.ErrorCounters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SegmentState {
    private SegmentPhase currentPhase;
    private Set<String> vehicleIds;
    private int inCounter;
    private int outCounter;
    private LocalDateTime phaseStartTime;
    private ErrorCounters errorCounters;

    // 配置参数
    private final int minGreenTime = 30; // 最小绿灯时间（秒）
    private final int maxGreenTime = 120; // 最大绿灯时间（秒）
    private final int maxRedTime = 60; // 最大全红时间（秒）

    public SegmentState() {
        this.currentPhase = SegmentPhase.ALL_RED_CLEAR;
        this.vehicleIds = ConcurrentHashMap.newKeySet();
        this.inCounter = 0;
        this.outCounter = 0;
        this.phaseStartTime = LocalDateTime.now();
        this.errorCounters = new ErrorCounters();
    }

    // 检查是否可以从绿灯切换到全红
    public boolean canSwitchToRed(boolean hasOppositeRequest, boolean hasCurrentFlow) {
        Duration phaseDuration = Duration.between(phaseStartTime, LocalDateTime.now());
        long seconds = phaseDuration.getSeconds();

        // 必须达到最小绿灯时间
        if (seconds < minGreenTime) {
            return false;
        }

        // 必须有对向通行请求
        if (!hasOppositeRequest) {
            return false;
        }

        // 满足以下任一条件：无当前车流 或 达到最大绿灯时间
        return !hasCurrentFlow || seconds >= maxGreenTime;
    }

    // 判断路段是否已清空（状态3的核心逻辑）
    public ClearanceDecision checkClearance() {
        boolean idsEmpty = vehicleIds.isEmpty();
        boolean countersBalanced = (inCounter == outCounter);

        if (idsEmpty && countersBalanced) {
            return ClearanceDecision.CLEARED_SAFE;
        } else if (idsEmpty && !countersBalanced) {
            errorCounters.incrementCounterMismatch();
            return ClearanceDecision.CLEARED_WITH_WARNING;
        } else if (!idsEmpty && countersBalanced) {
            return ClearanceDecision.CONSERVATIVE_CLEAR;
        } else {
            return ClearanceDecision.NOT_CLEARED;
        }
    }

    // 检查是否超时
    public boolean isRedPhaseTimeout() {
        if (currentPhase != SegmentPhase.ALL_RED_CLEAR) {
            return false;
        }
        Duration phaseDuration = Duration.between(phaseStartTime, LocalDateTime.now());
        return phaseDuration.getSeconds() > maxRedTime;
    }

    // 状态转换
    public void transitionTo(SegmentPhase newPhase) {
        System.out.printf("段状态转换: %s -> %s%n", currentPhase, newPhase);
        this.currentPhase = newPhase;
        this.phaseStartTime = LocalDateTime.now();
    }

    // 车辆进入
    public void vehicleEnter(String vehicleId) {
        vehicleIds.add(vehicleId);
        inCounter++;
        System.out.printf("车辆 %s 进入，当前车辆数: %d%n", vehicleId, vehicleIds.size());
    }

    // 车辆离开
    public void vehicleExit(String vehicleId) {
        if (vehicleIds.remove(vehicleId)) {
            outCounter++;
            System.out.printf("车辆 %s 离开，当前车辆数: %d%n", vehicleId, vehicleIds.size());
        } else {
            // 记录ID逻辑错误
            errorCounters.incrementIdLogicErrors();
            System.out.printf("警告: 车辆 %s 离开时未在列表中找到%n", vehicleId);
        }
    }

    // Getters
    public SegmentPhase getCurrentPhase() { return currentPhase; }
    public Set<String> getVehicleIds() { return new HashSet<>(vehicleIds); }
    public int getInCounter() { return inCounter; }
    public int getOutCounter() { return outCounter; }
    public ErrorCounters getErrorCounters() { return errorCounters; }
    public boolean hasVehicles() { return !vehicleIds.isEmpty(); }
}
