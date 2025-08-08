package com.traffic.config.statemachinev2.core;

import com.traffic.config.service.ConfigService;
import com.traffic.config.signalplatform.platformbase.CrossInfoManager;
import com.traffic.config.statemachinev2.enums.SystemStateV2;
import com.traffic.config.statemachinev2.enums.SystemEventV2;
import com.traffic.config.statemachinev2.enums.ext.ClearanceDecision;
import com.traffic.config.statemachinev2.variables.SystemVariables;
import com.traffic.config.statemachinev2.constants.SystemConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 系统状态机核心实现
 * 实现顶层系统状态机的转换函数、守护条件函数和动作函数
 *
 * @author System
 * @version 2.0.0
 */
@Component
public class SystemStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(SystemStateMachine.class);

    // 当前状态
    private final AtomicReference<SystemStateV2> currentState = new AtomicReference<>(SystemStateV2.SYSTEM_INIT);

    // 系统变量
    private final SystemVariables variables = new SystemVariables();

    // 状态机运行标志
    private volatile boolean running = false;

    @Autowired
    private CrossInfoManager crossInfoManager;

    @Autowired
    private ConfigService configService;
    // ==================== 状态机控制方法 ====================

    /**
     * 启动状态机
     */
    public void start() {
        if (!running) {
            running = true;
            currentState.set(SystemStateV2.SYSTEM_INIT);
            variables.setStateStartTime(LocalDateTime.now());
            variables.setSystemInitStartTime(LocalDateTime.now());
            logger.info("系统状态机启动，当前状态: {}", currentState.get().getChineseName());

            // 执行进入初始化状态的动作
            executeEnterInitAction();
        }
    }

    /**
     * 停止状态机
     */
    public void stop() {
        running = false;
        logger.info("系统状态机停止");
    }

    /**
     * 处理事件
     * @param event 输入事件
     * @return 是否处理成功
     */
    public boolean processEvent(SystemEventV2 event) {
        if (!running) {
            logger.warn("状态机未运行，忽略事件: {}", event.getChineseName());
            return false;
        }

        SystemStateV2 currentSt = currentState.get();
        logger.debug("处理事件: {} 在状态: {}", event.getChineseName(), currentSt.getChineseName());

        // 执行转换函数
        SystemStateV2 nextState = executeTransitionFunction(currentSt, event);

        if (nextState != null && nextState != currentSt) {
            // 状态转换
            return transitionToState(nextState, event);
        }

        return true; // 事件被处理但未触发状态转换
    }

    // ==================== 转换函数 δ_sys ====================

    /**
     * 转换函数实现
     * δ_sys(q, σ, v) → q'
     */
    private SystemStateV2 executeTransitionFunction(SystemStateV2 currentSt, SystemEventV2 event) {
        return switch (currentSt) {
            case SYSTEM_INIT -> transitionFromSystemInit(event);
            case ALL_RED_TRANSITION -> transitionFromAllRedTransition(event);
            case INDUCTIVE_MODE -> transitionFromInductiveMode(event);
            case DEGRADED_MODE -> transitionFromDegradedMode(event);
            case MAINTENANCE_MODE -> transitionFromMaintenanceMode(event);
            case EMERGENCY_MODE -> transitionFromEmergencyMode(event);
        };
    }

    /**
     * 从系统初始化状态的转换
     */
    private SystemStateV2 transitionFromSystemInit(SystemEventV2 event) {
        return switch (event) {
            case TIMER_TICK -> {
                if (guardInitComplete()) {
                    yield SystemStateV2.ALL_RED_TRANSITION;
                }
                yield SystemStateV2.SYSTEM_INIT; // 保持当前状态
            }
            case CRITICAL_FAULT, MANUAL_EMERGENCY -> SystemStateV2.EMERGENCY_MODE;
            case SYSTEM_RESET -> SystemStateV2.SYSTEM_INIT;
            default -> null; // 不处理的事件
        };
    }

    /**
     * 从全红过渡状态的转换
     */
    private SystemStateV2 transitionFromAllRedTransition(SystemEventV2 event) {
        return switch (event) {
            case TIMER_TICK -> {
                if (guardTransitionTimeout()) {
                    yield SystemStateV2.DEGRADED_MODE;
                } else if (guardTransitionComplete()) {
                    yield SystemStateV2.INDUCTIVE_MODE;
                }
                yield SystemStateV2.ALL_RED_TRANSITION; // 保持当前状态
            }
            case ALL_SEGMENTS_CLEARED -> {
                if (guardTransitionTimeElapsed()) {
                    yield SystemStateV2.INDUCTIVE_MODE;
                }
                yield SystemStateV2.ALL_RED_TRANSITION;
            }
            case CLEAR_TIMEOUT -> SystemStateV2.DEGRADED_MODE;
            case CRITICAL_FAULT, MANUAL_EMERGENCY -> SystemStateV2.EMERGENCY_MODE;
            case SYSTEM_RESET -> SystemStateV2.SYSTEM_INIT;
            default -> null;
        };
    }

    /**
     * 从感应控制模式的转换
     */
    private SystemStateV2 transitionFromInductiveMode(SystemEventV2 event) {
        return switch (event) {
            case FAULT_DETECTED, MANUAL_DEGRADE -> SystemStateV2.ALL_RED_TRANSITION;
            case CRITICAL_FAULT, MANUAL_EMERGENCY -> SystemStateV2.EMERGENCY_MODE;
            case MAINTENANCE_REQUEST -> SystemStateV2.MAINTENANCE_MODE;
            case SYSTEM_RESET -> SystemStateV2.SYSTEM_INIT;
            default -> null;
        };
    }

    /**
     * 从降级模式的转换
     */
    private SystemStateV2 transitionFromDegradedMode(SystemEventV2 event) {
        return switch (event) {
            case CONDITIONS_RESTORED -> {
                if (guardRecoveryConditionsMet()) {
                    yield SystemStateV2.ALL_RED_TRANSITION;
                }
                yield SystemStateV2.DEGRADED_MODE;
            }
            case MAINTENANCE_REQUEST -> SystemStateV2.MAINTENANCE_MODE;
            case CRITICAL_FAULT, MANUAL_EMERGENCY -> SystemStateV2.EMERGENCY_MODE;
            case SYSTEM_RESET -> SystemStateV2.SYSTEM_INIT;
            default -> null;
        };
    }

    /**
     * 从维护模式的转换
     */
    private SystemStateV2 transitionFromMaintenanceMode(SystemEventV2 event) {
        return switch (event) {
            case SYSTEM_RESET -> SystemStateV2.SYSTEM_INIT;
            case CRITICAL_FAULT, MANUAL_EMERGENCY -> SystemStateV2.EMERGENCY_MODE;
            default -> null;
        };
    }

    /**
     * 从紧急模式的转换
     */
    private SystemStateV2 transitionFromEmergencyMode(SystemEventV2 event) {
        return switch (event) {
            case SYSTEM_RESET -> SystemStateV2.SYSTEM_INIT;
            case RECOVERY_VERIFIED -> {
                if (guardRecoveryVerified()) {
                    yield SystemStateV2.ALL_RED_TRANSITION;
                }
                yield SystemStateV2.EMERGENCY_MODE;
            }
            default -> null;
        };
    }

    // ==================== 守护条件函数 G_sys ====================

    /**
     * 初始化完成守护条件
     * G_init_complete(SYSTEM_INIT, timer_tick, v)
     */
    private boolean guardInitComplete() {
        if (variables.getSystemInitStartTime() == null) {
            return false;
        }

        long initDuration = Duration.between(
                variables.getSystemInitStartTime(),
                LocalDateTime.now()
        ).getSeconds();

        boolean timeElapsed = initDuration >= SystemConstants.SYSTEM_INIT_DELAY;
        boolean systemCheckPassed = performSystemSelfCheck();
        boolean configLoaded = verifyConfigurationLoaded();

        boolean result = timeElapsed && systemCheckPassed && configLoaded;

        if (result) {
            logger.info("系统初始化完成 - 时间: {}s, 自检: {}, 配置: {}",
                    initDuration, systemCheckPassed, configLoaded);
        }

        return result;
    }

    /**
     * 过渡完成守护条件
     * G_transition_complete(ALL_RED_TRANSITION, timer_tick, v)
     */
    private boolean guardTransitionComplete() {
        boolean timeElapsed = guardTransitionTimeElapsed();
        boolean segmentsCleared = variables.isSegmentsCleared();

        boolean result = timeElapsed && segmentsCleared;

        if (result) {
            logger.info("全红过渡完成 - 时间已过: {}, 路段已清: {}", timeElapsed, segmentsCleared);
        }

        return result;
    }

    /**
     * 过渡时间已过守护条件
     */
    private boolean guardTransitionTimeElapsed() {
        if (variables.getTransitionStartTime() == null) {
            return false;
        }

        long transitionDuration = Duration.between(
                variables.getTransitionStartTime(),
                LocalDateTime.now()
        ).getSeconds();

        return transitionDuration >= SystemConstants.TRANSITION_TIME;
    }

    /**
     * 过渡超时守护条件
     * G_transition_timeout(ALL_RED_TRANSITION, timer_tick, v)
     */
    private boolean guardTransitionTimeout() {
        if (variables.getTransitionStartTime() == null) {
            return false;
        }

        long transitionDuration = Duration.between(
                variables.getTransitionStartTime(),
                LocalDateTime.now()
        ).getSeconds();

        boolean timeout = transitionDuration > SystemConstants.getTransitionTimeoutSeconds();

        if (timeout) {
            logger.warn("全红过渡超时 - 持续时间: {}s, 超时阈值: {}s",
                    transitionDuration, SystemConstants.getTransitionTimeoutSeconds());
        }

        return timeout;
    }

    /**
     * 恢复条件满足守护条件
     */
    private boolean guardRecoveryConditionsMet() {
        return variables.areRecoveryConditionsMet();
    }

    /**
     * 恢复验证守护条件
     */
    private boolean guardRecoveryVerified() {
        boolean systemCheckPassed = performSystemSelfCheck();
        boolean configConsistent = verifyConfigurationConsistency();
        boolean communicationStable = variables.isCommunicationNormal();

        return systemCheckPassed && configConsistent && communicationStable;
    }

    // ==================== 动作函数 A_sys ====================

    /**
     * 状态转换
     */
    private boolean transitionToState(SystemStateV2 nextState, SystemEventV2 triggerEvent) {
        SystemStateV2 previousState = currentState.get();

        try {
            // 执行退出当前状态的动作
            executeExitStateAction(previousState);

            // 更新状态
            currentState.set(nextState);
            variables.updateStateStartTime(nextState);

            // 执行进入新状态的动作
            executeEnterStateAction(nextState, previousState, triggerEvent);

            logger.info("状态转换: {} → {} (触发事件: {})",
                    previousState.getChineseName(),
                    nextState.getChineseName(),
                    triggerEvent.getChineseName());

            return true;

        } catch (Exception e) {
            logger.error("状态转换失败: {} → {}", previousState, nextState, e);
            return false;
        }
    }

    /**
     * 执行进入初始化状态的动作
     */
    private void executeEnterInitAction() {
        logger.info("执行系统初始化动作");

        // 重置系统变量
        variables.resetCounters();
        variables.setSystemHealthScore(SystemConstants.INITIAL_HEALTH_SCORE);
        variables.setCommunicationNormal(true);
        variables.setPowerStatusNormal(true);

        // 启动系统自检
        initializeSystemComponents();
    }

    /**
     * 执行进入全红过渡状态的动作
     */
    private void executeEnterTransitionAction(SystemStateV2 previousState) {
        logger.info("执行进入全红过渡动作 (来自: {})", previousState.getChineseName());

        // 设置过渡开始时间
        variables.setTransitionStartTime(LocalDateTime.now());
        variables.setClearDetectionActive(true);

        // 初始化路段清空状态
        initializeSegmentClearanceStatus();

        // 设置所有信号灯为红灯
        setAllTrafficLightsToRed();

        // 启动路段清空检测
        startSegmentClearanceDetection();
    }

    /**
     * 执行进入感应模式的动作
     */
    private void executeEnterInductiveAction() {
        logger.info("执行进入感应控制模式动作");

        // 停止清空检测
        variables.setClearDetectionActive(false);

        // 启动感应控制算法
        startInductiveControlAlgorithm();

        // 重置稳定运行时间
        variables.setStableOperationTime(0);

        // 更新健康度评分
        variables.updateHealthScore(10);
    }

    /**
     * 执行进入降级模式的动作
     */
    private void executeEnterDegradedAction(SystemEventV2 triggerEvent) {
        logger.warn("执行进入降级模式动作 (触发: {})", triggerEvent.getChineseName());

        // 记录故障信息
        variables.setLastFaultTime(LocalDateTime.now());
        variables.setFaultSource(triggerEvent.getCode());

        // 启动降级控制模式
        startDegradedControlMode();

        // 发送故障报警
        sendFaultAlert(triggerEvent);

        // 降低健康度评分
        variables.updateHealthScore(-20);
    }

    /**
     * 执行进入紧急模式的动作
     */
    private void executeEnterEmergencyAction(SystemEventV2 triggerEvent) {
        logger.error("执行进入紧急模式动作 (触发: {})", triggerEvent.getChineseName());

        // 设置紧急级别
        variables.setEmergencyLevel(assessEmergencyLevel(triggerEvent));

        // 启动紧急信号模式
        startEmergencySignalMode();

        // 立即报警
        sendCriticalAlert(triggerEvent);

        // 大幅降低健康度评分
        variables.updateHealthScore(-40);
    }

    /**
     * 通用的进入状态动作分发
     */
    private void executeEnterStateAction(SystemStateV2 newState, SystemStateV2 previousState, SystemEventV2 triggerEvent) {
        switch (newState) {
            case SYSTEM_INIT -> executeEnterInitAction();
            case ALL_RED_TRANSITION -> executeEnterTransitionAction(previousState);
            case INDUCTIVE_MODE -> executeEnterInductiveAction();
            case DEGRADED_MODE -> executeEnterDegradedAction(triggerEvent);
            case EMERGENCY_MODE -> executeEnterEmergencyAction(triggerEvent);
            case MAINTENANCE_MODE -> executeEnterMaintenanceAction();
        }
    }

    /**
     * 执行退出状态动作
     */
    private void executeExitStateAction(SystemStateV2 currentState) {
        switch (currentState) {
            case SYSTEM_INIT -> logger.debug("退出系统初始化状态");
            case ALL_RED_TRANSITION -> {
                logger.debug("退出全红过渡状态");
                variables.setClearDetectionActive(false);
            }
            case INDUCTIVE_MODE -> {
                logger.debug("退出感应控制模式");
                stopInductiveControlAlgorithm();
            }
            case DEGRADED_MODE -> logger.debug("退出降级模式");
            case EMERGENCY_MODE -> logger.debug("退出紧急模式");
            case MAINTENANCE_MODE -> logger.debug("退出维护模式");
        }
    }

    // ==================== 辅助方法 ====================

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
     * 验证配置一致性
     */
    private boolean verifyConfigurationConsistency() {
        logger.debug("验证配置一致性");
        return true; // 简化实现
    }

    /**
     * 初始化系统组件
     */
    private void initializeSystemComponents() {
        logger.debug("初始化系统组件");
        // 实现组件初始化逻辑
    }

    /**
     * 初始化路段清空状态
     */
    private void initializeSegmentClearanceStatus() {
        logger.debug("初始化路段清空状态");
        // 从配置获取路段数量
        int segmentCount = SystemConstants.TOTAL_SEGMENT_COUNT;
        variables.setSegmentCount(segmentCount);

        // 初始化各路段状态为未清空
        for (int i = 0; i < segmentCount; i++) {
            variables.updateSegmentClearStatus("segment_" + i, ClearanceDecision.NOT_CLEARED);
        }
    }

    /**
     * 设置所有信号灯为红灯
     */
    private void setAllTrafficLightsToRed() {
        logger.info("设置所有信号灯为红灯");
        // 实现信号灯控制逻辑
        crossInfoManager.controlAllCrossesToAllRed(3);
    }

    /**
     * 启动路段清空检测
     */
    private void startSegmentClearanceDetection() {
        logger.info("启动路段清空检测");
        // 实现清空检测逻辑
    }

    /**
     * 启动感应控制算法
     */
    private void startInductiveControlAlgorithm() {
        logger.info("启动感应控制算法");
        // 实现感应控制逻辑
    }

    /**
     * 停止感应控制算法
     */
    private void stopInductiveControlAlgorithm() {
        logger.info("停止感应控制算法");
    }

    /**
     * 启动降级控制模式
     */
    private void startDegradedControlMode() {
        logger.info("启动降级控制模式");
        // 实现降级控制逻辑
    }

    /**
     * 启动紧急信号模式
     */
    private void startEmergencySignalMode() {
        logger.error("启动紧急信号模式");
        // 实现紧急信号逻辑
    }

    /**
     * 执行进入维护模式动作
     */
    private void executeEnterMaintenanceAction() {
        logger.info("执行进入维护模式动作");
        // 实现维护模式逻辑
    }

    /**
     * 发送故障报警
     */
    private void sendFaultAlert(SystemEventV2 event) {
        logger.warn("发送故障报警: {}", event.getDescription());
        // 实现报警逻辑
    }

    /**
     * 发送严重报警
     */
    private void sendCriticalAlert(SystemEventV2 event) {
        logger.error("发送严重报警: {}", event.getDescription());
        // 实现严重报警逻辑
    }

    /**
     * 评估紧急级别
     */
    private int assessEmergencyLevel(SystemEventV2 event) {
        return switch (event) {
            case CRITICAL_FAULT -> 4;
            case POWER_FAILURE -> 5;
            case COMMUNICATION_LOSS -> 3;
            default -> 2;
        };
    }

    // ==================== 公共接口方法 ====================

    /**
     * 获取当前状态
     */
    public SystemStateV2 getCurrentState() {
        return currentState.get();
    }

    /**
     * 获取系统变量
     */
    public SystemVariables getVariables() {
        return variables;
    }

    /**
     * 获取系统状态摘要
     */
    public String getSystemStatusSummary() {
        return String.format("当前状态: %s, %s",
                currentState.get().getChineseName(),
                variables.getStatusSummary());
    }

    /**
     * 更新路段清空状态（外部调用）
     */
    public void updateSegmentClearanceStatus(String segmentId, ClearanceDecision decision) {
        variables.updateSegmentClearStatus(segmentId, decision);

        // 检查是否所有路段都已清空
        if (variables.isSegmentsCleared() && currentState.get() == SystemStateV2.ALL_RED_TRANSITION) {
            processEvent(SystemEventV2.ALL_SEGMENTS_CLEARED);
        }
    }

    /**
     * 手动触发事件（用于测试和调试）
     */
    public boolean triggerEvent(SystemEventV2 event) {
        logger.info("手动触发事件: {}", event.getChineseName());
        return processEvent(event);
    }
}