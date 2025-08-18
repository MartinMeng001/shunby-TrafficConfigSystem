package com.traffic.config.statemachinev3.core;

import com.traffic.config.entity.Segment;
import com.traffic.config.service.ConfigService;
import com.traffic.config.service.event.EventBusService;
import com.traffic.config.signalplatform.platformbase.CrossInfoManager;
import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.enums.system.SystemStateV3;
import com.traffic.config.statemachinev3.enums.system.SystemEventV3;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.events.CustomControlEvent;
import com.traffic.config.statemachinev3.variables.SystemVariables;
import com.traffic.config.statemachinev3.constants.SystemConstants;
import com.traffic.config.statemachinev3.guards.SystemGuards;
import com.traffic.config.statemachinev3.actions.SystemActions;
import com.traffic.config.statemachinev3.variables.objects.CrossMettingZoneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 顶层系统状态机核心引擎
 *
 * 实现顶层系统的完整状态机逻辑：
 * M_system = (Q_sys, Σ_sys, V_sys, δ_sys, G_sys, A_sys, q₀_sys, F_sys, C_sys, P_sys)
 *
 * 主要职责：
 * - 管理系统级状态转换（SYSTEM_INIT → ALL_RED_TRANSITION → INDUCTIVE_MODE 等）
 * - 协调多个路段状态机
 * - 集成路段清空决策
 * - 系统健康度管理
 * - 故障检测和恢复
 *
 * @author System
 * @version 3.0.0
 */
@Component
public class TopLevelStateMachine {

    //private static final Logger logger = Logger.getLogger(TopLevelStateMachine.class.getName());
    private static final Logger logger = LoggerFactory.getLogger(TopLevelStateMachine.class);

    // ==================== 核心组件 ====================

    /**
     * 当前系统状态 (q ∈ Q_sys)
     */
    private volatile SystemStateV3 currentState;

    /**
     * 系统变量集合 (V_sys)
     */
    private final SystemVariables variables;

    /**
     * 路段状态机集合
     */
    private final List<SegmentStateMachine> segmentStateMachines;

    /**
     * 事件队列
     */
    private final BlockingQueue<PrioritizedSystemEvent> eventQueue;

    /**
     * 系统运行状态
     */
    private volatile boolean isRunning;

    /**
     * 状态转换历史
     */
    private final List<SystemStateTransitionRecord> transitionHistory;

    @Autowired
    private CrossInfoManager crossInfoManager;

    @Autowired
    private ConfigService configService;

    // ==================== 构造函数和初始化 ====================

    /**
     * 构造函数
     */
    public TopLevelStateMachine() {
        this.variables = new SystemVariables();
        this.segmentStateMachines = new CopyOnWriteArrayList<>();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.transitionHistory = Collections.synchronizedList(new ArrayList<>());
        this.currentState = SystemStateV3.SYSTEM_INIT;
        this.isRunning = false;

        // 初始化路段状态机
        initializeSegmentStateMachines();

        logger.info("顶层系统状态机初始化完成 - 初始状态: {}, 路段数量: {}",
                currentState.getChineseName(), segmentStateMachines.size());
    }

    /**
     * 初始化路段状态机
     */
    private void initializeSegmentStateMachines() {
        for (int i = 1; i <= SystemConstants.TOTAL_SEGMENT_COUNT; i++) {
            SegmentStateMachine segment = new SegmentStateMachine(i);
            //logger.debug("初始化路段状态机 - 路段ID: {}", i);
            segmentStateMachines.add(segment);
        }
    }

    // ==================== 系统启动和停止 ====================

    /**
     * 启动系统状态机
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;

            // 启动所有路段状态机
//            for (SegmentStateMachine segment : segmentStateMachines) {
//                segment.start();
//            }

            // 发送系统启动事件
            postEvent(SystemEventV3.TIMER_TICK, null);

            logger.info("顶层系统状态机启动完成");
        }
    }

    /**
     * 停止系统状态机
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;

            // 停止所有路段状态机
            for (SegmentStateMachine segment : segmentStateMachines) {
                segment.stop();
            }

            eventQueue.clear();
            logger.info("顶层系统状态机停止");
        }
    }

    // ==================== 主事件处理循环 ====================

    /**
     * 处理定时器滴答事件 - 系统的心跳
     */
    public boolean processTimerTick() {
        if (!isRunning) {
            return false;
        }

        try {
            // 1. 处理路段状态机
            processSegmentStateMachines();

            // 2. 更新路段清空状态
            updateSegmentClearanceStates();

            // 3. 发送定时器事件到系统状态机
            postEvent(SystemEventV3.TIMER_TICK, null);

            // 4. 处理系统事件
            processSystemEvents();

            // 5. 检查并触发自动事件
            checkAndTriggerAutoEvents();

            // 6. 更新系统健康度 - 这里通过系统事件处理，不需要每次都执行
//            updateSystemHealth();
            // 7. 更新路口信号机状态 - 在路段启动后生效
            processSegmentStateCtrlInfo();
            return true;
        } catch (Exception e) {
            logger.warn("系统状态机处理定时器事件异常: {}", e.getMessage());
            variables.incrementConsecutiveFaults();
            return false;
        }
    }

    /**
     * 处理所有路段状态机
     */
    private void processSegmentStateMachines() {
        for (SegmentStateMachine segment : segmentStateMachines) {
            try {
                segment.processTimerTick();
            } catch (Exception e) {
                logger.warn("处理路段 {} 状态机异常: {}", segment.getSegmentId(), e.getMessage());
                // 标记路段故障
                markSegmentFaulty(segment.getSegmentId(), e.getMessage());
            }
        }
    }

    /**
     * 处理所有信号机状态
     */
    private void processSegmentStateCtrlInfo() {
        SegmentStateMachine lastSegment = null;
        if (!EventBusService.isReady()) return;
        for (SegmentStateMachine segment : segmentStateMachines) {
            try {
                if(!segment.isRunning()) continue;
                if(lastSegment == null) {
                    EventBusService.publishStatic(new CustomControlEvent("CustomControlEvent", segment.getCurrentState(), null,
                            getSigidBySegmentId(segment.getSegmentId(), true)));

//                    else if(segment.getCurrentState().isUpstreamState()) {
//                        EventBusService.publishStatic(new CustomControlEvent("CustomControlEvent", segment.getCurrentState(), null,
//                                getSigidBySegmentId(segment.getSegmentId(), true)));
//                    }
                }else{
                    EventBusService.publishStatic(new CustomControlEvent("CustomControlEvent", lastSegment.getCurrentState(), segment.getCurrentState(),
                            getSigidBySegmentId(segment.getSegmentId(), true)));
                }
                lastSegment = segment;
            } catch (Exception e) {
                logger.warn("处理路段 {} 状态机异常: {}", segment.getSegmentId(), e.getMessage());

            }
        }
        if(lastSegment != null) {
            if(lastSegment.getSegmentId()!=4) return;
            EventBusService.publishStatic(new CustomControlEvent("CustomControlEvent", null, lastSegment.getCurrentState(),
                    getSigidBySegmentId(lastSegment.getSegmentId(), false)));
//            if(lastSegment.getCurrentState().isDownstreamState()){
//                EventBusService.publishStatic(new CustomControlEvent("CustomControlEvent", null, lastSegment.getCurrentState(),
//                        getSigidBySegmentId(lastSegment.getSegmentId(), false)));
//            }else if(lastSegment.getCurrentState().isUpstreamState()) {
//                EventBusService.publishStatic(new CustomControlEvent("CustomControlEvent", lastSegment.getCurrentState(),null,
//                        getSigidBySegmentId(lastSegment.getSegmentId(), false)));
//            }
        }
    }

    private String getSigidBySegmentId(int segmentId, boolean isUpstream) {
        Optional<Segment> segment = configService.getSegmentBySegmentId(segmentId);
        if(segment.isEmpty()) return "";
        Segment seg = segment.get();
        if(isUpstream)return seg.getUpsigid();
        return seg.getDownsigid();
    }

    /**
     * 处理系统级事件
     */
    private void processSystemEvents() {
        while (!eventQueue.isEmpty() && isRunning) {
            try {
                PrioritizedSystemEvent prioritizedEvent = eventQueue.poll();
                if (prioritizedEvent != null) {
                    processSystemEvent(prioritizedEvent.event, prioritizedEvent.eventData);
                }
            } catch (Exception e) {
                logger.warn("处理系统事件异常: {}", e.getMessage());
                variables.incrementConsecutiveFaults();
            }
        }
    }

    /**
     * 处理单个系统事件
     * 实现系统级状态转换函数 δ_sys: Q_sys × Σ_sys × V_sys* → Q_sys
     */
    private boolean processSystemEvent(SystemEventV3 event, Map<String, Object> eventData) {
        SystemStateV3 oldState = currentState;
        LocalDateTime eventTime = LocalDateTime.now();

        try {
            // 1. 确定目标状态
            SystemStateV3 targetState = determineSystemTargetState(event);
            if (targetState == null) {
                return true; // 无状态变化, 有些无状态变化，也需要执行一些动作，这里只处理不需要任何动作的情况
            }

            // 2. 检查守护条件 G_sys(q, σ, v)
            if (!checkSystemGuardCondition(event, targetState)) {
                logger.debug("系统状态转换被守护条件阻止: {} -> {} (事件: {})",
                        currentState.getChineseName(), targetState.getChineseName(), event.getChineseName());
                return false;
            }

            // 3. 执行动作函数 A_sys(q, σ, v)
            executeSystemAction(event, targetState, eventData);

            // 4. 状态转换
            if (targetState != currentState) {
                transitionToSystemState(targetState, event);
                // 5. 记录状态转换
                recordSystemStateTransition(oldState, currentState, event, eventTime);
            }

            return true;

        } catch (Exception e) {
            logger.warn("处理系统事件 {} 时发生异常: {}", event.getChineseName(), e.getMessage());
            variables.incrementConsecutiveFaults();
            return false;
        }
    }

    // ==================== 系统状态转换逻辑 ====================

    /**
     * 确定系统目标状态
     */
    private SystemStateV3 determineSystemTargetState(SystemEventV3 event) {
        switch (event) {
            case TIMER_TICK -> {
                return determineTimerTickSystemTargetState();
            }
            case SYSTEM_INIT_COMPLETE -> {
                if (currentState == SystemStateV3.SYSTEM_INIT) {
                    return SystemStateV3.ALL_RED_TRANSITION;
                }
            }
            case TRANSITION_COMPLETE, RECOVERY_VERIFIED -> {
                if (currentState == SystemStateV3.ALL_RED_TRANSITION) {
                    return SystemStateV3.INDUCTIVE_MODE;
                }
            }
            case TRANSITION_TIMEOUT -> {
                if (currentState == SystemStateV3.ALL_RED_TRANSITION) {
                    return SystemStateV3.DEGRADED_MODE;
                }
            }
            case SEGMENT_FAULT_DETECTED, SYSTEM_FAULT_DETECTED -> {
                if (currentState == SystemStateV3.INDUCTIVE_MODE) {
                    return SystemStateV3.ALL_RED_TRANSITION;
                }
            }
            case CRITICAL_FAULT -> {
                return SystemStateV3.EMERGENCY_MODE;
            }
            case CONDITIONS_RESTORED -> {
                if (currentState == SystemStateV3.DEGRADED_MODE) {
                    return SystemStateV3.ALL_RED_TRANSITION;
                }
            }
            case MAINTENANCE_REQUEST -> {
                if (currentState == SystemStateV3.INDUCTIVE_MODE || currentState == SystemStateV3.DEGRADED_MODE) {
                    return SystemStateV3.MAINTENANCE_MODE;
                }
            }
            case MAINTENANCE_COMPLETE -> {
                if (currentState == SystemStateV3.MAINTENANCE_MODE) {
                    return SystemStateV3.SYSTEM_INIT;
                }
            }
            case SYSTEM_RESET -> {
                return SystemStateV3.SYSTEM_INIT;
            }
            case HEALTH_SCORE_UPDATE -> {
                return currentState;
            }
            default -> {// SEGMENT_CLEARANCE_UPDATE, ALL_SEGMENTS_CLEARED, CLEARANCE_TIMEOUT, CONSERVATIVE_CLEAR_TRIGGERED

            }
        }
        return null;
    }

    /**
     * 基于定时器事件确定系统目标状态
     */
    private SystemStateV3 determineTimerTickSystemTargetState() {
        switch (currentState) {
            case SYSTEM_INIT -> {
                // 检查初始化是否完成
                if(!variables.isCommunicationNormal()){
                    variables.setCommunicationNormal(performSystemSelfCheck());
                }
                if(!variables.isConfigurationLoaded()) {
                    if(verifyConfigurationLoaded()) {
                        variables.setCommunicationStatus(SystemVariables.CommunicationStatus.NORMAL);
                    }
                }
                if (variables.isSystemInitTimeout()) {
                    return SystemStateV3.ALL_RED_TRANSITION;
                }
            }
            case ALL_RED_TRANSITION -> {
                // 检查过渡是否完成或超时
                if (variables.isTransitionComplete()) {
                    variables.setSegmentsAllReady(true);
                    return SystemStateV3.INDUCTIVE_MODE;
                } else if (variables.isTransitionTimeout()) {
                    return SystemStateV3.DEGRADED_MODE;
                }
            }
            case INDUCTIVE_MODE -> {
                // 检查是否需要降级或进入紧急模式
                if (shouldEnterEmergencyMode()) {
                    return SystemStateV3.EMERGENCY_MODE;
                } else if (shouldEnterDegradedMode()) {
                    return SystemStateV3.ALL_RED_TRANSITION;
                }
            }
            case DEGRADED_MODE -> {
                // 检查是否可以恢复或需要紧急模式
                if (shouldEnterEmergencyMode()) {
                    return SystemStateV3.EMERGENCY_MODE;
                } else if (shouldRecoverFromDegradedMode()) {
                    return SystemStateV3.ALL_RED_TRANSITION;
                }
            }
        }
        return null; // 保持当前状态
    }

    /**
     * 检查系统守护条件
     */
    private boolean checkSystemGuardCondition(SystemEventV3 event, SystemStateV3 targetState) {
        switch (event) {
            case TIMER_TICK -> {
                if (targetState == SystemStateV3.ALL_RED_TRANSITION) {
                    return SystemGuards.checkInitializationComplete(currentState, event, variables) ||
                            SystemGuards.checkSegmentFaultDetected(currentState, event, variables) ||
                            SystemGuards.checkConditionsRestored(currentState, event, variables);
                } else if (targetState == SystemStateV3.INDUCTIVE_MODE) {
                    return SystemGuards.checkTransitionComplete(currentState, event, variables) ||
                            SystemGuards.checkRecoveryVerified(currentState, event, variables);
                } else if (targetState == SystemStateV3.DEGRADED_MODE) {
                    return SystemGuards.checkTransitionTimeout(currentState, event, variables);
                } else if (targetState == SystemStateV3.EMERGENCY_MODE) {
                    return SystemGuards.checkCriticalFaultDetected(currentState, event, variables);
                }
            }
            case SYSTEM_INIT_COMPLETE -> {
                return SystemGuards.checkInitializationComplete(currentState, event, variables);
            }
            case TRANSITION_COMPLETE -> {
                return SystemGuards.checkTransitionComplete(currentState, event, variables);
            }
            case TRANSITION_TIMEOUT -> {
                return SystemGuards.checkTransitionTimeout(currentState, event, variables);
            }
            case SEGMENT_FAULT_DETECTED -> {
                return SystemGuards.checkSegmentFaultDetected(currentState, event, variables);
            }
            case SYSTEM_FAULT_DETECTED -> {
                return SystemGuards.checkSystemFaultDetected(currentState, event, variables);
            }
            case CRITICAL_FAULT -> {
                return SystemGuards.checkCriticalFaultDetected(currentState, event, variables);
            }
            case CONDITIONS_RESTORED -> {
                return SystemGuards.checkConditionsRestored(currentState, event, variables);
            }
            case RECOVERY_VERIFIED -> {
                return SystemGuards.checkRecoveryVerified(currentState, event, variables);
            }
            case MAINTENANCE_REQUEST -> {
                return SystemGuards.checkMaintenanceRequestAllowed(currentState, event, variables);
            }
            case MAINTENANCE_COMPLETE -> {
                return SystemGuards.checkMaintenanceComplete(currentState, event, variables);
            }
        }
        return true; // 默认允许
    }

    /**
     * 执行系统动作函数
     */
    private void executeSystemAction(SystemEventV3 event, SystemStateV3 targetState, Map<String, Object> eventData) {
        switch (event) {
            case TIMER_TICK -> {
                // 定时器事件的动作在状态进入时执行
            }
            case SYSTEM_RESET -> {
                SystemActions.executeEnterSystemInit(currentState, event, variables);
            }
            case HEALTH_SCORE_UPDATE -> {
                updateSystemHealth();
            }
            default -> {
                logger.debug("事件 {} 无需特殊动作处理", event.getChineseName());
            }
        }

        // 如果有状态转换，执行状态进入动作
        if (targetState != currentState) {
            executeSystemStateEntryAction(targetState, event);
        }
    }

    /**
     * 执行系统状态进入动作
     */
    private void executeSystemStateEntryAction(SystemStateV3 newState, SystemEventV3 triggerEvent) {
        switch (newState) {
            case SYSTEM_INIT ->
                    SystemActions.executeEnterSystemInit(currentState, triggerEvent, variables);
            case ALL_RED_TRANSITION ->
                    SystemActions.executeEnterTransition(currentState, triggerEvent, variables);
            case INDUCTIVE_MODE ->
                    SystemActions.executeEnterInductiveMode(currentState, triggerEvent, variables);
            case DEGRADED_MODE ->
                    SystemActions.executeEnterDegradedMode(currentState, triggerEvent, variables);
            case MAINTENANCE_MODE ->
                    SystemActions.executeEnterMaintenanceMode(currentState, triggerEvent, variables);
            case EMERGENCY_MODE ->
                    SystemActions.executeEnterEmergencyMode(currentState, triggerEvent, variables);
        }
    }

    /**
     * 状态转换
     */
    private void transitionToSystemState(SystemStateV3 newState, SystemEventV3 triggerEvent) {
        SystemStateV3 oldState = currentState;
        currentState = newState;

        logger.info("系统状态转换: {} -> {} (触发事件: {})",
                oldState.getChineseName(), newState.getChineseName(), triggerEvent.getChineseName());

        // 通知路段状态机系统状态变化
        notifySegmentsOfSystemStateChange(newState);
    }

    // ==================== 路段清空状态管理 ====================

    /**
     * 更新路段清空状态
     */
    private void updateSegmentClearanceStates() {
        for (SegmentStateMachine segment : segmentStateMachines) {
            try {
                ClearanceDecision decision = segment.getCurrentClearanceDecision();
                updateSegmentClearanceState(segment.getSegmentId(), decision);
            } catch (Exception e) {
                logger.warn("更新路段 {} 清空状态异常: {}", segment.getSegmentId(), e.getMessage());
                markSegmentFaulty(segment.getSegmentId(), "清空状态更新异常");
            }
        }
    }

    /**
     * 更新单个路段的清空状态
     */
    public void updateSegmentClearanceState(int segmentId, ClearanceDecision decision) {
        // 这里简化实现，实际应该传递更详细的清空决策信息
        variables.updateSegmentClearanceState(segmentId, decision, decision, decision);
    }

    /**
     * 检查所有路段是否已清空
     */
    private boolean checkAllSegmentsCleared() {
        return variables.isSegmentsAllReady();
    }

    // ==================== 故障检测和处理 ====================

    /**
     * 标记路段故障
     */
    private void markSegmentFaulty(int segmentId, String reason) {
        logger.warn("路段 {} 被标记为故障: {}", segmentId, reason);

        // 更新系统变量中的故障信息
        variables.recordError(
                "SEGMENT_FAULT",
                "路段 " + segmentId + " 故障: " + reason,
                "SEGMENT_" + segmentId);

        // 降低系统健康度
        variables.updateHealthScore(-10);

        // 触发故障事件
        postEvent(SystemEventV3.SEGMENT_FAULT_DETECTED,
                Map.of("segmentId", segmentId, "reason", reason));
    }

    /**
     * 检查是否应该进入紧急模式
     */
    private boolean shouldEnterEmergencyMode() {
        return variables.getSystemHealthScore() < SystemConstants.CRITICAL_HEALTH_THRESHOLD ||
                variables.getConsecutiveFaults() >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT ||
                variables.getCommunicationStatus() == SystemVariables.CommunicationStatus.FAILED;
    }

    /**
     * 检查是否应该进入降级模式
     */
    private boolean shouldEnterDegradedMode() {
        return variables.getSystemHealthScore() < SystemConstants.NORMAL_HEALTH_THRESHOLD ||
                variables.getConsecutiveFaults() >= 2 ||
                getFaultySegmentCount() > SystemConstants.TOTAL_SEGMENT_COUNT / 2;
    }

    /**
     * 检查是否应该从降级模式恢复
     */
    private boolean shouldRecoverFromDegradedMode() {
        return variables.getSystemHealthScore() >= SystemConstants.RECOVERY_HEALTH_THRESHOLD &&
                variables.getConsecutiveFaults() == 0 &&
                getFaultySegmentCount() == 0 &&
                variables.getStableOperationTime() >= SystemConstants.STABLE_OPERATION_TIME;
    }

    /**
     * 获取故障路段数量
     */
    private int getFaultySegmentCount() {
        return (int) segmentStateMachines.stream()
                .mapToLong(segment -> segment.isFaultDetected() ? 1 : 0)
                .sum();
    }

    // ==================== 自动事件触发 ====================

    /**
     * 检查并触发自动事件
     */
    private void checkAndTriggerAutoEvents() {
        // 检查初始化完成
        if (currentState == SystemStateV3.SYSTEM_INIT && shouldTriggerInitComplete()) {
            postEvent(SystemEventV3.SYSTEM_INIT_COMPLETE, null);
        }

        // 检查过渡完成
        if (currentState == SystemStateV3.ALL_RED_TRANSITION && shouldTriggerTransitionComplete()) {
            postEvent(SystemEventV3.TRANSITION_COMPLETE, null);
        }

        // 检查过渡超时
        if (currentState == SystemStateV3.ALL_RED_TRANSITION && shouldTriggerTransitionTimeout()) {
            postEvent(SystemEventV3.TRANSITION_TIMEOUT, null);
        }

        // 检查系统故障
        if (shouldTriggerSystemFault()) {
            postEvent(SystemEventV3.SYSTEM_FAULT_DETECTED, null);
        }

        // 检查严重故障
        if (shouldTriggerCriticalFault()) {
            postEvent(SystemEventV3.CRITICAL_FAULT, null);
        }
    }

    /**
     * 检查是否应该触发初始化完成事件
     */
    private boolean shouldTriggerInitComplete() {
        return variables.getCurrentStateDurationSeconds() >= SystemConstants.SYSTEM_INIT_DELAY &&
                variables.getSystemHealthScore() >= SystemConstants.NORMAL_HEALTH_THRESHOLD &&
                allSegmentsReady();
    }

    /**
     * 检查是否应该触发过渡完成事件
     */
    private boolean shouldTriggerTransitionComplete() {
        return variables.getTransitionDurationSeconds() >= SystemConstants.TRANSITION_TIME &&
                checkAllSegmentsCleared();
    }

    /**
     * 检查是否应该触发过渡超时事件
     */
    private boolean shouldTriggerTransitionTimeout() {
        return variables.getTransitionDurationSeconds() >
                SystemConstants.TRANSITION_TIME * SystemConstants.TRANSITION_TIMEOUT_MULTIPLIER;
    }

    /**
     * 检查是否应该触发系统故障事件
     */
    private boolean shouldTriggerSystemFault() {
        return variables.getSystemHealthScore() < SystemConstants.NORMAL_HEALTH_THRESHOLD ||
                getFaultySegmentCount() > 1;
    }

    /**
     * 检查是否应该触发严重故障事件
     */
    private boolean shouldTriggerCriticalFault() {
        return variables.getSystemHealthScore() < SystemConstants.CRITICAL_HEALTH_THRESHOLD ||
                variables.getConsecutiveFaults() >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT ||
                getFaultySegmentCount() >= SystemConstants.TOTAL_SEGMENT_COUNT;
    }

    /**
     * 检查所有路段是否准备就绪
     */
    private boolean allSegmentsReady() {
        return segmentStateMachines.stream()
                .allMatch(segment -> segment.isRunning() && !segment.isFaultDetected());
    }

    // ==================== 系统健康度管理 ====================

    /**
     * 更新系统健康度
     */
    private void updateSystemHealth() {
        int baseScore = variables.getSystemHealthScore();

        // 基于路段状态调整健康度
        int healthySegments = (int) segmentStateMachines.stream()
                .mapToLong(segment -> segment.isFaultDetected() ? 0 : 1)
                .sum();

        double segmentHealthRatio = (double) healthySegments / SystemConstants.TOTAL_SEGMENT_COUNT;
        int segmentBonus = (int) (segmentHealthRatio * 10);

        // 基于连续故障调整
        int faultPenalty = variables.getConsecutiveFaults() * 5;

        // 基于通信状态调整
        int communicationPenalty = variables.getCommunicationStatus() == SystemVariables.CommunicationStatus.FAILED ? 20 : 0;

        // 计算新的健康度
        int newScore = Math.max(0, Math.min(100, baseScore + segmentBonus - faultPenalty - communicationPenalty));

        variables.setSystemHealthScore(newScore);
    }

    // ==================== 路段状态机通知 ====================

    /**
     * 通知路段状态机系统状态变化
     */
    private void notifySegmentsOfSystemStateChange(SystemStateV3 newState) {
        // 在感应控制模式下，确保所有路段状态机都在运行
        if (newState == SystemStateV3.INDUCTIVE_MODE) {
            for (SegmentStateMachine segment : segmentStateMachines) {
                if (!segment.isRunning()) {
                    segment.start();
                }
            }
        }

        // 在非感应模式下，可以考虑暂停某些路段状态机
        else if (newState == SystemStateV3.EMERGENCY_MODE || newState == SystemStateV3.MAINTENANCE_MODE) {
            // 这里可以根据需要暂停或调整路段状态机
            for (SegmentStateMachine segment : segmentStateMachines) {
                if (segment.isRunning()) {
                    segment.stop();
                }
            }
        }
    }

    // ==================== 公共接口方法 ====================

    /**
     * 发送事件到系统状态机
     */
    public void postEvent(SystemEventV3 event, Map<String, Object> eventData) {
        if (!isRunning) {
            logger.warn("系统状态机未运行，忽略事件: {}", event.getChineseName());
            return;
        }

        PrioritizedSystemEvent prioritizedEvent = new PrioritizedSystemEvent(event, eventData, LocalDateTime.now());
        try {
            eventQueue.offer(prioritizedEvent);
        } catch (Exception e) {
            logger.error("添加系统事件到队列失败: {}", e.getMessage());
        }
    }

    /**
     * 同步处理Event
     * @param eventV3
     * @param eventData
     * @return
     */
    public boolean processEvent(SystemEventV3 eventV3, Map<String, Object> eventData){
        return processSystemEvent(eventV3, eventData);
    }

    /**
     * 获取当前系统状态
     */
    public SystemStateV3 getCurrentState() {
        return currentState;
    }

    /**
     * 获取系统变量（只读）
     */
    public SystemVariables getVariables() {
        return variables;
    }

    /**
     * 获取路段状态机
     */
    public List<SegmentStateMachine> getSegmentStateMachines() {
        return new ArrayList<>(segmentStateMachines);
    }

    /**
     * 获取指定路段状态机
     */
    public SegmentStateMachine getSegmentStateMachine(int segmentId) {
        return segmentStateMachines.stream()
                .filter(segment -> segment.getSegmentId() == segmentId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查系统是否运行中
     */
    public boolean isRunning() {
        return isRunning;
    }

    // ==================== 调试和监控方法 ====================

    /**
     * 生成系统诊断报告
     */
    public String generateSystemDiagnosticReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 顶层系统状态机诊断报告 ===\n");
        report.append("当前状态: ").append(currentState.getChineseName()).append("\n");
        report.append("运行状态: ").append(isRunning ? "运行中" : "已停止").append("\n");
        report.append("系统健康度: ").append(variables.getSystemHealthScore()).append("/100\n");
        report.append("连续故障次数: ").append(variables.getConsecutiveFaults()).append("\n");
        report.append("路段总数: ").append(SystemConstants.TOTAL_SEGMENT_COUNT).append("\n");
        report.append("故障路段数: ").append(getFaultySegmentCount()).append("\n");
        report.append("清空完成率: ").append(String.format("%.1f%%", variables.getClearanceCompletionPercentage() * 100)).append("\n");
        report.append("通信状态: ").append(variables.getCommunicationStatus().getDescription()).append("\n");
        report.append("电源状态: ").append(variables.getPowerStatus().getDescription()).append("\n");
        report.append("事件队列大小: ").append(eventQueue.size()).append("\n");
        report.append("状态转换次数: ").append(transitionHistory.size()).append("\n");

        // 添加路段状态摘要
        report.append("\n=== 路段状态摘要 ===\n");
        for (SegmentStateMachine segment : segmentStateMachines) {
            report.append("路段 ").append(segment.getSegmentId())
                    .append(": ").append(segment.getCurrentState().getChineseName())
                    .append(" (健康度: ").append(segment.getVariables().getSegmentHealthScore())
                    .append(", 故障: ").append(segment.isFaultDetected() ? "是" : "否").append(")\n");
        }

        return report.toString();
    }

    /**
     * 获取系统状态转换历史
     */
    public List<SystemStateTransitionRecord> getTransitionHistory() {
        return new ArrayList<>(transitionHistory);
    }

    /**
     * 记录系统状态转换
     */
    private void recordSystemStateTransition(SystemStateV3 fromState, SystemStateV3 toState,
                                             SystemEventV3 triggerEvent, LocalDateTime transitionTime) {
        SystemStateTransitionRecord record = new SystemStateTransitionRecord(
                fromState, toState, triggerEvent, transitionTime);

        transitionHistory.add(record);

        // 保持历史记录大小限制
        if (transitionHistory.size() > 500) {
            transitionHistory.remove(0);
        }
    }

    // ==================== 辅助方法 =====================
    /**
     * 验证配置已加载
     */
    private boolean verifyConfigurationLoaded() {
        // 验证配置文件是否正确加载
        if(configService.isValidConfig()) {
            logger.debug("验证配置加载状态");
            return true; // 简化实现
        }else{
            logger.debug("验证配置加载失败");
            return false; // 简化实现
        }
    }
    /**
     * 系统自检
     */
    private boolean performSystemSelfCheck() {
        // 实现系统自检逻辑
        // 简化实现
        if(crossInfoManager.checkHealthStatus()) {
            logger.debug("执行系统自检 成功");
            return true;
        }else{
            logger.debug("执行系统自检 失败，继续等待");
        }
        return false; // 简化实现
    }
    /**
     * 设置所有信号灯为红灯
     */
    private void setAllTrafficLightsToRed() {
        logger.info("设置所有信号灯为红灯");
        // 实现信号灯控制逻辑
        crossInfoManager.controlAllCrossesToAllRed(3);
    }

    // ==================== 内部类定义 ====================

    /**
     * 优先级系统事件类
     */
    private static class PrioritizedSystemEvent implements Comparable<PrioritizedSystemEvent> {
        final SystemEventV3 event;
        final Map<String, Object> eventData;
        final LocalDateTime timestamp;
        final int priority;

        PrioritizedSystemEvent(SystemEventV3 event, Map<String, Object> eventData, LocalDateTime timestamp) {
            this.event = event;
            this.eventData = eventData;
            this.timestamp = timestamp;
            this.priority = event.getPriority().getLevel();
        }

        @Override
        public int compareTo(PrioritizedSystemEvent other) {
            // 优先级高的先处理
            int priorityComparison = Integer.compare(other.priority, this.priority);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            // 相同优先级按时间顺序
            return this.timestamp.compareTo(other.timestamp);
        }
    }

    /**
     * 系统状态转换记录类
     */
    public static class SystemStateTransitionRecord {
        private final SystemStateV3 fromState;
        private final SystemStateV3 toState;
        private final SystemEventV3 triggerEvent;
        private final LocalDateTime transitionTime;

        public SystemStateTransitionRecord(SystemStateV3 fromState, SystemStateV3 toState,
                                           SystemEventV3 triggerEvent, LocalDateTime transitionTime) {
            this.fromState = fromState;
            this.toState = toState;
            this.triggerEvent = triggerEvent;
            this.transitionTime = transitionTime;
        }

        // Getter方法
        public SystemStateV3 getFromState() { return fromState; }
        public SystemStateV3 getToState() { return toState; }
        public SystemEventV3 getTriggerEvent() { return triggerEvent; }
        public LocalDateTime getTransitionTime() { return transitionTime; }

        @Override
        public String toString() {
            return String.format("[%s] %s -> %s (触发: %s)",
                    transitionTime, fromState.getChineseName(),
                    toState.getChineseName(), triggerEvent.getChineseName());
        }
    }
}
