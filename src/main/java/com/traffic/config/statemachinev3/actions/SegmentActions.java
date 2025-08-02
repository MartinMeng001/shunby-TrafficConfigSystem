package com.traffic.config.statemachinev3.actions;

package com.traffic.config.statemachinev3.actions;

import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import com.traffic.config.statemachinev3.constants.SegmentConstants;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * 路段状态机动作函数
 * 对应数学模型中的动作函数 A: Q × Σ × V* → V*
 *
 * @author System
 * @version 3.0.0
 */
public class SegmentActions {

    private static final Logger logger = Logger.getLogger(SegmentActions.class.getName());

    // ==================== 状态转换动作 ====================

    /**
     * 进入绿灯状态动作
     * A(ALL_RED_CLEAR, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeEnterGreenState(SegmentState currentState,
                                              SegmentState targetState,
                                              SegmentEvent event,
                                              SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 确定绿灯方向
        SegmentVariables.Direction direction = switch (targetState) {
            case UPSTREAM_GREEN -> SegmentVariables.Direction.UPSTREAM;
            case DOWNSTREAM_GREEN -> SegmentVariables.Direction.DOWNSTREAM;
            default -> SegmentVariables.Direction.NONE;
        };

        // 开始绿灯计时
        variables.startGreenTimer(direction);

        // 重置保守清空计时器
        variables.setConservativeTimerStart(null);

        // 更新服务方向记录
        variables.setLastServedDirection(direction);

        // 重置对应方向等待时间
        resetDirectionWaitingTime(direction, variables);

        // 更新性能统计
        variables.updatePerformanceStatistics();

        // 记录状态切换
        recordStateTransition(currentState, targetState, variables);

        logger.info("路段" + variables.getSegmentId() + "进入" + targetState.getChineseName() + "状态");
    }

    /**
     * 进入全红状态动作
     * A(q_green, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeEnterRedState(SegmentState currentState,
                                            SegmentEvent event,
                                            SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 开始红灯计时
        variables.startRedTimer();

        // 更新清空决策（在进入红灯状态时重新计算）
        updateAllClearanceDecisions(variables);

        // 如果需要启动保守清空计时器
        if (variables.getOverallClearanceDecision() == ClearanceDecision.CONSERVATIVE) {
            variables.startConservativeTimer();
        }

        // 更新服务统计
        updateServiceStatistics(variables);

        // 记录状态切换
        recordStateTransition(currentState, SegmentState.ALL_RED_CLEAR, variables);

        logger.info("路段" + variables.getSegmentId() + "进入全红清空状态");
    }

    // ==================== 车辆事件处理动作 ====================

    /**
     * 车辆进入动作
     * A(q, vehicle_enter_direction, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 车辆ID
     */
    public static void executeVehicleEntry(SegmentState currentState,
                                           SegmentEvent event,
                                           SegmentVariables variables,
                                           String vehicleId) {
        // 确定车辆进入方向
        SegmentVariables.Direction direction = getVehicleDirection(event);

        // 添加车辆到对应方向
        switch (direction) {
            case UPSTREAM -> {
                variables.addUpstreamVehicle(vehicleId);

                // 检查是否需要生成通行请求
                if (variables.shouldGenerateRequest(SegmentVariables.Direction.UPSTREAM)) {
                    variables.generateUpstreamRequest();
                    logger.info("路段" + variables.getSegmentId() + "生成上行通行请求");
                }

                // 更新等待时间
                if (currentState != SegmentState.UPSTREAM_GREEN) {
                    updateUpstreamWaitingTime(variables);
                }
            }
            case DOWNSTREAM -> {
                variables.addDownstreamVehicle(vehicleId);

                // 检查是否需要生成通行请求
                if (variables.shouldGenerateRequest(SegmentVariables.Direction.DOWNSTREAM)) {
                    variables.generateDownstreamRequest();
                    logger.info("路段" + variables.getSegmentId() + "生成下行通行请求");
                }

                // 更新等待时间
                if (currentState != SegmentState.DOWNSTREAM_GREEN) {
                    updateDownstreamWaitingTime(variables);
                }
            }
            case NONE -> {
                logger.warning("路段" + variables.getSegmentId() + "收到无效方向的车辆进入事件");
                return;
            }
        }

        // 验证车辆进入
        validateVehicleEntry(vehicleId, direction, variables);

        // 记录车辆进入事件
        recordVehicleEntryEvent(vehicleId, direction, variables);

        logger.fine("车辆" + vehicleId + "进入路段" + variables.getSegmentId() +
                direction.getDescription() + "方向");
    }

    /**
     * 车辆离开动作
     * A(q, vehicle_exit_direction, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 车辆ID
     */
    public static void executeVehicleExit(SegmentState currentState,
                                          SegmentEvent event,
                                          SegmentVariables variables,
                                          String vehicleId) {
        // 确定车辆离开方向
        SegmentVariables.Direction direction = getVehicleDirection(event);

        // 从对应方向移除车辆
        switch (direction) {
            case UPSTREAM -> {
                variables.removeUpstreamVehicle(vehicleId);

                // 检查是否需要清除通行请求
                if (variables.shouldClearRequest(SegmentVariables.Direction.UPSTREAM)) {
                    variables.clearUpstreamRequest();
                    logger.info("路段" + variables.getSegmentId() + "清除上行通行请求");
                }

                // 更新清空决策
                updateUpstreamClearanceDecision(variables);
            }
            case DOWNSTREAM -> {
                variables.removeDownstreamVehicle(vehicleId);

                // 检查是否需要清除通行请求
                if (variables.shouldClearRequest(SegmentVariables.Direction.DOWNSTREAM)) {
                    variables.clearDownstreamRequest();
                    logger.info("路段" + variables.getSegmentId() + "清除下行通行请求");
                }

                // 更新清空决策
                updateDownstreamClearanceDecision(variables);
            }
            case NONE -> {
                logger.warning("路段" + variables.getSegmentId() + "收到无效方向的车辆离开事件");
                return;
            }
        }

        // 验证车辆离开
        validateVehicleExit(vehicleId, direction, variables);

        // 记录车辆离开事件
        recordVehicleExitEvent(vehicleId, direction, variables);

        logger.fine("车辆" + vehicleId + "离开路段" + variables.getSegmentId() +
                direction.getDescription() + "方向");
    }

    // ==================== 通行请求处理动作 ====================

    /**
     * 通行请求生成动作
     * A(q, request_generated, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeRequestGeneration(SegmentState currentState,
                                                SegmentEvent event,
                                                SegmentVariables variables) {
        SegmentVariables.Direction direction = getRequestDirection(event);

        switch (direction) {
            case UPSTREAM -> {
                variables.generateUpstreamRequest();
                logger.info("路段" + variables.getSegmentId() + "生成上行通行请求");
            }
            case DOWNSTREAM -> {
                variables.generateDownstreamRequest();
                logger.info("路段" + variables.getSegmentId() + "生成下行通行请求");
            }
            case NONE -> {
                logger.warning("路段" + variables.getSegmentId() + "收到无效的请求生成事件");
            }
        }

        // 记录请求生成事件
        recordRequestGenerationEvent(direction, variables);
    }

    /**
     * 通行请求清除动作
     * A(q, request_cleared, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeRequestClearance(SegmentState currentState,
                                               SegmentEvent event,
                                               SegmentVariables variables) {
        SegmentVariables.Direction direction = getRequestDirection(event);

        switch (direction) {
            case UPSTREAM -> {
                variables.clearUpstreamRequest();
                logger.info("路段" + variables.getSegmentId() + "清除上行通行请求");
            }
            case DOWNSTREAM -> {
                variables.clearDownstreamRequest();
                logger.info("路段" + variables.getSegmentId() + "清除下行通行请求");
            }
            case NONE -> {
                logger.warning("路段" + variables.getSegmentId() + "收到无效的请求清除事件");
            }
        }

        // 记录请求清除事件
        recordRequestClearanceEvent(direction, variables);
    }

    // ==================== 清空决策处理动作 ====================

    /**
     * 清空状态更新动作
     * A(q, clearance_status_update, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeClearanceStatusUpdate(SegmentState currentState,
                                                    SegmentEvent event,
                                                    SegmentVariables variables) {
        // 重新计算所有清空决策
        updateAllClearanceDecisions(variables);

        // 检查是否需要启动或停止保守清空计时器
        manageCons
