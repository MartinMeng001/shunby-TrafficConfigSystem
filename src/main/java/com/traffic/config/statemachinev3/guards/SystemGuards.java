package com.traffic.config.statemachinev3.guards;

import com.traffic.config.statemachinev3.enums.system.SystemStateV3;
import com.traffic.config.statemachinev3.enums.system.SystemEventV3;
import com.traffic.config.statemachinev3.variables.SystemVariables;
import com.traffic.config.statemachinev3.constants.SystemConstants;

import java.time.LocalDateTime;

/**
 * 顶层系统状态机守护条件函数
 * 对应数学模型中的守护条件函数 G_sys: Q_sys × Σ_sys × V_sys* → Boolean
 *
 * @author System
 * @version 3.0.0
 */
public class SystemGuards {

    // ==================== 初始化和过渡相关守护条件 ====================

    /**
     * 系统初始化完成条件
     * G_init_complete(SYSTEM_INIT, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否满足初始化完成条件
     */
    public static boolean checkInitializationComplete(SystemStateV3 currentState,
                                                      SystemEventV3 event,
                                                      SystemVariables variables) {
        // 只在系统初始化状态下检查
        if (currentState != SystemStateV3.SYSTEM_INIT) {
            System.out.println("current state is " + currentState.getChineseName()+", not SYSTEM_INIT");
            return false;
        }

        // 检查初始化时间是否足够
        boolean timeRequirementMet = variables.getCurrentStateDurationSeconds() >= SystemConstants.SYSTEM_INIT_DELAY;

        // 检查系统健康度是否达标, 健康度暂时只作为一个参考指标，不作为动作条件
        // boolean healthRequirementMet = variables.getSystemHealthScore() >= SystemConstants.NORMAL_HEALTH_THRESHOLD;

        // 检查通信和电源状态
        boolean systemStatusNormal = variables.isCommunicationNormal() && variables.isPowerStatusNormal();

        // 检查配置加载是否成功（简化实现）
        boolean configLoaded = variables.isConfigurationLoaded();
        //return timeRequirementMet && healthRequirementMet && systemStatusNormal && configLoaded;
        return timeRequirementMet && systemStatusNormal && configLoaded;
    }

    /**
     * 过渡完成条件
     * G_transition_complete(ALL_RED_TRANSITION, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否满足过渡完成条件
     */
    public static boolean checkTransitionComplete(SystemStateV3 currentState,
                                                  SystemEventV3 event,
                                                  SystemVariables variables) {
        // 只在全红过渡状态下检查
        if (currentState != SystemStateV3.ALL_RED_TRANSITION) {
            return false;
        }

        // 检查是否处于全红状态
        boolean allSignalRed = variables.isAllSignalRed();

        // 检查过渡时间是否足够
        boolean timeRequirementMet = variables.getTransitionDurationSeconds() >= SystemConstants.TRANSITION_TIME;

        // 检查所有路段是否清空完成
        boolean segmentsClearanceMet = variables.isSegmentsAllReady();

        // 检查清空超时次数未超限
        boolean timeoutCountAcceptable = variables.getClearTimeoutCount() < SystemConstants.MAX_CLEAR_TIMEOUT_COUNT;

        // 检查系统健康度
        boolean systemHealthy = !variables.isCriticalFault();

        return allSignalRed && timeRequirementMet && segmentsClearanceMet && timeoutCountAcceptable && systemHealthy;
    }

    /**
     * 过渡超时条件
     * G_transition_timeout(ALL_RED_TRANSITION, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否满足过渡超时条件
     */
    public static boolean checkTransitionTimeout(SystemStateV3 currentState,
                                                 SystemEventV3 event,
                                                 SystemVariables variables) {
        // 只在全红过渡状态下检查
        if (currentState != SystemStateV3.ALL_RED_TRANSITION) {
            return false;
        }

        // 检查是否超过最大过渡时间
        boolean maxTimeExceeded = variables.getTransitionDurationSeconds() > SystemConstants.getTransitionTimeoutSeconds();

        // 检查清空超时次数是否超限
        boolean clearTimeoutExceeded = variables.getClearTimeoutCount() >= SystemConstants.MAX_CLEAR_TIMEOUT_COUNT;

        return maxTimeExceeded || clearTimeoutExceeded;
    }

    // ==================== 故障检测相关守护条件 ====================

    /**
     * 路段故障检测条件
     * G_segment_fault(INDUCTIVE_MODE, segment_fault_detected, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否检测到路段故障
     */
    public static boolean checkSegmentFaultDetected(SystemStateV3 currentState,
                                                    SystemEventV3 event,
                                                    SystemVariables variables) {
        // 检查是否有路段报告故障
        boolean hasSegmentFault = variables.getSegmentClearanceStates().values().stream()
                .anyMatch(state -> state.isFaultDetected());

        // 检查错误计数是否超限
        boolean errorCountExceeded = variables.getErrorCountWindow() >= SystemConstants.MAX_MISMATCH_ERRORS ||
                variables.getIdLogicErrorCount() >= SystemConstants.MAX_ID_ERRORS;

        // 检查连续故障次数
        boolean consecutiveFaultsExceeded = variables.getConsecutiveFaults() >= 2; // 路段故障阈值较低

        return hasSegmentFault || errorCountExceeded || consecutiveFaultsExceeded;
    }

    /**
     * 系统级故障检测条件
     * G_system_fault(q, system_fault_detected, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否检测到系统级故障
     */
    public static boolean checkSystemFaultDetected(SystemStateV3 currentState,
                                                   SystemEventV3 event,
                                                   SystemVariables variables) {
        // 检查系统健康度
        boolean healthDegraded = variables.getSystemHealthScore() < SystemConstants.NORMAL_HEALTH_THRESHOLD;

        // 检查连续故障次数
        boolean consecutiveFaultsHigh = variables.getConsecutiveFaults() >= 2;

        // 检查通信状态
        boolean communicationDegraded = variables.getCommunicationStatus() == SystemVariables.CommunicationStatus.DEGRADED;

        // 检查电源状态
        boolean powerProblems = variables.getPowerStatus() == SystemVariables.PowerStatus.BACKUP;

        // 检查性能降级
        boolean performanceDegraded = variables.getPerformanceDegradation() > SystemConstants.SYSTEM_DEGRADATION_THRESHOLD;

        return healthDegraded || consecutiveFaultsHigh || communicationDegraded ||
                powerProblems || performanceDegraded;
    }

    /**
     * 严重故障检测条件
     * G_critical_fault(q, critical_fault, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否检测到严重故障
     */
    public static boolean checkCriticalFaultDetected(SystemStateV3 currentState,
                                                     SystemEventV3 event,
                                                     SystemVariables variables) {
        // 检查连续故障次数是否达到关键阈值
        boolean criticalConsecutiveFaults = variables.getConsecutiveFaults() >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT;

        // 检查系统健康度是否严重下降
        boolean criticalHealthScore = variables.getSystemHealthScore() < SystemConstants.CRITICAL_HEALTH_THRESHOLD;

        // 检查通信是否完全失败
        boolean communicationFailed = variables.getCommunicationStatus() == SystemVariables.CommunicationStatus.FAILED;

        // 检查电源是否处于危机状态
        boolean powerCritical = variables.getPowerStatus() == SystemVariables.PowerStatus.CRITICAL;

        // 检查紧急级别
        boolean emergencyLevelCritical = variables.getEmergencyLevel() == SystemVariables.EmergencyLevel.CRITICAL;

        return criticalConsecutiveFaults || criticalHealthScore || communicationFailed ||
                powerCritical || emergencyLevelCritical;
    }

    // ==================== 恢复条件相关守护条件 ====================

    /**
     * 条件恢复检测
     * G_conditions_restored(DEGRADED_MODE, conditions_restored, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否条件已恢复
     */
    public static boolean checkConditionsRestored(SystemStateV3 currentState,
                                                  SystemEventV3 event,
                                                  SystemVariables variables) {
        // 只在降级模式下检查
        if (currentState != SystemStateV3.DEGRADED_MODE) {
            return false;
        }

        // 检查距离最后故障时间是否足够
        boolean stableTimeRequirementMet = false;
        if (variables.getLastFaultTime() != null) {
            long timeSinceLastFault = java.time.Duration.between(variables.getLastFaultTime(), LocalDateTime.now()).getSeconds();
            stableTimeRequirementMet = timeSinceLastFault >= SystemConstants.STABLE_OPERATION_TIME;
        }

        // 检查错误率是否显著下降
        boolean errorRateImproved = variables.getErrorCountWindow() <
                (SystemConstants.MAX_MISMATCH_ERRORS * SystemConstants.ERROR_RESET_THRESHOLD);

        // 检查系统健康度是否回升
        boolean healthImproved = variables.getSystemHealthScore() > SystemConstants.RECOVERY_HEALTH_THRESHOLD;

        // 检查通信状态
        boolean communicationNormal = variables.getCommunicationStatus() == SystemVariables.CommunicationStatus.NORMAL;

        // 检查电源状态
        boolean powerNormal = variables.getPowerStatus() == SystemVariables.PowerStatus.NORMAL;

        return stableTimeRequirementMet && errorRateImproved && healthImproved &&
                communicationNormal && powerNormal;
    }

    /**
     * 恢复验证条件
     * G_recovery_verified(ALL_RED_TRANSITION, recovery_verified, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否恢复验证通过
     */
    public static boolean checkRecoveryVerified(SystemStateV3 currentState,
                                                SystemEventV3 event,
                                                SystemVariables variables) {
        // 检查系统自检是否通过（简化实现）
        boolean systemSelfCheckPassed = variables.getSystemHealthScore() >= SystemConstants.RECOVERY_HEALTH_THRESHOLD;

        // 检查配置一致性（简化实现）
        boolean configConsistencyVerified = variables.getSegmentCount() == SystemConstants.TOTAL_SEGMENT_COUNT;

        // 检查所有路段状态机是否正常
        boolean allSegmentsNormal = variables.getSegmentClearanceStates().values().stream()
                .noneMatch(state -> state.isFaultDetected());

        // 检查通信链路稳定
        boolean communicationStable = variables.isCommunicationNormal();

        // 检查恢复尝试次数未超限
        boolean recoveryAttemptsAcceptable = variables.getRecoveryAttempts() < SystemConstants.MAX_RECOVERY_ATTEMPTS;

        return systemSelfCheckPassed && configConsistencyVerified && allSegmentsNormal &&
                communicationStable && recoveryAttemptsAcceptable;
    }

    // ==================== 控制事件相关守护条件 ====================

    /**
     * 手动降级条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否允许手动降级
     */
    public static boolean checkManualDegradeAllowed(SystemStateV3 currentState,
                                                    SystemEventV3 event,
                                                    SystemVariables variables) {
        // 只有在正常运行状态下才允许手动降级
        return currentState == SystemStateV3.INDUCTIVE_MODE;
    }

    /**
     * 维护请求条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否允许维护请求
     */
    public static boolean checkMaintenanceRequestAllowed(SystemStateV3 currentState,
                                                         SystemEventV3 event,
                                                         SystemVariables variables) {
        // 在感应模式或降级模式下允许维护请求
        boolean allowedState = currentState == SystemStateV3.INDUCTIVE_MODE ||
                currentState == SystemStateV3.DEGRADED_MODE;

        // 检查系统不处于严重故障状态
        boolean notCriticalFault = !variables.isCriticalFault();

        return allowedState && notCriticalFault;
    }

    /**
     * 维护完成条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否维护完成
     */
    public static boolean checkMaintenanceComplete(SystemStateV3 currentState,
                                                   SystemEventV3 event,
                                                   SystemVariables variables) {
        // 只在维护模式下检查
        if (currentState != SystemStateV3.MAINTENANCE_MODE) {
            return false;
        }

        // 检查维护类型是否允许自动完成
        boolean autoCompletionAllowed = variables.getMaintenanceType() == SystemVariables.MaintenanceType.ROUTINE;

        // 检查系统健康度是否恢复
        boolean healthRestored = variables.getSystemHealthScore() >= SystemConstants.NORMAL_HEALTH_THRESHOLD;

        return autoCompletionAllowed && healthRestored;
    }

    // ==================== 外部事件相关守护条件 ====================

    /**
     * 电源故障条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否检测到电源故障
     */
    public static boolean checkPowerFailure(SystemStateV3 currentState,
                                            SystemEventV3 event,
                                            SystemVariables variables) {
        return variables.getPowerStatus() != SystemVariables.PowerStatus.NORMAL;
    }

    /**
     * 通信中断条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否检测到通信中断
     */
    public static boolean checkCommunicationLoss(SystemStateV3 currentState,
                                                 SystemEventV3 event,
                                                 SystemVariables variables) {
        return variables.getCommunicationStatus() != SystemVariables.CommunicationStatus.NORMAL;
    }

    // ==================== 清空相关守护条件 ====================

    /**
     * 所有路段清空完成条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否所有路段清空完成
     */
    public static boolean checkAllSegmentsCleared(SystemStateV3 currentState,
                                                  SystemEventV3 event,
                                                  SystemVariables variables) {
        return variables.isSegmentsAllReady();
    }

    /**
     * 清空超时条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否清空超时
     */
    public static boolean checkClearanceTimeout(SystemStateV3 currentState,
                                                SystemEventV3 event,
                                                SystemVariables variables) {
        // 只在全红过渡状态下检查
        if (currentState != SystemStateV3.ALL_RED_TRANSITION) {
            return false;
        }

        // 检查过渡时间是否超过限制但路段未清空
        boolean transitionTimeExceeded = variables.getTransitionDurationSeconds() >= SystemConstants.TRANSITION_TIME;
        boolean segmentsNotReady = !variables.isSegmentsAllReady();
        boolean withinTimeoutWindow = variables.getTransitionDurationSeconds() <= SystemConstants.getTransitionTimeoutSeconds();

        return transitionTimeExceeded && segmentsNotReady && withinTimeoutWindow;
    }

    // ==================== 通用守护条件方法 ====================

    /**
     * 检查事件是否为强制转换事件
     * @param event 事件
     * @return 是否为强制转换事件
     */
    public static boolean isForceTransitionEvent(SystemEventV3 event) {
        return event.isForceTransition();
    }

    /**
     * 检查状态转换是否被允许
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否允许状态转换
     */
    public static boolean isStateTransitionAllowed(SystemStateV3 currentState,
                                                   SystemStateV3 targetState,
                                                   SystemEventV3 event,
                                                   SystemVariables variables) {
        // 检查基本转换规则
        if (!currentState.canTransitionTo(targetState)) {
            return false;
        }

        // 强制转换事件总是被允许
        if (event.isForceTransition()) {
            return true;
        }

        // 检查特定的守护条件
        return checkSpecificGuardCondition(currentState, targetState, event, variables);
    }

    /**
     * 检查特定的守护条件
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 系统变量
     * @return 是否满足特定守护条件
     */
    private static boolean checkSpecificGuardCondition(SystemStateV3 currentState,
                                                       SystemStateV3 targetState,
                                                       SystemEventV3 event,
                                                       SystemVariables variables) {
        // 根据当前状态和目标状态检查相应的守护条件
        return switch (currentState) {
            case SYSTEM_INIT -> {
                if (targetState == SystemStateV3.ALL_RED_TRANSITION) {
                    yield checkInitializationComplete(currentState, event, variables);
                } else if (targetState == SystemStateV3.EMERGENCY_MODE) {
                    yield checkCriticalFaultDetected(currentState, event, variables);
                }
                yield false;
            }
            case ALL_RED_TRANSITION -> {
                if (targetState == SystemStateV3.INDUCTIVE_MODE) {
                    yield checkTransitionComplete(currentState, event, variables);
                } else if (targetState == SystemStateV3.DEGRADED_MODE) {
                    yield checkTransitionTimeout(currentState, event, variables);
                } else if (targetState == SystemStateV3.EMERGENCY_MODE) {
                    yield checkCriticalFaultDetected(currentState, event, variables);
                }
                yield false;
            }
            case INDUCTIVE_MODE -> {
                if (targetState == SystemStateV3.ALL_RED_TRANSITION) {
                    yield checkSegmentFaultDetected(currentState, event, variables) ||
                            checkSystemFaultDetected(currentState, event, variables);
                } else if (targetState == SystemStateV3.EMERGENCY_MODE) {
                    yield checkCriticalFaultDetected(currentState, event, variables);
                } else if (targetState == SystemStateV3.MAINTENANCE_MODE) {
                    yield checkMaintenanceRequestAllowed(currentState, event, variables);
                }
                yield false;
            }
            case DEGRADED_MODE -> {
                if (targetState == SystemStateV3.ALL_RED_TRANSITION) {
                    yield checkConditionsRestored(currentState, event, variables);
                } else if (targetState == SystemStateV3.MAINTENANCE_MODE) {
                    yield checkMaintenanceRequestAllowed(currentState, event, variables);
                } else if (targetState == SystemStateV3.EMERGENCY_MODE) {
                    yield checkCriticalFaultDetected(currentState, event, variables);
                }
                yield false;
            }
            case MAINTENANCE_MODE -> {
                if (targetState == SystemStateV3.SYSTEM_INIT) {
                    yield checkMaintenanceComplete(currentState, event, variables);
                } else if (targetState == SystemStateV3.EMERGENCY_MODE) {
                    yield checkCriticalFaultDetected(currentState, event, variables);
                }
                yield false;
            }
            case EMERGENCY_MODE -> {
                if (targetState == SystemStateV3.SYSTEM_INIT) {
                    yield true; // 紧急模式总是可以重置到初始状态
                } else if (targetState == SystemStateV3.ALL_RED_TRANSITION) {
                    yield checkRecoveryVerified(currentState, event, variables);
                }
                yield false;
            }
        };
    }

    /**
     * 检查系统是否处于安全状态进行转换
     * @param variables 系统变量
     * @return 是否安全
     */
    public static boolean isSystemSafeForTransition(SystemVariables variables) {
        // 检查健康度
        boolean healthAcceptable = variables.getSystemHealthScore() >= SystemConstants.CRITICAL_HEALTH_THRESHOLD;

        // 检查错误率
        boolean errorRateAcceptable = variables.getConsecutiveFaults() < SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT;

        // 检查基础设施状态
        boolean infrastructureNormal = variables.isCommunicationNormal() && variables.isPowerStatusNormal();

        return healthAcceptable && errorRateAcceptable && infrastructureNormal;
    }
}
