package com.traffic.config.statemachinev3.core;

import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import com.traffic.config.statemachinev3.constants.SegmentConstants;
import com.traffic.config.statemachinev3.guards.SegmentGuards;
import com.traffic.config.statemachinev3.actions.SegmentActions;
import com.traffic.config.statemachinev3.clearance.ClearanceDecisionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 路段状态机核心引擎
 *
 * 实现完整的扩展有限状态机(EFSM)：
 * M_segment = (Q, Σ, V, δ, G, A, q₀, F, P, C)
 *
 * 核心功能：
 * - 事件驱动的状态转换
 * - 守护条件检查
 * - 动作函数执行
 * - 清空决策集成
 * - 优先级事件处理
 *
 * @author System
 * @version 3.0.0
 */
public class SegmentStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(SegmentStateMachine.class);

    // ==================== 状态机核心组件 ====================

    /**
     * 当前状态 (q ∈ Q)
     */
    private volatile SegmentState currentState;

    /**
     * 状态变量集合 (V)
     */
    private final SegmentVariables variables;

    /**
     * 清空决策引擎
     */
    private final ClearanceDecisionEngine clearanceEngine;

    /**
     * 事件队列 (支持优先级)
     */
    private final BlockingQueue<PrioritizedEvent> eventQueue;

    /**
     * 状态转换历史 (用于调试和分析)
     */
    private final List<StateTransitionRecord> transitionHistory;

    /**
     * 状态机是否运行中
     */
    private volatile boolean isRunning;

    /**
     * 最后处理的事件时间
     */
    private volatile LocalDateTime lastEventProcessTime;

    // ==================== 构造函数和初始化 ====================

    /**
     * 构造函数
     * @param segmentId 路段ID
     */
    public SegmentStateMachine(int segmentId) {
        this.variables = new SegmentVariables(segmentId);
        this.clearanceEngine = new ClearanceDecisionEngine();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.transitionHistory = Collections.synchronizedList(new ArrayList<>());
        this.currentState = SegmentState.ALL_RED_CLEAR;
        this.isRunning = false;
        this.lastEventProcessTime = LocalDateTime.now();

        // 初始化路段容量
        this.variables.setUpstreamCapacity(SegmentConstants.DEFAULT_UPSTREAM_CAPACITY);
        this.variables.setDownstreamCapacity(SegmentConstants.DEFAULT_DOWNSTREAM_CAPACITY);

        logger.info("路段状态机初始化完成 - 路段ID: {}, 初始状态: {}",
                segmentId, currentState.getChineseName());
    }

    // ==================== 状态机主循环 ====================

    /**
     * 启动状态机
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            // 发送初始化完成事件
            postEvent(SegmentEvent.CLEARANCE_COMPLETE, null);
            logger.info("路段 {} 状态机启动", variables.getSegmentId());
        }
    }

    /**
     * 停止状态机
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            eventQueue.clear();
            logger.info("路段 {} 状态机停止", variables.getSegmentId());
        }
    }

    /**
     * 主要的事件处理循环
     * 实现状态转换函数 δ: Q × Σ × V* → Q
     */
    public boolean processEvents() {
        if (!isRunning) {
            return false;
        }

        try {
            // 处理所有待处理事件
            while (!eventQueue.isEmpty() && isRunning) {
                PrioritizedEvent prioritizedEvent = eventQueue.poll();
                if (prioritizedEvent != null) {
                    processEvent(prioritizedEvent.event, prioritizedEvent.data);
                }
            }

            // 更新清空决策
            updateClearanceDecisions();

            // 检查自动事件触发条件
            checkAndTriggerAutoEvents();

            // 更新最后处理时间
            lastEventProcessTime = LocalDateTime.now();
            return true;
        } catch (Exception e) {
            logger.warn("路段 {} 事件处理异常: {}", variables.getSegmentId(), e.getMessage());
            variables.incrementConsecutiveErrors();
            return false;
        }
    }

    /**
     * 处理定时器滴答事件
     * 这是状态机的心跳，触发各种检查和自动处理
     */
    public void processTimerTick() {
        if (!isRunning) {
            return;
        }

        // 发送定时器事件
        postEvent(SegmentEvent.TIMER_TICK, null);

        // 处理所有事件
        processEvents();
    }

    // ==================== 事件处理核心逻辑 ====================

    /**
     * 处理单个事件
     * 实现完整的状态转换逻辑：δ(q, σ, v) → q'
     */
    private boolean processEvent(SegmentEvent event, Map<String, Object> eventData) {
        SegmentState oldState = currentState;
        LocalDateTime eventTime = LocalDateTime.now();

        try {
            // 1. 检查事件是否适用于当前状态
            if (!isEventApplicable(event, currentState)) {
                logger.debug("路段 {} 事件 {} 不适用于当前状态 {}",
                        variables.getSegmentId(), event.getChineseName(), currentState.getChineseName());
                return false;
            }

            // 2. 确定目标状态
            SegmentState targetState = determineTargetState(event, currentState);
            if (targetState == null) {
                logger.debug("路段 {} 事件 {} 无法确定目标状态",
                        variables.getSegmentId(), event.getChineseName());
                return false;
            }

            // 3. 检查守护条件 G(q, σ, v)
            if (!checkGuardCondition(event, targetState)) {
                logger.debug("路段 {} 状态转换被守护条件阻止: {} -> {}, event:{}",
                        variables.getSegmentId(), currentState.getChineseName(), targetState.getChineseName(), event.getChineseName());
                return false;
            }

            // 4. 执行动作函数 A(q, σ, v)
            executeAction(event, targetState, eventData);

            // 5. 状态转换 q' = δ(q, σ, v)
            if (targetState != currentState) {
                transitionToState(targetState, event);
            }

            // 6. 记录状态转换
            recordStateTransition(oldState, currentState, event, eventTime);

            return true;
        } catch (Exception e) {
            logger.warn("路段 {} 处理事件 {} 时发生异常: {}", variables.getSegmentId(), event.getChineseName(), e.getMessage());
            variables.incrementConsecutiveErrors();
            return false;
        }
    }

    /**
     * 检查守护条件
     * 实现守护条件函数 G: Q × Σ × V* → Boolean
     */
    private boolean checkGuardCondition(SegmentEvent event, SegmentState targetState) {
        switch (event) {
            case TIMER_TICK -> {
                if (targetState == SegmentState.ALL_RED_CLEAR) {
                    return SegmentGuards.checkGreenToRedTransition(currentState, event, variables);
                } else if (targetState.isGreenState()) {
                    return SegmentGuards.checkRedToGreenTransition(currentState, targetState, event, variables);
                }
            }
            case FORCE_SWITCH -> {
                return SegmentGuards.checkForceSwitch(currentState, event, variables);
            }
            case VEHICLE_ENTER_UPSTREAM, VEHICLE_EXIT_UPSTREAM, VEHICLE_ENTER_DOWNSTREAM, VEHICLE_EXIT_DOWNSTREAM -> {
                return SegmentGuards.checkVehicleEventAllowed(currentState, event, variables);
            }
            case SENSOR_FAULT -> {
                return SegmentGuards.checkSensorFault(currentState, event, variables);
            }
            case SYSTEM_RESET -> {  //RECOVERY_REQUEST
                return SegmentGuards.checkRedToGreenTransition(currentState, targetState, event, variables);
            }
            case CLEARANCE_COMPLETE -> { //SYSTEM_INIT_COMPLETE
                //return currentState == SegmentState.IDLE;
                return SegmentGuards.checkRedToGreenTransition(currentState, targetState, event, variables);
            }
        }
        return true; // 默认允许
    }

    /**
     * 执行动作函数
     * 实现动作函数 A: Q × Σ × V* → V*
     */
    private void executeAction(SegmentEvent event, SegmentState targetState, Map<String, Object> eventData) {
        switch (event) {
            case TIMER_TICK -> SegmentActions.executeTimerTick(currentState, event, variables);
            case CLEARANCE_COMPLETE -> SegmentActions.executeEnterIdleState(currentState, targetState, event, variables);
            case FORCE_SWITCH -> SegmentActions.executeForceSwitch(currentState, event, variables);
            case VEHICLE_ENTER_UPSTREAM, VEHICLE_ENTER_DOWNSTREAM -> {
                String vehicleId = (String) eventData.get("vehicleId");
                SegmentVariables.Direction direction = (SegmentVariables.Direction) eventData.get("direction");
                SegmentActions.executeVehicleEnter(currentState, event, variables, vehicleId, direction);
            }
            case VEHICLE_EXIT_UPSTREAM, VEHICLE_EXIT_DOWNSTREAM -> {
                String vehicleId = (String) eventData.get("vehicleId");
                SegmentVariables.Direction direction = (SegmentVariables.Direction) eventData.get("direction");
                SegmentActions.executeVehicleExit(currentState, event, variables, vehicleId, direction);
            }
            case SENSOR_FAULT -> SegmentActions.executeEnterFaultMode(currentState, targetState, event, variables);
            case SYSTEM_RESET -> SegmentActions.executeRecoveryFromFault(currentState, event, variables);//RECOVERY_REQUEST
        }

        // 如果是状态转换，执行相应的进入动作
        if (targetState != currentState) {
            executeStateEntryAction(targetState, event);
        }
    }

    /**
     * 执行状态进入动作
     */
    private void executeStateEntryAction(SegmentState newState, SegmentEvent triggerEvent) {
        switch (newState) {
            case UPSTREAM_GREEN, DOWNSTREAM_GREEN ->
                    SegmentActions.executeEnterGreenState(currentState, newState, triggerEvent, variables);
            case ALL_RED_CLEAR ->
                    SegmentActions.executeEnterAllRedClear(currentState, newState, triggerEvent, variables);
//            case FAULT_MODE -> // 路段不包含该状态
//                    SegmentActions.executeEnterFaultMode(currentState, newState, triggerEvent, variables);
//            case MAINTENANCE -> // 路段不包含该状态
//                    SegmentActions.executeEnterMaintenanceMode(currentState, newState, triggerEvent, variables);
        }
    }

    // ==================== 状态转换逻辑 ====================

    /**
     * 确定目标状态
     * 基于当前状态和事件类型确定下一个状态
     */
    private SegmentState determineTargetState(SegmentEvent event, SegmentState currentState) {
        switch (event) {
            case TIMER_TICK -> {
                return determineTimerTickTargetState();
            }
            case FORCE_SWITCH -> {
                if (currentState.isGreenState()) {
                    return SegmentState.ALL_RED_CLEAR;
                }
            }
            case GREEN_TIMEOUT -> {
                return SegmentState.ALL_RED_CLEAR;
            }
            case VEHICLE_ENTER_UPSTREAM, VEHICLE_EXIT_UPSTREAM, VEHICLE_ENTER_DOWNSTREAM, VEHICLE_EXIT_DOWNSTREAM -> {
                // 车辆事件不改变状态，只更新变量
                return currentState;
            }
            case CLEARANCE_COMPLETE -> {
                return determineGreenStateFromRed();
            }
            case CLEAR_TIMEOUT -> {
                // 黄闪，停止感应，需人工介入
                logger.info("Event:{} 处理流程缺失", event.getChineseName());
                return null;
            }
        }
        logger.info("Event:{} 不能被处理", event.getChineseName());
        return null;
    }

    /**
     * 基于定时器事件确定目标状态
     * 这是状态机的核心逻辑，实现感应控制算法
     */
    private SegmentState determineTimerTickTargetState() {
        switch (currentState) {
            case ALL_RED_CLEAR -> {
                // 从全红清空状态转换到绿灯状态
                return determineGreenStateFromRed();
            }
            case UPSTREAM_GREEN -> {    // 目标状态是确定的，不应该有条件，是否转换是有条件的
                // 检查是否需要从上行绿灯转换到全红清空
                //if (shouldSwitchFromUpstreamGreen()) {
                return SegmentState.ALL_RED_CLEAR;
                //}
            }
            case DOWNSTREAM_GREEN -> {
                // 检查是否需要从下行绿灯转换到全红清空
                //if (shouldSwitchFromDownstreamGreen()) {
                return SegmentState.ALL_RED_CLEAR;
                //}
            }
//            case FAULT_MODE -> {
//                // 故障模式下不自动转换状态
//                return SegmentState.FAULT_MODE;
//            }
//            case MAINTENANCE -> {
//                // 维护模式下不自动转换状态
//                return SegmentState.MAINTENANCE;
//            }
        }
        return null;
    }

    /**
     * 从全红清空状态确定下一个绿灯状态
     * 基于优先级算法和通行请求
     */
    private SegmentState determineGreenStateFromRed() {
        // 检查清空条件是否满足
        if (!isClearanceConditionMet()) {
            return SegmentState.ALL_RED_CLEAR; // 继续等待清空
        }

        // 基于优先级算法确定服务方向
        SegmentVariables.Direction priorityDirection = determinePriorityDirection();

        switch (priorityDirection) {
            case UPSTREAM -> {
                return SegmentState.UPSTREAM_GREEN;
            }
            case DOWNSTREAM -> {
                return SegmentState.DOWNSTREAM_GREEN;
            }
        }

        // 如果优先级方向没有请求，检查另一个方向, 理论上应该不会出现下面的情况
        if (variables.isUpstreamRequest()) {
            return SegmentState.UPSTREAM_GREEN;
        } else if (variables.isDownstreamRequest()) {
            return SegmentState.DOWNSTREAM_GREEN;
        }

        return SegmentState.ALL_RED_CLEAR; // 没有请求时保持全红
    }

    /**
     * 判断是否应该从上行绿灯状态切换
     */
    private boolean shouldSwitchFromUpstreamGreen() {
        // 检查强制切换条件
        if (variables.isGreenTimeout() ||
                isCapacityNearFull(SegmentVariables.Direction.UPSTREAM)) {
            return true;
        }
        // 检查最小绿灯时间
        if (!variables.isMinGreenTimeReached()) {
            return false;
        }

        // 检查是否有下行请求
        if (variables.isDownstreamRequest()) {
            return true;
        }

        // 基于优先级判断是否切换
        return shouldSwitchBasedOnPriority(SegmentVariables.Direction.DOWNSTREAM);
    }

    /**
     * 判断是否应该从下行绿灯状态切换
     */
    private boolean shouldSwitchFromDownstreamGreen() {
        // 检查最小绿灯时间
        if (!variables.isMinGreenTimeReached()) {
            return false;
        }

        // 检查是否有上行请求
        if (!variables.isUpstreamRequest()) {
            return false;
        }

        // 检查强制切换条件
        if (variables.isGreenTimeout() ||
                isCapacityNearFull(SegmentVariables.Direction.DOWNSTREAM)) {
            return true;
        }

        // 基于优先级判断是否切换
        return shouldSwitchBasedOnPriority(SegmentVariables.Direction.UPSTREAM);
    }

    // ==================== 清空决策集成 ====================

    /**
     * 更新清空决策
     * 集成清空决策引擎的结果到状态机
     */
    private void updateClearanceDecisions() {
        // 计算各方向的清空决策
        ClearanceDecision upstreamDecision = clearanceEngine.calculateUpstreamClearance(variables);
        ClearanceDecision downstreamDecision = clearanceEngine.calculateDownstreamClearance(variables);
        ClearanceDecision overallDecision = clearanceEngine.calculateOverallClearance(variables);

        // 更新到变量中
        variables.setUpstreamClearanceDecision(upstreamDecision);
        variables.setDownstreamClearanceDecision(downstreamDecision);
        variables.setOverallClearanceDecision(overallDecision);

        // 处理保守清空
        handleConservativeClearance(overallDecision);

        // 检查强制清空条件
        if (clearanceEngine.shouldForceConservativeClearance(variables)) {
            variables.setUpstreamClearanceDecision(ClearanceDecision.SAFE);
            variables.setDownstreamClearanceDecision(ClearanceDecision.SAFE);
            variables.setOverallClearanceDecision(ClearanceDecision.SAFE);
            clearanceEngine.stopConservativeClearanceTimer(variables);
        }
    }

    /**
     * 处理保守清空逻辑
     */
    private void handleConservativeClearance(ClearanceDecision overallDecision) {
        if (overallDecision == ClearanceDecision.CONSERVATIVE) {
            clearanceEngine.startConservativeClearanceTimer(variables);
        } else if (overallDecision == ClearanceDecision.SAFE || overallDecision == ClearanceDecision.WARNING) {
            clearanceEngine.stopConservativeClearanceTimer(variables);
        }
    }

    /**
     * 检查清空条件是否满足
     */
    private boolean isClearanceConditionMet() {
        ClearanceDecision overallDecision = variables.getOverallClearanceDecision();
        return overallDecision == ClearanceDecision.SAFE ||
                overallDecision == ClearanceDecision.WARNING ||
                clearanceEngine.shouldForceConservativeClearance(variables);
    }

    // ==================== 优先级算法 ====================

    /**
     * 确定优先级方向
     * 实现感应控制的核心优先级算法
     */
    private SegmentVariables.Direction determinePriorityDirection() {
        // 如果只有一个方向有请求，直接返回
        if (variables.isUpstreamRequest() && !variables.isDownstreamRequest()) {
            return SegmentVariables.Direction.UPSTREAM;
        }
        if (!variables.isUpstreamRequest() && variables.isDownstreamRequest()) {
            return SegmentVariables.Direction.DOWNSTREAM;
        }

        // 如果两个方向都有请求,或者都无请求，使用交替策略，暂时不使用得分，后期可尝试基于优先级得分判断
        if (variables.isUpstreamRequest() && variables.isDownstreamRequest()) {

            return alternateDirection();
//            double upstreamScore = variables.getPriorityScoreUpstream();
//            double downstreamScore = variables.getPriorityScoreDownstream();
//
//            // 如果得分相近，采用交替策略
//            if (Math.abs(upstreamScore - downstreamScore) < SegmentConstants.PRIORITY_SCORE_THRESHOLD) {
//                return alternateDirection();
//            }
//
//            return upstreamScore > downstreamScore ?
//                    SegmentVariables.Direction.UPSTREAM : SegmentVariables.Direction.DOWNSTREAM;
        }

        // 如果都没有请求，返回交替方向
        return alternateDirection();
        //return SegmentVariables.Direction.NONE;
    }

    /**
     * 交替方向选择
     */
    private SegmentVariables.Direction alternateDirection() {
        SegmentVariables.Direction lastServed = variables.getLastServedDirection();
        return lastServed == SegmentVariables.Direction.UPSTREAM ?
                SegmentVariables.Direction.DOWNSTREAM : SegmentVariables.Direction.UPSTREAM;
    }

    /**
     * 基于优先级判断是否应该切换
     */
    private boolean shouldSwitchBasedOnPriority(SegmentVariables.Direction requestDirection) {
        SegmentVariables.Direction priorityDirection = determinePriorityDirection();
        return priorityDirection == requestDirection;
    }

    // ==================== 辅助判断方法 ====================

    /**
     * 检查容量是否接近满载
     */
    private boolean isCapacityNearFull(SegmentVariables.Direction direction) {
        switch (direction) {
            case UPSTREAM -> {
                return variables.getUpstreamVehicleIds().size() >=
                        variables.getUpstreamCapacity() * 0.9;
            }
            case DOWNSTREAM -> {
                return variables.getDownstreamVehicleIds().size() >=
                        variables.getDownstreamCapacity() * 0.9;
            }
        }
        return false;
    }

    /**
     * 检查事件是否适用于当前状态
     */
    private boolean isEventApplicable(SegmentEvent event, SegmentState state) {
        // 系统事件在所有状态下都适用 - 不存在这种状态，暂时禁用，以后根据需求确定是否添加
//        if (event.isSystemEvent()) {
//            return true;
//        }

        // 故障事件在所有非故障状态下都适用，路段状态机没有故障状态，这个条件暂时也用不上
//        if (event == SegmentEvent.FAULT_DETECTED && state != SegmentState.FAULT_MODE) {
//            return true;
//        }

        // 恢复事件只在故障状态下适用，不存在恢复请求，该条件目前不适用
//        if (event == SegmentEvent.RECOVERY_REQUEST) {
//            return state == SegmentState.FAULT_MODE;
//        }

        // 车辆事件在运行状态下适用，不存在其它状态，这里应该永远返回true
        if (event == SegmentEvent.VEHICLE_ENTER_UPSTREAM || event == SegmentEvent.VEHICLE_EXIT_UPSTREAM ||
        event == SegmentEvent.VEHICLE_EXIT_DOWNSTREAM || event == SegmentEvent.VEHICLE_ENTER_DOWNSTREAM) {
            //return state != SegmentState.FAULT_MODE && state != SegmentState.MAINTENANCE;
        }

        return true;
    }

    /**
     * 状态转换
     */
    private void transitionToState(SegmentState newState, SegmentEvent triggerEvent) {
        SegmentState oldState = currentState;
        currentState = newState;

        logger.info("路段 {} 状态转换: {} -> {} (触发事件: {})",
                variables.getSegmentId(), oldState.getChineseName(),
                newState.getChineseName(), triggerEvent.getChineseName());
    }

    // ==================== 自动事件触发 ====================

    /**
     * 检查并触发自动事件
     */
    private void checkAndTriggerAutoEvents() {
        // 检查故障条件
//        if (shouldTriggerFaultEvent()) {
//            postEvent(SegmentEvent.FAULT_DETECTED, null);
//        }

        // 检查强制切换条件
        if (shouldTriggerForceSwitch()) {
            postEvent(SegmentEvent.FORCE_SWITCH, null);
        }
    }

    /**
     * 检查是否应该触发故障事件
     */
    private boolean shouldTriggerFaultEvent() {
        if (variables.isFaultDetected()) {
            return false; // 已经是故障状态
        }

        // 检查各种故障条件
        return variables.getErrorCountMismatch() >= SegmentConstants.MAX_COUNTER_MISMATCH_ERRORS ||
                variables.getErrorCountIdLogic() >= SegmentConstants.MAX_ID_LOGIC_ERRORS ||
                variables.getConsecutiveErrors() >= SegmentConstants.MAX_CONSECUTIVE_ERRORS ||
                variables.getSegmentHealthScore() < SegmentConstants.CRITICAL_HEALTH_THRESHOLD;
    }

    /**
     * 检查是否应该触发强制切换
     */
    private boolean shouldTriggerForceSwitch() {
        // 如果绿灯时间过长
        if (currentState.isGreenState() && variables.isGreenTimeout()) {
            return true;
        }

        // 如果容量达到上限
        if (currentState == SegmentState.UPSTREAM_GREEN &&
                variables.getUpstreamVehicleIds().size() >= variables.getUpstreamCapacity()) {
            return true;
        }

        if (currentState == SegmentState.DOWNSTREAM_GREEN &&
                variables.getDownstreamVehicleIds().size() >= variables.getDownstreamCapacity()) {
            return true;
        }

        return false;
    }

    // ==================== 公共接口方法 ====================

    /**
     * 发送事件到状态机
     */
    public void postEvent(SegmentEvent event, Map<String, Object> eventData) {
        if (!isRunning) {
            logger.warn("路段 {} 状态机未运行，忽略事件: {}", variables.getSegmentId(), event.getChineseName());
            return;
        }

        PrioritizedEvent prioritizedEvent = new PrioritizedEvent(event, eventData, LocalDateTime.now());
        try {
            eventQueue.offer(prioritizedEvent);
        } catch (Exception e) {
            logger.error("路段 {} 添加事件到队列失败: {}", variables.getSegmentId(), e.getMessage());
        }
    }

    /**
     * 同步处理Event
     * @param event
     * @param eventData
     * @return
     */
    public boolean processSegmentEvent(SegmentEvent event, Map<String, Object> eventData) {
        return processEvent(event, eventData);
    }
    /**
     * 处理车辆进入事件
     */
    public void onVehicleEnter(String vehicleId, SegmentVariables.Direction direction) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("vehicleId", vehicleId);
        eventData.put("direction", direction);
        switch (direction){
            case UPSTREAM -> { postEvent(SegmentEvent.VEHICLE_ENTER_UPSTREAM, eventData); }
            default -> { postEvent(SegmentEvent.VEHICLE_ENTER_DOWNSTREAM, eventData); }
        }
    }

    /**
     * 处理车辆离开事件
     */
    public void onVehicleExit(String vehicleId, SegmentVariables.Direction direction) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("vehicleId", vehicleId);
        eventData.put("direction", direction);
        switch (direction){
            case UPSTREAM -> { postEvent(SegmentEvent.VEHICLE_EXIT_UPSTREAM, eventData); }
            default -> { postEvent(SegmentEvent.VEHICLE_EXIT_DOWNSTREAM, eventData); }
        }
    }

    /**
     * 获取当前清空决策
     */
    public ClearanceDecision getCurrentClearanceDecision() {
        return variables.getOverallClearanceDecision();
    }

    /**
     * 获取路段ID
     */
    public int getSegmentId() {
        return variables.getSegmentId();
    }

    /**
     * 获取当前状态
     */
    public SegmentState getCurrentState() {
        return currentState;
    }

    /**
     * 获取状态变量（只读副本）
     */
    public SegmentVariables getVariables() {
        return variables; // 注意：实际应用中可能需要返回只读副本
    }

    /**
     * 检查是否检测到故障
     */
    public boolean isFaultDetected() {
        return variables.isFaultDetected();
    }

    /**
     * 获取状态机运行状态
     */
    public boolean isRunning() {
        return isRunning;
    }

    // ==================== 调试和监控方法 ====================

    /**
     * 获取状态转换历史
     */
    public List<StateTransitionRecord> getTransitionHistory() {
        return new ArrayList<>(transitionHistory);
    }

    /**
     * 生成状态机诊断报告
     */
    public String generateDiagnosticReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 路段 ").append(variables.getSegmentId()).append(" 状态机诊断报告 ===\n");
        report.append("当前状态: ").append(currentState.getChineseName()).append("\n");
        report.append("运行状态: ").append(isRunning ? "运行中" : "已停止").append("\n");
        report.append("事件队列大小: ").append(eventQueue.size()).append("\n");
        report.append("最后事件处理时间: ").append(lastEventProcessTime).append("\n");
        report.append("当前清空决策: ").append(variables.getOverallClearanceDecision()).append("\n");
        report.append("健康度评分: ").append(variables.getSegmentHealthScore()).append("\n");
        report.append("故障状态: ").append(variables.isFaultDetected() ? "是" : "否").append("\n");
        report.append("上行车辆数: ").append(variables.getUpstreamVehicleIds().size()).append("/").append(variables.getUpstreamCapacity()).append("\n");
        report.append("下行车辆数: ").append(variables.getDownstreamVehicleIds().size()).append("/").append(variables.getDownstreamCapacity()).append("\n");
        report.append("状态转换次数: ").append(transitionHistory.size()).append("\n");

        return report.toString();
    }

    /**
     * 记录状态转换
     */
    private void recordStateTransition(SegmentState fromState, SegmentState toState,
                                       SegmentEvent triggerEvent, LocalDateTime transitionTime) {
        StateTransitionRecord record = new StateTransitionRecord(
                fromState, toState, triggerEvent, transitionTime);

        transitionHistory.add(record);

        // 保持历史记录大小限制
        if (transitionHistory.size() > 1000) {
            transitionHistory.remove(0);
        }
    }

    // ==================== 内部类定义 ====================

    /**
     * 优先级事件类
     */
    private static class PrioritizedEvent implements Comparable<PrioritizedEvent> {
        final SegmentEvent event;
        final Map<String, Object> data;
        final LocalDateTime timestamp;
        final int priority;

        PrioritizedEvent(SegmentEvent event, Map<String, Object> data, LocalDateTime timestamp) {
            this.event = event;
            this.data = data;
            this.timestamp = timestamp;
            this.priority = event.getPriority().getLevel();
        }

        @Override
        public int compareTo(PrioritizedEvent other) {
            // 优先级高的先处理（数值大的优先级高）
            int priorityComparison = Integer.compare(other.priority, this.priority);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            // 相同优先级按时间顺序
            return this.timestamp.compareTo(other.timestamp);
        }
    }

    /**
     * 状态转换记录类
     */
    public static class StateTransitionRecord {
        private final SegmentState fromState;
        private final SegmentState toState;
        private final SegmentEvent triggerEvent;
        private final LocalDateTime transitionTime;

        public StateTransitionRecord(SegmentState fromState, SegmentState toState,
                                     SegmentEvent triggerEvent, LocalDateTime transitionTime) {
            this.fromState = fromState;
            this.toState = toState;
            this.triggerEvent = triggerEvent;
            this.transitionTime = transitionTime;
        }

        // Getter方法
        public SegmentState getFromState() { return fromState; }
        public SegmentState getToState() { return toState; }
        public SegmentEvent getTriggerEvent() { return triggerEvent; }
        public LocalDateTime getTransitionTime() { return transitionTime; }

        @Override
        public String toString() {
            return String.format("[%s] %s -> %s (触发: %s)",
                    transitionTime, fromState.getChineseName(),
                    toState.getChineseName(), triggerEvent.getChineseName());
        }
    }
}
