package com.traffic.config.statemachinev3.guards;

import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import com.traffic.config.statemachinev3.constants.SegmentConstants;

/**
 * 路段状态机守护条件函数
 * 对应数学模型中的守护条件函数 G: Q × Σ × V* → Boolean
 *
 * @author System
 * @version 3.0.0
 */
public class SegmentGuards {

    // ==================== 状态转换守护条件 ====================

    /**
     * 从绿灯状态转换到全红清空的守护条件
     * G(q_green, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否满足转换条件
     */
    public static boolean checkGreenToRedTransition(SegmentState currentState,
                                                    SegmentEvent event,
                                                    SegmentVariables variables) {
        // 检查当前是否为绿灯状态
        if (!currentState.isGreenState()) {
            return false;
        }

        // 检查最小绿灯时间是否满足
        boolean minGreenTimeReached = variables.isMinGreenTimeReached();

        // 检查是否有相反方向的通行请求
        boolean hasOppositeRequest = switch (currentState) {
            case UPSTREAM_GREEN -> variables.isDownstreamRequest();
            case DOWNSTREAM_GREEN -> variables.isUpstreamRequest();
            default -> false;
        };

        // 检查清空条件或强制切换条件
        boolean shouldSwitch = checkSwitchConditions(currentState, variables);

        return minGreenTimeReached && hasOppositeRequest && shouldSwitch;
    }

    /**
     * 检查切换条件
     * @param currentState 当前状态
     * @param variables 路段变量
     * @return 是否应该切换
     */
    private static boolean checkSwitchConditions(SegmentState currentState, SegmentVariables variables) {
        // 获取当前方向的清空状态
        ClearanceDecision currentDirectionClearance = switch (currentState) {
            case UPSTREAM_GREEN -> variables.getUpstreamClearanceDecision();
            case DOWNSTREAM_GREEN -> variables.getDownstreamClearanceDecision();
            default -> ClearanceDecision.WAIT;
        };

        // 获取相反方向的清空状态
        ClearanceDecision oppositeDirectionClearance = switch (currentState) {
            case UPSTREAM_GREEN -> variables.getDownstreamClearanceDecision();
            case DOWNSTREAM_GREEN -> variables.getUpstreamClearanceDecision();
            default -> ClearanceDecision.WAIT;
        };

        // 条件1：当前方向可以安全清空，相反方向准备好接收
        boolean safeTransitionCondition = currentDirectionClearance.isSafeForTransition() &&
                (oppositeDirectionClearance == ClearanceDecision.SAFE ||
                        oppositeDirectionClearance == ClearanceDecision.WARNING);

        // 条件2：绿灯时间达到最大值
        boolean maxGreenTimeReached = variables.isGreenTimeout();

        // 条件3：容量接近饱和
        boolean capacityNearFull = switch (currentState) {
            case UPSTREAM_GREEN -> variables.isCapacityReached(SegmentVariables.Direction.UPSTREAM);
            case DOWNSTREAM_GREEN -> variables.isCapacityReached(SegmentVariables.Direction.DOWNSTREAM);
            default -> false;
        };

        // 条件4：优先级评估结果
        boolean priorityFavorsSwitch = checkPriorityConditions(currentState, variables);

        return safeTransitionCondition || maxGreenTimeReached || capacityNearFull || priorityFavorsSwitch;
    }

    /**
     * 检查优先级条件
     * @param currentState 当前状态
     * @param variables 路段变量
     * @return 优先级是否支持切换
     */
    private static boolean checkPriorityConditions(SegmentState currentState, SegmentVariables variables) {
        SegmentVariables.Direction priorityDirection = variables.determinePriorityDirection();

        return switch (currentState) {
            case UPSTREAM_GREEN -> priorityDirection == SegmentVariables.Direction.DOWNSTREAM;
            case DOWNSTREAM_GREEN -> priorityDirection == SegmentVariables.Direction.UPSTREAM;
            default -> false;
        };
    }

    /**
     * 从全红清空转换到绿灯状态的守护条件
     * G(ALL_RED_CLEAR, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否满足转换条件
     */
    public static boolean checkRedToGreenTransition(SegmentState currentState,
                                                    SegmentState targetState,
                                                    SegmentEvent event,
                                                    SegmentVariables variables) {
        // 检查当前是否为全红状态
        if (currentState != SegmentState.ALL_RED_CLEAR) {
            return false;
        }

        // 检查目标方向是否有通行请求, 这个条件不应当作为限制条件，但可以作为触发条件
//        boolean hasTargetRequest = switch (targetState) {
//            case UPSTREAM_GREEN -> variables.isUpstreamRequest();
//            case DOWNSTREAM_GREEN -> variables.isDownstreamRequest();
//            default -> false;
//        };

        // 检查清空条件是否满足
        boolean clearanceConditionMet = checkClearanceConditions(variables);

        // 检查优先级判断，优先级判断，这里不应当再进行判断
//        SegmentVariables.Direction priorityDirection = variables.determinePriorityDirection();
//        boolean priorityMatches = switch (targetState) {
//            case UPSTREAM_GREEN -> priorityDirection == SegmentVariables.Direction.UPSTREAM;
//            case DOWNSTREAM_GREEN -> priorityDirection == SegmentVariables.Direction.DOWNSTREAM;
//            default -> false;
//        };
        return  clearanceConditionMet;
        //return hasTargetRequest && clearanceConditionMet && priorityMatches;
    }

    /**
     * 检查清空条件是否满足
     * @param variables 路段变量
     * @return 清空条件是否满足
     */
    private static boolean checkClearanceConditions(SegmentVariables variables) {
        ClearanceDecision overallDecision = variables.getOverallClearanceDecision();

        return switch (overallDecision) {
            case SAFE, WARNING -> true;
            case CONSERVATIVE -> variables.isConservativeTimerExpired();
            case WAIT -> variables.isMaxTimerExpired();
        };
    }

    // ==================== 车辆事件守护条件 ====================

    /**
     * 车辆进入守护条件
     * G(q, vehicle_enter_direction, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 车辆ID
     * @return 是否允许车辆进入
     */
    public static boolean checkVehicleEntry(SegmentState currentState,
                                            SegmentEvent event,
                                            SegmentVariables variables,
                                            String vehicleId) {
        // 获取车辆进入方向
        SegmentVariables.Direction direction = getVehicleDirection(event);
        if (direction == SegmentVariables.Direction.NONE) {
            return false;
        }

        // 检查容量限制
        boolean capacityAvailable = !variables.isCapacityReached(direction);

        // 检查车辆ID唯一性
        boolean uniqueVehicleId = !variables.isDuplicateVehicleId(vehicleId);

        // 检查状态兼容性（是否允许该方向进入）
        boolean stateCompatible = variables.canAcceptVehicle(direction, currentState);

        // 检查传感器状态
        boolean sensorsNormal = variables.areCriticalSensorsNormal();

        return capacityAvailable && uniqueVehicleId && stateCompatible && sensorsNormal;
    }

    /**
     * 车辆离开守护条件
     * G(q, vehicle_exit_direction, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 车辆ID
     * @return 是否车辆离开有效
     */
    public static boolean checkVehicleExit(SegmentState currentState,
                                           SegmentEvent event,
                                           SegmentVariables variables,
                                           String vehicleId) {
        // 获取车辆离开方向
        SegmentVariables.Direction direction = getVehicleDirection(event);
        if (direction == SegmentVariables.Direction.NONE) {
            return false;
        }

        // 检查车辆是否在对应的方向集合中
        boolean vehicleExists = switch (direction) {
            case UPSTREAM -> variables.getUpstreamVehicleIds().contains(vehicleId);
            case DOWNSTREAM -> variables.getDownstreamVehicleIds().contains(vehicleId);
            case NONE -> false;
        };

        // 检查传感器状态
        boolean sensorsNormal = variables.areCriticalSensorsNormal();

        return vehicleExists && sensorsNormal;
    }

    /**
     * 从事件获取车辆方向
     * @param event 车辆事件
     * @return 车辆方向
     */
    private static SegmentVariables.Direction getVehicleDirection(SegmentEvent event) {
        return switch (event) {
            case VEHICLE_ENTER_UPSTREAM, VEHICLE_EXIT_UPSTREAM -> SegmentVariables.Direction.UPSTREAM;
            case VEHICLE_ENTER_DOWNSTREAM, VEHICLE_EXIT_DOWNSTREAM -> SegmentVariables.Direction.DOWNSTREAM;
            default -> SegmentVariables.Direction.NONE;
        };
    }

    // ==================== 通行请求事件守护条件 ====================

    /**
     * 通行请求生成守护条件
     * G(q, request_generated, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否应该生成通行请求
     */
    public static boolean checkRequestGeneration(SegmentState currentState,
                                                 SegmentEvent event,
                                                 SegmentVariables variables) {
        SegmentVariables.Direction direction = getRequestDirection(event);
        if (direction == SegmentVariables.Direction.NONE) {
            return false;
        }

        // 检查该方向是否有车辆等待
        boolean hasWaitingVehicles = switch (direction) {
            case UPSTREAM -> !variables.getUpstreamVehicleIds().isEmpty();
            case DOWNSTREAM -> !variables.getDownstreamVehicleIds().isEmpty();
            case NONE -> false;
        };

        // 检查当前状态是否不服务该方向
        boolean notCurrentlyServed = switch (direction) {
            case UPSTREAM -> currentState != SegmentState.UPSTREAM_GREEN;
            case DOWNSTREAM -> currentState != SegmentState.DOWNSTREAM_GREEN;
            case NONE -> false;
        };

        // 检查该方向是否还没有请求
        boolean noExistingRequest = switch (direction) {
            case UPSTREAM -> !variables.isUpstreamRequest();
            case DOWNSTREAM -> !variables.isDownstreamRequest();
            case NONE -> false;
        };

        // 检查是否达到请求触发阈值
        boolean shouldGenerateRequest = variables.shouldGenerateRequest(direction);

        return hasWaitingVehicles && notCurrentlyServed && noExistingRequest && shouldGenerateRequest;
    }

    /**
     * 通行请求清除守护条件
     * G(q, request_cleared, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否应该清除通行请求
     */
    public static boolean checkRequestClearance(SegmentState currentState,
                                                SegmentEvent event,
                                                SegmentVariables variables) {
        SegmentVariables.Direction direction = getRequestDirection(event);
        if (direction == SegmentVariables.Direction.NONE) {
            return false;
        }

        // 检查该方向是否有现有请求
        boolean hasExistingRequest = switch (direction) {
            case UPSTREAM -> variables.isUpstreamRequest();
            case DOWNSTREAM -> variables.isDownstreamRequest();
            case NONE -> false;
        };

        // 检查该方向是否已经没有车辆
        boolean noVehicles = switch (direction) {
            case UPSTREAM -> variables.getUpstreamVehicleIds().isEmpty();
            case DOWNSTREAM -> variables.getDownstreamVehicleIds().isEmpty();
            case NONE -> false;
        };

        // 检查清空确认完成
        boolean clearanceConfirmed = variables.shouldClearRequest(direction);

        return hasExistingRequest && noVehicles && clearanceConfirmed;
    }

    /**
     * 从请求事件获取方向
     * @param event 请求事件
     * @return 请求方向
     */
    private static SegmentVariables.Direction getRequestDirection(SegmentEvent event) {
        return switch (event) {
            case UPSTREAM_REQUEST_GENERATED, UPSTREAM_REQUEST_CLEARED -> SegmentVariables.Direction.UPSTREAM;
            case DOWNSTREAM_REQUEST_GENERATED, DOWNSTREAM_REQUEST_CLEARED -> SegmentVariables.Direction.DOWNSTREAM;
            default -> SegmentVariables.Direction.NONE;
        };
    }

    // ==================== 超时和故障守护条件 ====================

    /**
     * 绿灯超时条件
     * G(q_green, green_timeout, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否绿灯超时
     */
    public static boolean checkGreenTimeout(SegmentState currentState,
                                            SegmentEvent event,
                                            SegmentVariables variables) {
        return currentState.isGreenState() && variables.isGreenTimeout();
    }

    /**
     * 清空超时条件
     * G(ALL_RED_CLEAR, clear_timeout, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否清空超时
     */
    public static boolean checkClearTimeout(SegmentState currentState,
                                            SegmentEvent event,
                                            SegmentVariables variables) {
        return currentState == SegmentState.ALL_RED_CLEAR &&
                variables.isRedTimeout() &&
                variables.getOverallClearanceDecision() != ClearanceDecision.SAFE;
    }

    /**
     * 传感器故障检测条件
     * G(q, sensor_fault, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否检测到传感器故障
     */
    public static boolean checkSensorFault(SegmentState currentState,
                                           SegmentEvent event,
                                           SegmentVariables variables) {
        // 检查是否有传感器处于故障状态
        boolean hasSensorFailure = variables.getSensorStatus().values().stream()
                .anyMatch(status -> status == SegmentVariables.SensorState.FAILED);

        // 检查传感器响应超时（简化实现）
        boolean sensorTimeout = !variables.areCriticalSensorsNormal();

        return hasSensorFailure || sensorTimeout;
    }

    /**
     * 计数器不匹配检测条件
     * G(q, counter_mismatch_detected, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否检测到计数器不匹配
     */
    public static boolean checkCounterMismatch(SegmentState currentState,
                                               SegmentEvent event,
                                               SegmentVariables variables) {
        // 检查上行计数器不匹配
        boolean upstreamMismatch = (variables.getUpstreamInCounter() != variables.getUpstreamOutCounter()) &&
                variables.getUpstreamVehicleIds().isEmpty();

        // 检查下行计数器不匹配
        boolean downstreamMismatch = (variables.getDownstreamInCounter() != variables.getDownstreamOutCounter()) &&
                variables.getDownstreamVehicleIds().isEmpty();

        return upstreamMismatch || downstreamMismatch;
    }

    /**
     * ID逻辑错误检测条件
     * G(q, id_logic_error_detected, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 相关车辆ID（如果有）
     * @return 是否检测到ID逻辑错误
     */
    public static boolean checkIdLogicError(SegmentState currentState,
                                            SegmentEvent event,
                                            SegmentVariables variables,
                                            String vehicleId) {
        if (vehicleId == null) {
            return false;
        }

        // 检查重复ID错误
        boolean duplicateId = variables.isDuplicateVehicleId(vehicleId);

        // 检查无源注册错误（车辆离开但从未进入）
        boolean unregisteredExit = false;
        if (event.isVehicleExitEvent()) {
            SegmentVariables.Direction direction = getVehicleDirection(event);
            unregisteredExit = switch (direction) {
                case UPSTREAM -> !variables.getUpstreamVehicleIds().contains(vehicleId);
                case DOWNSTREAM -> !variables.getDownstreamVehicleIds().contains(vehicleId);
                case NONE -> false;
            };
        }

        return duplicateId || unregisteredExit;
    }

    // ==================== 控制事件守护条件 ====================

    /**
     * 强制切换条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否允许强制切换
     */
    public static boolean checkForceSwitch(SegmentState currentState,
                                           SegmentEvent event,
                                           SegmentVariables variables) {
        // 强制切换总是被允许，但记录为异常操作
        return true;
    }

    /**
     * 紧急覆盖条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否允许紧急覆盖
     */
    public static boolean checkEmergencyOverride(SegmentState currentState,
                                                 SegmentEvent event,
                                                 SegmentVariables variables) {
        // 紧急覆盖总是被允许
        return true;
    }

    /**
     * 维护模式条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否允许进入维护模式
     */
    public static boolean checkMaintenanceMode(SegmentState currentState,
                                               SegmentEvent event,
                                               SegmentVariables variables) {
        // 检查路段是否处于相对稳定状态
        boolean stableState = variables.getOverallClearanceDecision() == ClearanceDecision.SAFE;

        // 检查是否没有严重故障
        boolean noSevereFault = !variables.shouldReportFault();

        return stableState && noSevereFault;
    }

    // ==================== 保守清空相关守护条件 ====================

    /**
     * 保守清空触发条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否触发保守清空
     */
    public static boolean checkConservativeClearTriggered(SegmentState currentState,
                                                          SegmentEvent event,
                                                          SegmentVariables variables) {
        return variables.getOverallClearanceDecision() == ClearanceDecision.CONSERVATIVE &&
                variables.isConservativeTimerExpired();
    }

    /**
     * 清空完成条件
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否清空完成
     */
    public static boolean checkClearanceComplete(SegmentState currentState,
                                                 SegmentEvent event,
                                                 SegmentVariables variables) {
        return variables.getOverallClearanceDecision() == ClearanceDecision.SAFE;
    }

    // ==================== 通用守护条件方法 ====================

    /**
     * 检查状态转换是否被允许
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否允许状态转换
     */
    public static boolean isStateTransitionAllowed(SegmentState currentState,
                                                   SegmentState targetState,
                                                   SegmentEvent event,
                                                   SegmentVariables variables) {
        // 检查基本转换规则
        if (!currentState.canTransitionTo(targetState)) {
            return false;
        }

        // 强制转换事件总是被允许
        if (event.isForceTransition()) {
            return true;
        }

        // 检查特定的守护条件
        return checkSpecificTransitionGuard(currentState, targetState, event, variables);
    }

    /**
     * 检查特定的状态转换守护条件
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     * @return 是否满足特定守护条件
     */
    private static boolean checkSpecificTransitionGuard(SegmentState currentState,
                                                        SegmentState targetState,
                                                        SegmentEvent event,
                                                        SegmentVariables variables) {
        return switch (currentState) {
            case UPSTREAM_GREEN, DOWNSTREAM_GREEN -> {
                if (targetState == SegmentState.ALL_RED_CLEAR) {
                    yield checkGreenToRedTransition(currentState, event, variables);
                }
                yield false;
            }
            case ALL_RED_CLEAR -> {
                if (targetState == SegmentState.UPSTREAM_GREEN || targetState == SegmentState.DOWNSTREAM_GREEN) {
                    yield checkRedToGreenTransition(currentState, targetState, event, variables);
                }
                yield false;
            }
        };
    }

    /**
     * 检查路段是否处于健康状态
     * @param variables 路段变量
     * @return 是否健康
     */
    public static boolean isSegmentHealthy(SegmentVariables variables) {
        // 检查健康度评分
        boolean healthScoreAcceptable = variables.getSegmentHealthScore() >= 70;

        // 检查错误率
        boolean errorRateAcceptable = variables.getConsecutiveErrors() < SegmentConstants.MAX_CONSECUTIVE_ERRORS;

        // 检查传感器状态
        boolean sensorsNormal = variables.areCriticalSensorsNormal();

        // 检查是否未检测到故障
        boolean noFaultDetected = !variables.isFaultDetected();

        return healthScoreAcceptable && errorRateAcceptable && sensorsNormal && noFaultDetected;
    }

    /**
     * 检查事件是否为高优先级事件
     * @param event 事件
     * @return 是否为高优先级事件
     */
    public static boolean isHighPriorityEvent(SegmentEvent event) {
        return event.isHighPriority();
    }

    /**
     * 检查路段是否可以安全进行状态转换
     * @param variables 路段变量
     * @return 是否安全
     */
    public static boolean isSafeForTransition(SegmentVariables variables) {
        // 检查清空状态
        boolean clearanceAcceptable = variables.getOverallClearanceDecision().isSafeForTransition();

        // 检查健康状态
        boolean segmentHealthy = isSegmentHealthy(variables);

        // 检查系统稳定性
        boolean systemStable = variables.getConsecutiveErrors() == 0;

        return clearanceAcceptable && segmentHealthy && systemStable;
    }
    /**
     * 车辆事件统一守护条件检查
     * 对应数学模型中的车辆事件守护条件 G(q, vehicle_event, v)
     *
     * @param currentState 当前状态
     * @param event 车辆事件
     * @param variables 路段变量
     * @return 是否允许处理该车辆事件
     */
    public static boolean checkVehicleEventAllowed(SegmentState currentState,
                                                   SegmentEvent event,
                                                   SegmentVariables variables) {
        // 检查事件是否为车辆事件
        if (!event.isVehicleEvent()) {
            return false;
        }

        // 检查系统基本状态
        if (!checkBasicSystemConditions(variables)) {
            return false;
        }

        // 检查是否处于故障状态
        if (variables.isFaultDetected()) {
            // 故障状态下需要特殊处理
            return handleFaultStateVehicleEvent(currentState, event, variables);
        }

        // 检查传感器状态
//        if (!variables.areCriticalSensorsNormal()) {
//            // 传感器异常时，只允许基于备用传感器的事件
//            return event.SENSOR_FAULT();
//        }

        // 根据具体车辆事件类型进行详细检查
        return switch (event) {
            case VEHICLE_ENTER_UPSTREAM, VEHICLE_ENTER_DOWNSTREAM ->
                    checkVehicleEntryPreconditions(currentState, event, variables);

            case VEHICLE_EXIT_UPSTREAM, VEHICLE_EXIT_DOWNSTREAM ->
                    checkVehicleExitPreconditions(currentState, event, variables);

            default -> false;
        };
    }

    /**
     * 检查系统基本条件
     * @param variables 路段变量
     * @return 系统是否处于可接受车辆事件的状态
     */
    private static boolean checkBasicSystemConditions(SegmentVariables variables) {
        // 检查健康度
        boolean healthAcceptable = variables.getSegmentHealthScore() >=
                SegmentConstants.CRITICAL_HEALTH_THRESHOLD;

        // 检查连续错误数量
        boolean errorCountAcceptable = variables.getConsecutiveErrors() <
                SegmentConstants.MAX_CONSECUTIVE_ERRORS;

        // 检查是否超出最大容量
        boolean capacityNotExceeded = !variables.isCapacityReached(SegmentVariables.Direction.NONE);

        return healthAcceptable && errorCountAcceptable && capacityNotExceeded;
    }

    /**
     * 处理故障状态下的车辆事件
     * @param currentState 当前状态
     * @param event 车辆事件
     * @param variables 路段变量
     * @return 是否允许处理
     */
    private static boolean handleFaultStateVehicleEvent(SegmentState currentState,
                                                        SegmentEvent event,
                                                        SegmentVariables variables) {
        // 故障状态下的特殊处理逻辑

        // 允许车辆离开（安全考虑）
        if (event.isVehicleExitEvent()) {
            return true;
        }

        // 车辆进入需要更严格的检查
        if (event.isVehicleEntryEvent()) {
            // 只有在清空决策为SAFE时才允许新车辆进入
            return variables.getOverallClearanceDecision() == ClearanceDecision.SAFE;
        }

        return false;
    }

    /**
     * 检查车辆进入的前置条件
     * @param currentState 当前状态
     * @param event 车辆进入事件
     * @param variables 路段变量
     * @return 是否满足进入前置条件
     */
    private static boolean checkVehicleEntryPreconditions(SegmentState currentState,
                                                          SegmentEvent event,
                                                          SegmentVariables variables) {
        // 获取车辆进入方向
        SegmentVariables.Direction direction = getVehicleDirection(event);

        // 检查方向容量
        boolean capacityAvailable = !variables.isCapacityReached(direction);

        // 检查状态兼容性
        boolean stateCompatible = variables.canAcceptVehicle(direction, currentState);

        // 检查是否处于切换过程中
        boolean notInTransition = !variables.isInRedState();

        // 检查清空状态
        boolean clearanceAllowsEntry = checkClearanceAllowsEntry(direction, variables);

        return capacityAvailable && stateCompatible && notInTransition && clearanceAllowsEntry;
    }

    /**
     * 检查车辆离开的前置条件
     * @param currentState 当前状态
     * @param event 车辆离开事件
     * @param variables 路段变量
     * @return 是否满足离开前置条件
     */
    private static boolean checkVehicleExitPreconditions(SegmentState currentState,
                                                         SegmentEvent event,
                                                         SegmentVariables variables) {
        // 车辆离开通常应该被允许（安全考虑）
        // 但仍需要基本的数据一致性检查

        SegmentVariables.Direction direction = getVehicleDirection(event);

        // 检查是否有车辆可以离开
        boolean hasVehiclesToExit = switch (direction) {
            case UPSTREAM -> !variables.getUpstreamVehicleIds().isEmpty();
            case DOWNSTREAM -> !variables.getDownstreamVehicleIds().isEmpty();
            case NONE -> false;
        };

        // 检查传感器可用性
        boolean exitSensorsWorking = variables.areCriticalSensorsNormal();//direction

        return hasVehiclesToExit && exitSensorsWorking;
    }

    /**
     * 检查清空状态是否允许车辆进入
     * @param direction 车辆方向
     * @param variables 路段变量
     * @return 是否允许进入
     */
    private static boolean checkClearanceAllowsEntry(SegmentVariables.Direction direction,
                                                     SegmentVariables variables) {
        ClearanceDecision overallDecision = variables.getOverallClearanceDecision();

        // SAFE状态总是允许进入
        if (overallDecision == ClearanceDecision.SAFE) {
            return true;
        }

        // WARNING状态需要检查具体方向
        if (overallDecision == ClearanceDecision.WARNING) {
            ClearanceDecision directionDecision = switch (direction) {
                case UPSTREAM -> variables.getUpstreamClearanceDecision();
                case DOWNSTREAM -> variables.getDownstreamClearanceDecision();
                case NONE -> ClearanceDecision.WAIT;
            };
            return directionDecision == ClearanceDecision.SAFE;
        }

        // CONSERVATIVE和WAIT状态不允许新车辆进入
        return false;
    }
}
