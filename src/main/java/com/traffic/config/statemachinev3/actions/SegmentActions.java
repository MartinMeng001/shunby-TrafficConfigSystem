package com.traffic.config.statemachinev3.actions;

import com.traffic.config.service.event.EventBusService;
import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.events.GreenCtrlEvent;
import com.traffic.config.statemachinev3.events.SegmentMachineActionEvent;
import com.traffic.config.statemachinev3.utils.SpringContextUtil;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import com.traffic.config.statemachinev3.constants.SegmentConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 路段状态机动作函数
 * 对应数学模型中的动作函数 A: Q × Σ × V* → V*
 *
 * @author System
 * @version 3.0.0
 */
public class SegmentActions {

    private static final Logger logger = LoggerFactory.getLogger(SegmentActions.class);

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

        publishEvent(new GreenCtrlEvent("GreenCtrlEvent", targetState, event, variables));
        // 开始绿灯计时
        variables.startGreenTimer(direction);

        // 重置保守清空计时器
        variables.setConservativeTimerStart(null);

        // 更新服务方向记录
        variables.setLastServedDirection(direction);

        // 重置对应方向等待时间
        resetDirectionWaitingTime(direction, variables);

        // 清除对应方向的通行请求
        clearDirectionRequest(direction, variables);

        // 更新性能统计
        variables.updatePerformanceStatistics();

        // 记录状态切换
        recordStateSwitch(currentState, targetState, direction, variables);

        logger.info("路段 {} 进入绿灯状态 - 方向: {}, 前状态: {}",
                variables.getSegmentId(), direction.getDescription(), currentState.getChineseName());
    }

    /**
     * 进入全红清空状态动作
     * A(q_green, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeEnterAllRedClear(SegmentState currentState,
                                               SegmentState targetState,
                                               SegmentEvent event,
                                               SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 开始红灯计时
        variables.startRedTimer();

        // 重置绿灯计时器
        variables.resetGreenTimer();

        // 初始化清空决策为等待状态
//        variables.setUpstreamClearanceDecision(ClearanceDecision.WAIT);
//        variables.setDownstreamClearanceDecision(ClearanceDecision.WAIT);
        variables.setClearanceDecisions(ClearanceDecision.WAIT, ClearanceDecision.WAIT);
        variables.setOverallClearanceDecision(ClearanceDecision.WAIT);

        // 重置保守清空计时器
        variables.setConservativeTimerStart(null);

        // 更新等待时间
        updateWaitingTimes(variables);

        // 更新优先级得分
        updatePriorityScores(variables);

        // 记录状态切换
        recordStateSwitch(currentState, targetState, SegmentVariables.Direction.NONE, variables);

        logger.info("路段 {} 进入全红清空状态 - 前状态: {}",
                variables.getSegmentId(), currentState.getChineseName());
    }

    /**
     * 进入故障模式动作
     * A(q, fault_detected, v)
     *
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeEnterFaultMode(SegmentState currentState,
                                             SegmentState targetState,
                                             SegmentEvent event,
                                             SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 设置故障标志
        variables.setFaultDetected(true);

        // 停止所有计时器
        variables.resetAllTimers(now);

        // 清除所有通行请求
        variables.setUpstreamRequest(false);
        variables.setDownstreamRequest(false);
        variables.setUpstreamRequestTime(null);
        variables.setDownstreamRequestTime(null);

        // 设置清空决策为等待状态
//        variables.setUpstreamClearanceDecision(ClearanceDecision.WAIT);
//        variables.setDownstreamClearanceDecision(ClearanceDecision.WAIT);
        variables.setClearanceDecisions(ClearanceDecision.WAIT, ClearanceDecision.WAIT);
        variables.setOverallClearanceDecision(ClearanceDecision.WAIT);

        // 降低健康度评分
        variables.decreaseHealthScore(20);

        // 增加连续错误计数
        variables.incrementConsecutiveErrors();

        // 记录故障信息
        recordFaultOccurrence(currentState, event, variables);

        logger.warn("路段 {} 进入故障模式 - 前状态: {}, 故障事件: {}",
                variables.getSegmentId(), currentState.getChineseName(), event.getChineseName());
    }

    /**
     * 进入维护模式动作
     * A(q, maintenance_request, v)
     *
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeEnterMaintenanceMode(SegmentState currentState,
                                                   SegmentState targetState,
                                                   SegmentEvent event,
                                                   SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 停止所有自动控制
        variables.resetAllTimers(now);

        // 清除所有通行请求
        clearAllRequests(variables);

        // 设置维护时间
        variables.setLastMaintenanceTime(now);

        // 记录维护开始
        recordMaintenanceStart(currentState, variables);

        logger.info("路段 {} 进入维护模式 - 前状态: {}",
                variables.getSegmentId(), currentState.getChineseName());
    }

    /**
     * 进入空闲状态动作
     * A(q, system_init_complete, v)
     *
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeEnterIdleState(SegmentState currentState,
                                             SegmentState targetState,
                                             SegmentEvent event,
                                             SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 初始化所有状态
        initializeSegmentState(variables);

        // 重置故障标志
        variables.setFaultDetected(false);

        // 重置健康度评分
        variables.setHealthScore(100);

        // 重置错误计数
        variables.resetErrorCounters();

        logger.info("路段 {} 进入空闲状态 - 系统初始化完成", variables.getSegmentId());
    }
    public static void executeGreenTimeout(SegmentState currentState,
                                           SegmentState targetState,
                                           SegmentEvent event,
                                           SegmentVariables variables){
        switch (variables.getLastServedDirection()){
            case DOWNSTREAM -> variables.clearDownstreamMeetingzone();
            case UPSTREAM -> variables.clearUpstreamMeetingzone();
        }
    }

    // ==================== 车辆事件处理动作 ====================

    /**
     * 车辆进入动作
     * A(q, vehicle_enter, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 车辆ID
     * @param direction 车辆方向
     */
    public static void executeVehicleEnter(SegmentState currentState,
                                           SegmentEvent event,
                                           SegmentVariables variables,
                                           String vehicleId,
                                           SegmentVariables.Direction direction) {
        String vehicledid = validateVehicleId(vehicleId);
        LocalDateTime now = LocalDateTime.now();
        switch (direction){
            case UPSTREAM -> {
                variables.addUpstreamVehicle(vehicledid);
                // 要将该车辆从等待区中删除
                variables.outUpstreamMeetingzone(vehicledid);
            }
            case DOWNSTREAM -> {
                variables.addDownstreamVehicle(vehicledid);
                // 要将该车辆从等待区中删除
                variables.outDownstreamMeetingzone(vehicledid);
            }
        }

        // 检查是否冲突,其实不应该产生这种情况
        if(checkCrashLimit(direction, variables)){
            recordVehicleEntryError(vehicledid, direction, "冲突", variables);
            variables.incrementCounterMismatchErrors();
            variables.addUpstreamVehicle(vehicledid);
        }

        // 生成通行请求（如果需要）,这里应该是执行器要做得动作，而不是事件检查需要处理的。
        // generateTrafficRequestIfNeeded(direction, variables);

        // 记录事件
        logger.debug("路段 {} 车辆进入 - ID: {}, 方向: {}, 当前状态: {}",
                variables.getSegmentId(), vehicleId, direction.getDescription(), currentState.getChineseName());
    }

    /**
     * 车辆离开动作
     * A(q, vehicle_exit, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param vehicleId 车辆ID
     * @param direction 车辆方向
     */
    public static void executeVehicleExit(SegmentState currentState,
                                          SegmentEvent event,
                                          SegmentVariables variables,
                                          String vehicleId,
                                          SegmentVariables.Direction direction) {
        LocalDateTime now = LocalDateTime.now();
        String vehicledid = validateVehicleId(vehicleId);
        switch (direction){
            case UPSTREAM -> {
                variables.removeUpstreamVehicle(vehicledid);
                // 该车辆进入下行等待区中
                variables.inUpstreamMeetingzoneNext(vehicledid);
            }
            case DOWNSTREAM -> {
                variables.removeDownstreamVehicle(vehicledid);
                variables.inDownstreamMeetingzoneNext(vehicledid);
            }
        }

        // 记录事件
        logger.debug("路段 {} 车辆离开 - ID: {}, 方向: {}, 当前状态: {}",
                variables.getSegmentId(), vehicleId, direction.getDescription(), currentState.getChineseName());
    }

    // ==================== 定时器事件处理动作 ====================

    /**
     * 定时器滴答动作
     * A(q, timer_tick, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeTimerTick(SegmentState currentState,
                                        SegmentEvent event,
                                        SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 更新等待时间
        updateWaitingTimes(variables);

        // 更新优先级得分
        updatePriorityScores(variables);

        // 检查保守清空计时器
        checkConservativeTimer(variables);

        // 更新健康度评分
        updateHealthScore(variables);

        // 检查错误时间窗口
        checkErrorTimeWindow(variables);

        // 更新性能统计
        updatePerformanceMetrics(variables);

        // 检查故障条件
        checkFaultConditions(variables);
    }

    /**
     * 强制切换动作
     * A(q, force_switch, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeForceSwitch(SegmentState currentState,
                                          SegmentEvent event,
                                          SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 记录强制切换原因
        recordForceSwitchReason(currentState, event, variables);

        // 设置强制清空决策
        variables.setUpstreamClearanceDecision(ClearanceDecision.WAIT);
        variables.setDownstreamClearanceDecision(ClearanceDecision.WAIT);
        variables.setOverallClearanceDecision(ClearanceDecision.WAIT);

        // 重置计时器
        variables.resetAllTimers(now);

        logger.info("路段 {} 执行强制切换 - 当前状态: {}",
                variables.getSegmentId(), currentState.getChineseName());
    }

    // ==================== 辅助方法 ====================

    /**
     * 重置方向等待时间
     */
    private static void resetDirectionWaitingTime(SegmentVariables.Direction direction, SegmentVariables variables) {
        switch (direction) {
            case UPSTREAM -> variables.setWaitingTimeByDirection(SegmentVariables.Direction.UPSTREAM,0.0);
            case DOWNSTREAM -> variables.setWaitingTimeByDirection(SegmentVariables.Direction.DOWNSTREAM,0.0);
            case NONE -> {
                variables.setWaitingTimeByDirection(SegmentVariables.Direction.UPSTREAM,0.0);
                variables.setWaitingTimeByDirection(SegmentVariables.Direction.DOWNSTREAM,0.0);
            }
        }
    }

    /**
     * 清除方向通行请求
     */
    private static void clearDirectionRequest(SegmentVariables.Direction direction, SegmentVariables variables) {
        switch (direction) {
            case UPSTREAM -> {
                variables.setUpstreamRequest(false);
                variables.setUpstreamRequestTime(null);
            }
            case DOWNSTREAM -> {
                variables.setDownstreamRequest(false);
                variables.setDownstreamRequestTime(null);
            }
            case NONE -> clearAllRequests(variables);
        }
    }

    /**
     * 清除所有通行请求
     */
    private static void clearAllRequests(SegmentVariables variables) {
        variables.setUpstreamRequest(false);
        variables.setDownstreamRequest(false);
        variables.setUpstreamRequestTime(null);
        variables.setDownstreamRequestTime(null);
    }

    /**
     * 更新等待时间
     */
    private static void updateWaitingTimes(SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 更新上行等待时间
        if (variables.isUpstreamRequest() && variables.getUpstreamRequestTime() != null) {
            long waitingSeconds = ChronoUnit.SECONDS.between(variables.getUpstreamRequestTime(), now);
            variables.setWaitingTimeByDirection(SegmentVariables.Direction.UPSTREAM,waitingSeconds);
        }

        // 更新下行等待时间
        if (variables.isDownstreamRequest() && variables.getDownstreamRequestTime() != null) {
            long waitingSeconds = ChronoUnit.SECONDS.between(variables.getDownstreamRequestTime(), now);
            variables.setWaitingTimeByDirection(SegmentVariables.Direction.DOWNSTREAM,waitingSeconds);
        }
    }

    /**
     * 更新优先级得分
     */
    private static void updatePriorityScores(SegmentVariables variables) {
        // 计算上行优先级得分
        double upstreamScore = calculatePriorityScore(SegmentVariables.Direction.UPSTREAM, variables);
        variables.setPriorityScoreByDirection(SegmentVariables.Direction.UPSTREAM, upstreamScore);

        // 计算下行优先级得分
        double downstreamScore = calculatePriorityScore(SegmentVariables.Direction.DOWNSTREAM, variables);
        variables.setPriorityScoreByDirection(SegmentVariables.Direction.DOWNSTREAM, downstreamScore);
    }

    /**
     * 计算优先级得分
     */
    private static double calculatePriorityScore(SegmentVariables.Direction direction, SegmentVariables variables) {
        double timeScore = 0.0;
        double loadScore = 0.0;
        double alternationScore = 0.0;

        switch (direction) {
            case UPSTREAM -> {
                // 时间因子
                timeScore = Math.min(1.0, variables.getUpstreamWaitingTime() / SegmentConstants.MAX_REASONABLE_WAIT_TIME);
                // 负载因子
                loadScore = (double) variables.getUpstreamVehicleIds().size() / variables.getUpstreamCapacity();
                // 交替因子
                alternationScore = variables.getLastServedDirection() == SegmentVariables.Direction.DOWNSTREAM ? 1.0 : 0.5;
            }
            case DOWNSTREAM -> {
                // 时间因子
                timeScore = Math.min(1.0, variables.getDownstreamWaitingTime() / SegmentConstants.MAX_REASONABLE_WAIT_TIME);
                // 负载因子
                loadScore = (double) variables.getDownstreamVehicleIds().size() / variables.getDownstreamCapacity();
                // 交替因子
                alternationScore = variables.getLastServedDirection() == SegmentVariables.Direction.UPSTREAM ? 1.0 : 0.5;
            }
        }

        // 综合计算优先级得分
        double score = SegmentConstants.DEFAULT_TIME_PRIORITY_WEIGHT * timeScore +
                SegmentConstants.DEFAULT_LOAD_BALANCE_WEIGHT * loadScore +
                SegmentConstants.DEFAULT_ALTERNATION_WEIGHT * alternationScore;

        // 紧急情况加权
        double waitingTime = direction == SegmentVariables.Direction.UPSTREAM ?
                variables.getUpstreamWaitingTime() : variables.getDownstreamWaitingTime();
        if (waitingTime > SegmentConstants.EMERGENCY_OVERRIDE_THRESHOLD) {
            score *= SegmentConstants.EMERGENCY_BOOST_FACTOR;
        }

        return score;
    }

    /**
     * 验证车辆进入条件
     */
    private static boolean validateVehicleEntry(String vehicleId, SegmentVariables.Direction direction, SegmentVariables variables) {
        // 检查车辆ID是否为空
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return false;
        }

        // 检查方向是否有效
        if (direction == SegmentVariables.Direction.NONE) {
            return false;
        }

        // 检查车辆是否已存在
        return !variables.getUpstreamVehicleIds().contains(vehicleId) &&
                !variables.getDownstreamVehicleIds().contains(vehicleId);
    }
    private static String validateVehicleId(String vehicleId){
        // 检查车辆ID是否为空
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return "";
        }
        if("无车牌".equals(vehicleId)) return "";
        return vehicleId;
    }

    /**
     * 验证车辆离开条件
     */
    private static boolean validateVehicleExit(String vehicleId, SegmentVariables.Direction direction, SegmentVariables variables) {
        // 检查车辆ID是否为空
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return false;
        }

        // 检查车辆是否存在于指定方向
        return switch (direction) {
            case UPSTREAM -> variables.getUpstreamVehicleIds().contains(vehicleId);
            case DOWNSTREAM -> variables.getDownstreamVehicleIds().contains(vehicleId);
            case NONE -> false;
        };
    }

    /**
     * 检查容量限制
     */
    private static boolean checkCapacityLimit(SegmentVariables.Direction direction, SegmentVariables variables) {
        return switch (direction) {
            case UPSTREAM -> variables.getUpstreamVehicleIds().size() < variables.getUpstreamCapacity();
            case DOWNSTREAM -> variables.getDownstreamVehicleIds().size() < variables.getDownstreamCapacity();
            case NONE -> false;
        };
    }
    /**
     * 检查车辆冲突
     */
    private static boolean checkCrashLimit(SegmentVariables.Direction direction, SegmentVariables variables){
        return switch (direction){
            case UPSTREAM -> variables.hasRequestByDirection(SegmentVariables.Direction.DOWNSTREAM);
            case DOWNSTREAM -> variables.hasRequestByDirection(SegmentVariables.Direction.UPSTREAM);
            case NONE -> false;
        };
    }

    /**
     * 添加车辆记录
     */
    private static void addVehicleRecord(String vehicleId, SegmentVariables.Direction direction, SegmentVariables variables) {
        switch (direction) {
            case UPSTREAM -> variables.getUpstreamVehicleIds().add(vehicleId);
            case DOWNSTREAM -> variables.getDownstreamVehicleIds().add(vehicleId);
        }
    }

    /**
     * 移除车辆记录
     */
    private static void removeVehicleRecord(String vehicleId, SegmentVariables.Direction direction, SegmentVariables variables) {
        switch (direction) {
            case UPSTREAM -> variables.getUpstreamVehicleIds().remove(vehicleId);
            case DOWNSTREAM -> variables.getDownstreamVehicleIds().remove(vehicleId);
        }
    }

    /**
     * 增加方向计数器
     */
    private static void incrementDirectionCounter(SegmentVariables.Direction direction, SegmentVariables variables, boolean isEntry) {
        switch (direction) {
            case UPSTREAM -> {
                if (isEntry) {
                    variables.incrementUpstreamInCounter();
                } else {
                    variables.incrementUpstreamOutCounter();
                }
            }
            case DOWNSTREAM -> {
                if (isEntry) {
                    variables.incrementDownstreamInCounter();
                } else {
                    variables.incrementDownstreamOutCounter();
                }
            }
        }
    }

    /**
     * 生成通行请求（如果需要）
     */
    private static void generateTrafficRequestIfNeeded(SegmentVariables.Direction direction, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        switch (direction) {
            case UPSTREAM -> {
                if (!variables.isUpstreamRequest()) {
                    int threshold = SegmentConstants.calculateRequestTriggerThreshold(variables.getUpstreamCapacity());
                    if (variables.getUpstreamVehicleIds().size() >= threshold) {
                        variables.setUpstreamRequest(true);
                        variables.setUpstreamRequestTime(now);
                        logger.debug("路段 {} 生成上行通行请求 - 车辆数: {}, 阈值: {}",
                                variables.getSegmentId(), variables.getUpstreamVehicleIds().size(), threshold);
                    }
                }
            }
            case DOWNSTREAM -> {
                if (!variables.isDownstreamRequest()) {
                    int threshold = SegmentConstants.calculateRequestTriggerThreshold(variables.getDownstreamCapacity());
                    if (variables.getDownstreamVehicleIds().size() >= threshold) {
                        variables.setDownstreamRequest(true);
                        variables.setDownstreamRequestTime(now);
                        logger.debug("路段 {} 生成下行通行请求 - 车辆数: {}, 阈值: {}",
                                variables.getSegmentId(), variables.getDownstreamVehicleIds().size(), threshold);
                    }
                }
            }
        }
    }

    /**
     * 检查并清除通行请求（如果需要）
     */
    private static void checkAndClearRequestIfNeeded(SegmentVariables.Direction direction, SegmentVariables variables) {
        switch (direction) {
            case UPSTREAM -> {
                if (variables.isUpstreamRequest() && variables.getUpstreamVehicleIds().isEmpty()) {
                    // 检查计数器一致性
                    if (variables.getUpstreamInCounter() == variables.getUpstreamOutCounter()) {
                        variables.setUpstreamRequest(false);
                        variables.setUpstreamRequestTime(null);
                        logger.debug("路段 {} 清除上行通行请求 - 无剩余车辆", variables.getSegmentId());
                    }
                }
            }
            case DOWNSTREAM -> {
                if (variables.isDownstreamRequest() && variables.getDownstreamVehicleIds().isEmpty()) {
                    // 检查计数器一致性
                    if (variables.getDownstreamInCounter() == variables.getDownstreamOutCounter()) {
                        variables.setDownstreamRequest(false);
                        variables.setDownstreamRequestTime(null);
                        logger.debug("路段 {} 清除下行通行请求 - 无剩余车辆", variables.getSegmentId());
                    }
                }
            }
        }
    }

    /**
     * 更新车辆等待时间
     */
    private static void updateVehicleWaitingTime(String vehicleId, SegmentVariables variables) {
        LocalDateTime entryTime = variables.getVehicleEntryTime(vehicleId);
        if (entryTime != null) {
            LocalDateTime now = LocalDateTime.now();
            long waitingSeconds = ChronoUnit.SECONDS.between(entryTime, now);

            // 更新平均等待时间（移动平均）
            double currentAvg = variables.getAverageWaitingTime();
            double newAvg = SegmentConstants.WAITING_TIME_SMOOTH_FACTOR * currentAvg +
                    (1 - SegmentConstants.WAITING_TIME_SMOOTH_FACTOR) * waitingSeconds;
            variables.setAverageWaitingTime(newAvg);
        }
    }

    /**
     * 更新性能指标
     */
    private static void updatePerformanceMetrics(SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 更新通行效率
        updateThroughputRate(variables);

        // 更新拥堵程度
        updateCongestionLevel(variables);
    }

    /**
     * 更新通行效率
     */
    private static void updateThroughputRate(SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSwitch = variables.getLastSwitchTime();

        if (lastSwitch != null) {
            long timeWindowSeconds = ChronoUnit.SECONDS.between(lastSwitch, now);
            if (timeWindowSeconds > 0) {
                long totalVehicles = variables.getTotalVehiclesServed();
                double rate = (double) totalVehicles / timeWindowSeconds;
                variables.setThroughputRate(rate);
            }
        }
    }

    /**
     * 更新拥堵程度
     */
    private static void updateCongestionLevel(SegmentVariables variables) {
        int totalVehicles = variables.getUpstreamVehicleIds().size() + variables.getDownstreamVehicleIds().size();
        int totalCapacity = variables.getUpstreamCapacity() + variables.getDownstreamCapacity();

        double congestionLevel = totalCapacity > 0 ? (double) totalVehicles / totalCapacity : 0.0;
        variables.setCongestionLevel(Math.min(1.0, congestionLevel));
    }

    /**
     * 检查保守清空计时器
     */
    private static void checkConservativeTimer(SegmentVariables variables) {
        LocalDateTime conservativeStart = variables.getConservativeTimerStart();
        if (conservativeStart != null) {
            LocalDateTime now = LocalDateTime.now();
            long elapsedSeconds = ChronoUnit.SECONDS.between(conservativeStart, now);

            if (elapsedSeconds >= SegmentConstants.CONSERVATIVE_CLEAR_TIME) {
                // 保守清空时间到期，强制清空
                variables.setUpstreamClearanceDecision(ClearanceDecision.SAFE);
                variables.setDownstreamClearanceDecision(ClearanceDecision.SAFE);
                variables.setOverallClearanceDecision(ClearanceDecision.SAFE);
                variables.setConservativeTimerStart(null);

                logger.info("路段 {} 保守清空计时器到期，强制清空", variables.getSegmentId());
            }
        }
    }

    /**
     * 更新健康度评分
     */
    private static void updateHealthScore(SegmentVariables variables) {
        int currentScore = variables.getSegmentHealthScore();

        // 基于错误数量调整
        int errorPenalty = (variables.getErrorCountMismatch() + variables.getErrorCountIdLogic()) * 2;

        // 基于连续错误调整
        int consecutivePenalty = variables.getConsecutiveErrors() * 3;

        // 基于性能调整（正向）
        int performanceBonus = (int) (variables.getThroughputRate() * 10);

        // 计算新的健康度
        int newScore = Math.max(0, Math.min(100,
                currentScore - errorPenalty - consecutivePenalty + performanceBonus));

        variables.setHealthScore(newScore);
    }

    /**
     * 检查错误时间窗口
     */
    private static void checkErrorTimeWindow(SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = variables.getErrorTimeWindowStart();

        if (windowStart != null) {
            long elapsedSeconds = ChronoUnit.SECONDS.between(windowStart, now);

            // 如果超过错误统计窗口时间，重置错误计数
            if (elapsedSeconds >= SegmentConstants.ERROR_WINDOW) {
                variables.resetErrorCounters();
                variables.setErrorTimeWindowStart(now);

                logger.debug("路段 {} 错误时间窗口重置", variables.getSegmentId());
            }
        } else {
            variables.setErrorTimeWindowStart(now);
        }
    }

    /**
     * 检查故障条件
     */
    private static void checkFaultConditions(SegmentVariables variables) {
        boolean faultDetected = false;
        String faultReason = "";

        // 检查计数器不匹配错误
        if (variables.getErrorCountMismatch() >= SegmentConstants.MAX_COUNTER_MISMATCH_ERRORS) {
            faultDetected = true;
            faultReason = "计数器不匹配错误超限";
        }

        // 检查ID逻辑错误
        if (variables.getErrorCountIdLogic() >= SegmentConstants.MAX_ID_LOGIC_ERRORS) {
            faultDetected = true;
            faultReason = "ID逻辑错误超限";
        }

        // 检查连续错误
        if (variables.getConsecutiveErrors() >= SegmentConstants.MAX_CONSECUTIVE_ERRORS) {
            faultDetected = true;
            faultReason = "连续错误超限";
        }

        // 检查健康度
        if (variables.getSegmentHealthScore() < SegmentConstants.CRITICAL_HEALTH_THRESHOLD) {
            faultDetected = true;
            faultReason = "路段健康度过低";
        }

        // 更新故障状态
        if (faultDetected && !variables.isFaultDetected()) {
            variables.setFaultDetected(true);
            recordFaultDetection(faultReason, variables);
            logger.warn("路段 {} 检测到故障 - 原因: {}", variables.getSegmentId(), faultReason);
        }
    }

    /**
     * 初始化路段状态
     */
    private static void initializeSegmentState(SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 重置所有计时器
        variables.resetAllTimers(now);

        // 清除所有车辆记录
        variables.getUpstreamVehicleIds().clear();
        variables.getDownstreamVehicleIds().clear();

        // 重置计数器
        variables.resetCounters();

        // 清除通行请求
        clearAllRequests(variables);

        // 重置清空决策
        variables.setUpstreamClearanceDecision(ClearanceDecision.WAIT);
        variables.setDownstreamClearanceDecision(ClearanceDecision.WAIT);
        variables.setOverallClearanceDecision(ClearanceDecision.WAIT);

        // 重置服务方向
        variables.setLastServedDirection(SegmentVariables.Direction.NONE);

        // 重置等待时间和优先级
        variables.setWaitingTimeByDirection(SegmentVariables.Direction.DOWNSTREAM,0.0);
        variables.setWaitingTimeByDirection(SegmentVariables.Direction.UPSTREAM,0.0);
        variables.setPriorityScoreByDirection(SegmentVariables.Direction.UPSTREAM, 0.0);
        variables.setPriorityScoreByDirection(SegmentVariables.Direction.DOWNSTREAM, 0.0);

        // 重置性能统计
        variables.setAverageWaitingTime(0.0);
        variables.setThroughputRate(0.0);
        variables.setCongestionLevel(0.0);

        // 重置错误状态
        variables.resetErrorCounters();
        variables.setErrorTimeWindowStart(now);

        logger.info("路段 {} 状态初始化完成", variables.getSegmentId());
    }

    // ==================== 记录和日志方法 ====================

    /**
     * 记录状态切换
     */
    private static void recordStateSwitch(SegmentState fromState, SegmentState toState,
                                          SegmentVariables.Direction direction, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();
        variables.setLastSwitchTime(now);

        logger.info("路段 {} 状态切换: {} -> {}, 方向: {}",
                variables.getSegmentId(),
                fromState.getChineseName(),
                toState.getChineseName(),
                direction.getDescription());
    }

    /**
     * 记录故障发生
     */
    private static void recordFaultOccurrence(SegmentState currentState, SegmentEvent event, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 这里可以记录到日志系统或数据库
        String faultRecord = String.format("路段 %d 故障 - 状态: %s, 事件: %s, 时间: %s",
                variables.getSegmentId(),
                currentState.getChineseName(),
                event.getChineseName(),
                now);

        logger.error(faultRecord);
    }

    /**
     * 记录维护开始
     */
    private static void recordMaintenanceStart(SegmentState currentState, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        String maintenanceRecord = String.format("路段 %d 开始维护 - 前状态: %s, 时间: %s",
                variables.getSegmentId(),
                currentState.getChineseName(),
                now);

        logger.info(maintenanceRecord);
    }

    /**
     * 记录强制切换原因
     */
    private static void recordForceSwitchReason(SegmentState currentState, SegmentEvent event, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        String switchRecord = String.format("路段 %d 强制切换 - 状态: %s, 事件: %s, 时间: %s",
                variables.getSegmentId(),
                currentState.getChineseName(),
                event.getChineseName(),
                now);

        logger.warn(switchRecord);
    }

    /**
     * 记录车辆进入错误
     */
    private static void recordVehicleEntryError(String vehicleId, SegmentVariables.Direction direction,
                                                String reason, SegmentVariables variables) {
        variables.incrementErrorCountIdLogic();

        String errorRecord = String.format("路段 %d 车辆进入错误 - 车辆: %s, 方向: %s, 原因: %s",
                variables.getSegmentId(),
                vehicleId,
                direction.getDescription(),
                reason);

        logger.warn(errorRecord);
    }

    /**
     * 记录车辆离开错误
     */
    private static void recordVehicleExitError(String vehicleId, SegmentVariables.Direction direction,
                                               String reason, SegmentVariables variables) {
        variables.incrementErrorCountIdLogic();

        String errorRecord = String.format("路段 %d 车辆离开错误 - 车辆: %s, 方向: %s, 原因: %s",
                variables.getSegmentId(),
                vehicleId,
                direction.getDescription(),
                reason);

        logger.warn(errorRecord);
    }

    /**
     * 记录故障检测
     */
    private static void recordFaultDetection(String reason, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        String faultRecord = String.format("路段 %d 故障检测 - 原因: %s, 时间: %s, 健康度: %d",
                variables.getSegmentId(),
                reason,
                now,
                variables.getSegmentHealthScore());

        logger.error(faultRecord);
    }

    // ==================== 清空决策相关动作 ====================

    /**
     * 更新清空决策动作
     * A(q, clearance_decision_update, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param upstreamDecision 上行清空决策
     * @param downstreamDecision 下行清空决策
     */
    public static void executeUpdateClearanceDecision(SegmentState currentState,
                                                      SegmentEvent event,
                                                      SegmentVariables variables,
                                                      ClearanceDecision upstreamDecision,
                                                      ClearanceDecision downstreamDecision) {
        LocalDateTime now = LocalDateTime.now();

        // 更新清空决策
        variables.setUpstreamClearanceDecision(upstreamDecision);
        variables.setDownstreamClearanceDecision(downstreamDecision);

        // 计算综合清空决策
        ClearanceDecision overallDecision = calculateOverallClearanceDecision(upstreamDecision, downstreamDecision);
        variables.setOverallClearanceDecision(overallDecision);

        // 处理保守清空
        handleConservativeClearance(upstreamDecision, downstreamDecision, variables);

        logger.debug("路段 {} 清空决策更新 - 上行: {}, 下行: {}, 综合: {}",
                variables.getSegmentId(),
                upstreamDecision,
                downstreamDecision,
                overallDecision);
    }

    /**
     * 计算综合清空决策
     */
    private static ClearanceDecision calculateOverallClearanceDecision(ClearanceDecision upstream, ClearanceDecision downstream) {
        // 如果任一方向为等待，则整体为等待
        if (upstream == ClearanceDecision.WAIT || downstream == ClearanceDecision.WAIT) {
            return ClearanceDecision.WAIT;
        }

        // 如果任一方向为保守，则整体为保守
        if (upstream == ClearanceDecision.CONSERVATIVE || downstream == ClearanceDecision.CONSERVATIVE) {
            return ClearanceDecision.CONSERVATIVE;
        }

        // 如果任一方向为警告，则整体为警告
        if (upstream == ClearanceDecision.WARNING || downstream == ClearanceDecision.WARNING) {
            return ClearanceDecision.WARNING;
        }

        // 只有当两个方向都为安全时，整体才为安全
        return ClearanceDecision.SAFE;
    }

    /**
     * 处理保守清空
     */
    private static void handleConservativeClearance(ClearanceDecision upstream, ClearanceDecision downstream, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 如果任一方向为保守清空，启动保守清空计时器
        if ((upstream == ClearanceDecision.CONSERVATIVE || downstream == ClearanceDecision.CONSERVATIVE)
                && variables.getConservativeTimerStart() == null) {

            variables.setConservativeTimerStart(now);
            logger.info("路段 {} 启动保守清空计时器", variables.getSegmentId());
        }

        // 如果两个方向都不是保守清空，停止计时器
        if (upstream != ClearanceDecision.CONSERVATIVE && downstream != ClearanceDecision.CONSERVATIVE) {
            variables.setConservativeTimerStart(null);
        }
    }

    // ==================== 恢复相关动作 ====================

    /**
     * 执行故障恢复动作
     * A(FAULT_MODE, recovery_request, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeRecoveryFromFault(SegmentState currentState,
                                                SegmentEvent event,
                                                SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 重置故障标志
        variables.setFaultDetected(false);

        // 重置错误计数器
        variables.resetErrorCounters();

        // 重置健康度评分
        variables.setHealthScore(80); // 恢复后的初始健康度

        // 重新初始化状态
        initializeSegmentState(variables);

        // 记录恢复事件
        recordRecoveryFromFault(currentState, variables);

        logger.info("路段 {} 从故障模式恢复", variables.getSegmentId());
    }

    /**
     * 记录故障恢复
     */
    private static void recordRecoveryFromFault(SegmentState currentState, SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        String recoveryRecord = String.format("路段 %d 故障恢复 - 前状态: %s, 时间: %s, 当前健康度: %d",
                variables.getSegmentId(),
                currentState.getChineseName(),
                now,
                variables.getSegmentHealthScore());

        logger.info(recoveryRecord);
    }

    // ==================== 传感器相关动作 ====================

    /**
     * 处理传感器状态更新动作
     * A(q, sensor_status_update, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     * @param sensorId 传感器ID
     * @param sensorState 传感器状态
     */
    public static void executeSensorStatusUpdate(SegmentState currentState,
                                                 SegmentEvent event,
                                                 SegmentVariables variables,
                                                 String sensorId,
                                                 SegmentVariables.SensorState sensorState) {
        LocalDateTime now = LocalDateTime.now();

        // 更新传感器状态
        variables.updateSensorStatus(sensorId, sensorState);

        // 检查传感器健康状况
        checkSensorHealth(variables);

        logger.debug("路段 {} 传感器状态更新 - ID: {}, 状态: {}",
                variables.getSegmentId(), sensorId, sensorState.getDescription());
    }

    /**
     * 检查传感器健康状况
     */
    private static void checkSensorHealth(SegmentVariables variables) {
        Map<String, SegmentVariables.SensorState> sensorStatus = variables.getSensorStatus();

        long failedSensors = sensorStatus.values().stream()
                .mapToLong(state -> state == SegmentVariables.SensorState.FAILED ? 1 : 0)
                .sum();

        long degradedSensors = sensorStatus.values().stream()
                .mapToLong(state -> state == SegmentVariables.SensorState.DEGRADED ? 1 : 0)
                .sum();

        // 如果故障传感器过多，降低健康度
        if (!sensorStatus.isEmpty()) {
            double failureRate = (double) failedSensors / sensorStatus.size();
            double degradationRate = (double) degradedSensors / sensorStatus.size();

            if (failureRate > SegmentConstants.SENSOR_ERROR_RATE_THRESHOLD) {
                variables.decreaseHealthScore(10);
                logger.warn("路段 {} 传感器故障率过高: {:.2f}%",
                        variables.getSegmentId(), failureRate * 100);
            }

            if (degradationRate > SegmentConstants.SENSOR_ERROR_RATE_THRESHOLD * 2) {
                variables.decreaseHealthScore(5);
                logger.warn("路段 {} 传感器降级率过高: {:.2f}%",
                        variables.getSegmentId(), degradationRate * 100);
            }
        }
    }

    // ==================== 调试和监控动作 ====================

    /**
     * 执行状态诊断动作
     * A(q, diagnostic_request, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executeStateDiagnostic(SegmentState currentState,
                                              SegmentEvent event,
                                              SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 收集诊断信息
        StringBuilder diagnosticInfo = new StringBuilder();
        diagnosticInfo.append(String.format("路段 %d 状态诊断报告 [%s]:\n", variables.getSegmentId(), now));
        diagnosticInfo.append(String.format("  当前状态: %s\n", currentState.getChineseName()));
        diagnosticInfo.append(String.format("  健康度评分: %d/100\n", variables.getSegmentHealthScore()));
        diagnosticInfo.append(String.format("  故障状态: %s\n", variables.isFaultDetected() ? "是" : "否"));
        diagnosticInfo.append(String.format("  上行车辆数: %d/%d\n", variables.getUpstreamVehicleIds().size(), variables.getUpstreamCapacity()));
        diagnosticInfo.append(String.format("  下行车辆数: %d/%d\n", variables.getDownstreamVehicleIds().size(), variables.getDownstreamCapacity()));
        diagnosticInfo.append(String.format("  上行请求: %s\n", variables.isUpstreamRequest() ? "是" : "否"));
        diagnosticInfo.append(String.format("  下行请求: %s\n", variables.isDownstreamRequest() ? "是" : "否"));
        diagnosticInfo.append(String.format("  清空决策: %s\n", variables.getOverallClearanceDecision()));
        diagnosticInfo.append(String.format("  错误计数: 不匹配=%d, ID逻辑=%d, 连续=%d\n",
                variables.getErrorCountMismatch(),
                variables.getErrorCountIdLogic(),
                variables.getConsecutiveErrors()));
        diagnosticInfo.append(String.format("  性能指标: 通行效率=%.3f, 拥堵程度=%.2f, 平均等待=%.1fs\n",
                variables.getThroughputRate(),
                variables.getCongestionLevel(),
                variables.getAverageWaitingTime()));

        logger.info(diagnosticInfo.toString());
    }

    /**
     * 执行性能报告动作
     * A(q, performance_report_request, v)
     *
     * @param currentState 当前状态
     * @param event 触发事件
     * @param variables 路段变量
     */
    public static void executePerformanceReport(SegmentState currentState,
                                                SegmentEvent event,
                                                SegmentVariables variables) {
        LocalDateTime now = LocalDateTime.now();

        // 生成性能报告
        StringBuilder performanceReport = new StringBuilder();
        performanceReport.append(String.format("路段 %d 性能报告 [%s]:\n", variables.getSegmentId(), now));
        performanceReport.append(String.format("  服务车辆总数: %d\n", variables.getTotalVehiclesServed()));
        performanceReport.append(String.format("  通行效率: %.3f 车辆/秒\n", variables.getThroughputRate()));
        performanceReport.append(String.format("  平均等待时间: %.1f 秒\n", variables.getAverageWaitingTime()));
        performanceReport.append(String.format("  当前拥堵程度: %.1f%%\n", variables.getCongestionLevel() * 100));
        performanceReport.append(String.format("  最后服务方向: %s\n", variables.getLastServedDirection().getDescription()));

        if (variables.getLastSwitchTime() != null) {
            long timeSinceLastSwitch = ChronoUnit.SECONDS.between(variables.getLastSwitchTime(), now);
            performanceReport.append(String.format("  距离上次切换: %d 秒\n", timeSinceLastSwitch));
        }

        // 优先级得分
        performanceReport.append(String.format("  优先级得分: 上行=%.3f, 下行=%.3f\n",
                variables.getPriorityScoreUpstream(),
                variables.getPriorityScoreDownstream()));

        logger.info(performanceReport.toString());
    }

    /**
     * 发布事件的辅助方法
     */
//    private static void publishEvent(SegmentMachineActionEvent event) {
//        if (SpringContextUtil.isContextAvailable()) {
//            try {
//                ApplicationEventPublisher publisher = SpringContextUtil.getBean(ApplicationEventPublisher.class);
//                publisher.publishEvent(event);
//            } catch (Exception e) {
//                logger.warn("发布状态机事件失败: {}", e.getMessage());
//            }
//        } else {
//            logger.warn("Spring上下文不可用，无法发布控制信号机事件");
//        }
//    }
    private static void publishEvent(SegmentMachineActionEvent event) {
        if (EventBusService.isReady()) {
            EventBusService.publishStatic(event);
        } else {
            logger.warn("事件总线未就绪，无法发布事件");
        }
    }
}