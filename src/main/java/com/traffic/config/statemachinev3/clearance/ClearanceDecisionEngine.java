package com.traffic.config.statemachinev3.clearance;

import com.traffic.config.statemachinev3.actions.SegmentActions;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import com.traffic.config.statemachinev3.constants.SegmentConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * 路段清空决策引擎
 * 实现四级清空决策算法：SAFE, WARNING, CONSERVATIVE, WAIT
 *
 * 对应数学模型中的清空决策函数：
 * C_upstream(v) → {SAFE, WARNING, CONSERVATIVE, WAIT}
 * C_downstream(v) → {SAFE, WARNING, CONSERVATIVE, WAIT}
 * C_overall(v) → {SAFE, WARNING, CONSERVATIVE, WAIT}
 *
 * @author System
 * @version 3.0.0
 */
public class ClearanceDecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(ClearanceDecisionEngine.class);

    // ==================== 主要清空决策方法 ====================

    /**
     * 计算上行方向清空决策
     * C_upstream(v) = evaluate_clearance(upstream_data, v)
     *
     * @param variables 路段变量
     * @return 上行清空决策
     */
    public ClearanceDecision calculateUpstreamClearance(SegmentVariables variables) {
        ClearanceContext context = createClearanceContext(
                SegmentVariables.Direction.UPSTREAM, variables);

        ClearanceDecision decision = evaluateClearanceDecision(context);

        // 记录决策过程
        logClearanceDecision("上行", decision, context, variables);

        return decision;
    }

    /**
     * 计算下行方向清空决策
     * C_downstream(v) = evaluate_clearance(downstream_data, v)
     *
     * @param variables 路段变量
     * @return 下行清空决策
     */
    public ClearanceDecision calculateDownstreamClearance(SegmentVariables variables) {
        ClearanceContext context = createClearanceContext(
                SegmentVariables.Direction.DOWNSTREAM, variables);

        ClearanceDecision decision = evaluateClearanceDecision(context);

        // 记录决策过程
        logClearanceDecision("下行", decision, context, variables);

        return decision;
    }

    /**
     * 计算综合清空决策
     * C_overall(v) = combine_decisions(C_upstream(v), C_downstream(v))
     *
     * @param variables 路段变量
     * @return 综合清空决策
     */
    public ClearanceDecision calculateOverallClearance(SegmentVariables variables) {
        ClearanceDecision upstreamDecision = calculateUpstreamClearance(variables);
        ClearanceDecision downstreamDecision = calculateDownstreamClearance(variables);

        ClearanceDecision overallDecision = combineDirectionalDecisions(
                upstreamDecision, downstreamDecision);

        //logger.debug("路段 {} 综合清空决策: 上行={}, 下行={}, 综合={}",
        //        variables.getSegmentId(), upstreamDecision, downstreamDecision, overallDecision);

        return overallDecision;
    }

    // ==================== 清空决策核心算法 ====================

    /**
     * 评估清空决策的核心算法
     * 基于四个关键因素：车辆ID集合、计数器状态、传感器状态、时间因素
     *
     * @param context 清空上下文
     * @return 清空决策
     */
    private ClearanceDecision evaluateClearanceDecision(ClearanceContext context) {
        // 1. 检查是否为等待状态（最高优先级）
        if (shouldWait(context)) {
            return ClearanceDecision.WAIT;
        }

        // 2. 检查是否为安全状态（理想情况）
        if (isSafeForClearance(context)) {
            return ClearanceDecision.SAFE;
        }

        // 3. 检查是否为警告状态（需要注意但可通行）
//        if (isWarningCondition(context)) {
//            return ClearanceDecision.WARNING;
//        }

        // 4. 默认为保守状态（需要保守清空）
//        if(isCONSERVATIVECondition(context)) {
//            return ClearanceDecision.CONSERVATIVE;
//        }
        return ClearanceDecision.CONSERVATIVE;
    }

    /**
     * 判断是否应该等待
     * 条件：存在明显的不安全因素
     */
    private boolean shouldWait(ClearanceContext context) {
        // 车辆ID集合不为空且计数器严重不匹配
        boolean severeCounterMismatch = !context.vehicleIds.isEmpty() &&
                Math.abs(context.inCounter - context.outCounter) >= 1;

        // 传感器故障率过高, 不是目前算法规定的条件
        // boolean sensorCriticalFailure = context.sensorFailureRate > 0.5;

        // 存在严重的逻辑错误 不是目前算法规定的条件
        // boolean severeLogicError = context.consecutiveErrors > SegmentConstants.MAX_CONSECUTIVE_ERRORS / 2;

        // 健康度过低, 先不考虑健康度问题
        //boolean criticalHealth = context.segmentHealthScore < SegmentConstants.CRITICAL_HEALTH_THRESHOLD;

        return severeCounterMismatch;// || criticalHealth|| sensorCriticalFailure ||severeLogicError
    }

    /**
     * 判断是否安全清空
     * 条件：所有指标都正常
     */
    private boolean isSafeForClearance(ClearanceContext context) {
        // 车辆ID集合为空
        boolean vehicleIdsEmpty = context.vehicleIds.isEmpty();

        // 计数器完全匹配
        boolean countersMatch = context.inCounter == context.outCounter;

        // 传感器状态良好
        //boolean sensorsHealthy = context.sensorFailureRate < SegmentConstants.SENSOR_ERROR_RATE_THRESHOLD;

        // 健康度良好
        //boolean healthGood = context.segmentHealthScore >= SegmentConstants.NORMAL_HEALTH_THRESHOLD;

        // 没有连续错误
        //boolean noRecentErrors = context.consecutiveErrors == 0;

        return vehicleIdsEmpty && countersMatch;//&& sensorsHealthy &&
                //healthGood && noRecentErrors;
    }

    /**
     * 判断是否为警告状态
     * 条件：存在一些异常但不严重
     */
    private boolean isWarningCondition(ClearanceContext context) {
        // 车辆ID集合为空但计数器不匹配
        boolean minorCounterMismatch = context.vehicleIds.isEmpty() &&
                Math.abs(context.inCounter - context.outCounter) >= 1;

        // 传感器轻微降级
        //boolean minorSensorIssues = context.sensorFailureRate > SegmentConstants.SENSOR_ERROR_RATE_THRESHOLD &&
         //       context.sensorFailureRate <= 0.3;

        // 健康度中等
        //boolean moderateHealth = context.segmentHealthScore >= SegmentConstants.CRITICAL_HEALTH_THRESHOLD &&
        //        context.segmentHealthScore < SegmentConstants.NORMAL_HEALTH_THRESHOLD;

        // 有少量错误但不严重
        //boolean minorErrors = context.consecutiveErrors > 0 &&
        //        context.consecutiveErrors <= SegmentConstants.MAX_CONSECUTIVE_ERRORS / 2;

        return minorCounterMismatch;//|| minorSensorIssues || moderateHealth || minorErrors;
    }

    /**
     * 判断是否为保守清空状态
     * 条件：存在一些异常但不严重
     */
    private boolean isCONSERVATIVECondition(ClearanceContext context) {
        // 车辆ID集合为空但计数器不匹配
        boolean minorCounterMismatch = !context.vehicleIds.isEmpty() &&
                Math.abs(context.inCounter - context.outCounter) == 0;

        // 传感器轻微降级
        //boolean minorSensorIssues = context.sensorFailureRate > SegmentConstants.SENSOR_ERROR_RATE_THRESHOLD &&
        //       context.sensorFailureRate <= 0.3;

        // 健康度中等
        //boolean moderateHealth = context.segmentHealthScore >= SegmentConstants.CRITICAL_HEALTH_THRESHOLD &&
        //        context.segmentHealthScore < SegmentConstants.NORMAL_HEALTH_THRESHOLD;

        // 有少量错误但不严重
        //boolean minorErrors = context.consecutiveErrors > 0 &&
        //        context.consecutiveErrors <= SegmentConstants.MAX_CONSECUTIVE_ERRORS / 2;

        return minorCounterMismatch;//|| minorSensorIssues || moderateHealth || minorErrors;
    }
    /**
     * 合并双方向清空决策
     * 优先级：WAIT > CONSERVATIVE > WARNING > SAFE
     */
    private ClearanceDecision combineDirectionalDecisions(ClearanceDecision upstream, ClearanceDecision downstream) {
        // 如果任一方向需要等待，整体等待
        if (upstream == ClearanceDecision.WAIT || downstream == ClearanceDecision.WAIT) {
            return ClearanceDecision.WAIT;
        }

        // 如果任一方向需要保守清空，整体保守清空
        if (upstream == ClearanceDecision.CONSERVATIVE || downstream == ClearanceDecision.CONSERVATIVE) {
            return ClearanceDecision.CONSERVATIVE;
        }

        // 如果任一方向为警告，整体为警告
        if (upstream == ClearanceDecision.WARNING || downstream == ClearanceDecision.WARNING) {
            return ClearanceDecision.WARNING;
        }

        // 只有当两个方向都安全时，整体才安全
        return ClearanceDecision.SAFE;
    }

    // ==================== 清空上下文创建 ====================

    /**
     * 创建清空决策上下文
     * 收集评估清空决策所需的所有信息
     */
    private ClearanceContext createClearanceContext(SegmentVariables.Direction direction,
                                                    SegmentVariables variables) {
        ClearanceContext context = new ClearanceContext();
        context.direction = direction;
        context.segmentId = variables.getSegmentId();

        // 根据方向获取相应的数据
        switch (direction) {
            case UPSTREAM -> {
                context.vehicleIds = variables.getUpstreamVehicleIds();
                context.inCounter = variables.getUpstreamInCounter();
                context.outCounter = variables.getUpstreamOutCounter();
                context.capacity = variables.getUpstreamCapacity();
            }
            case DOWNSTREAM -> {
                context.vehicleIds = variables.getDownstreamVehicleIds();
                context.inCounter = variables.getDownstreamInCounter();
                context.outCounter = variables.getDownstreamOutCounter();
                context.capacity = variables.getDownstreamCapacity();
            }
            default -> throw new IllegalArgumentException("不支持的方向: " + direction);
        }

        // 收集共同的上下文信息
        context.segmentHealthScore = variables.getSegmentHealthScore();
        context.consecutiveErrors = variables.getConsecutiveErrors();
        context.errorCountMismatch = variables.getErrorCountMismatch();
        context.errorCountIdLogic = variables.getErrorCountIdLogic();
        context.sensorFailureRate = calculateSensorFailureRate(variables);
        context.lastSwitchTime = variables.getLastSwitchTime();
        context.congestionLevel = variables.getCongestionLevel();

        return context;
    }

    /**
     * 计算传感器故障率
     */
    private double calculateSensorFailureRate(SegmentVariables variables) {
        if (variables.getSensorStatus().isEmpty()) {
            return 0.0;
        }

        long failedSensors = variables.getSensorStatus().values().stream()
                .mapToLong(state -> state == SegmentVariables.SensorState.FAILED ? 1 : 0)
                .sum();

        return (double) failedSensors / variables.getSensorStatus().size();
    }

    // ==================== 保守清空处理 ====================

    /**
     * 检查保守清空是否应该强制执行
     * 当保守清空计时器到期时，强制切换到SAFE状态
     *
     * @param variables 路段变量
     * @return 是否应该强制清空
     */
    public boolean shouldForceConservativeClearance(SegmentVariables variables) {
        LocalDateTime conservativeStart = variables.getConservativeTimerStart();
        if (conservativeStart == null) {
            return false;
        }

        long elapsedSeconds = ChronoUnit.SECONDS.between(conservativeStart, LocalDateTime.now());
        boolean shouldForce = elapsedSeconds >= SegmentConstants.CONSERVATIVE_CLEAR_TIME;

        if (shouldForce) {
            logger.info("路段 {} 保守清空计时器到期，强制清空 - 等待时间: {}秒",
                    variables.getSegmentId(), elapsedSeconds);
        }

        return shouldForce;
    }

    /**
     * 启动保守清空计时器
     *
     * @param variables 路段变量
     */
    public void startConservativeClearanceTimer(SegmentVariables variables) {
        if (variables.getConservativeTimerStart() == null) {
            variables.setConservativeTimerStart(LocalDateTime.now());
            logger.info("路段 {} 启动保守清空计时器", variables.getSegmentId());
        }
    }

    /**
     * 停止保守清空计时器
     *
     * @param variables 路段变量
     */
    public void stopConservativeClearanceTimer(SegmentVariables variables) {
        if (variables.getConservativeTimerStart() != null) {
            long duration = ChronoUnit.SECONDS.between(
                    variables.getConservativeTimerStart(), LocalDateTime.now());
            variables.setConservativeTimerStart(null);
            logger.debug("路段 {} 停止保守清空计时器 - 持续时间: {}秒",
                    variables.getSegmentId(), duration);
        }
    }

    // ==================== 决策历史和统计 ====================

    /**
     * 更新清空决策历史统计
     *
     * @param decision 决策结果
     * @param variables 路段变量
     */
    public void updateClearanceStatistics(ClearanceDecision decision, SegmentVariables variables) {
        // 这里可以记录决策历史，用于性能分析和优化
        // 例如：统计各种决策的频率，分析决策质量等

        logger.debug("路段 {} 清空决策统计更新 - 决策: {}, 健康度: {}, 拥堵程度: {:.2f}",
                variables.getSegmentId(), decision,
                variables.getSegmentHealthScore(), variables.getCongestionLevel());
    }

    // ==================== 辅助方法 ====================

    /**
     * 记录清空决策的详细日志
     */
    private void logClearanceDecision(String direction, ClearanceDecision decision,
                                      ClearanceContext context, SegmentVariables variables) {
        //logger.debug("路段 {} {} 清空决策: {} - 车辆数: {}, 进入计数: {}, 离开计数: {}, " +
        //                "健康度: {}, 传感器故障率: {:.2f}%, 连续错误: {}",
//                variables.getSegmentId(), direction, decision,
//                context.vehicleIds.size(), context.inCounter, context.outCounter,
//                context.segmentHealthScore, context.sensorFailureRate * 100,
//                context.consecutiveErrors);
    }

    /**
     * 验证清空决策的合理性
     * 用于调试和质量保证
     */
    public boolean validateClearanceDecision(ClearanceDecision decision, SegmentVariables variables) {
        // 检查决策是否符合基本逻辑
        boolean isValid = true;
        String reason = "";

        // 如果车辆ID集合不为空但决策为SAFE，可能存在问题
        if (decision == ClearanceDecision.SAFE) {
            boolean hasVehicles = !variables.getUpstreamVehicleIds().isEmpty() ||
                    !variables.getDownstreamVehicleIds().isEmpty();
            if (hasVehicles) {
                isValid = false;
                reason = "存在车辆但决策为SAFE";
            }
        }

        // 如果健康度过低但决策为SAFE，可能存在问题
        if (decision == ClearanceDecision.SAFE &&
                variables.getSegmentHealthScore() < SegmentConstants.CRITICAL_HEALTH_THRESHOLD) {
            isValid = false;
            reason = "健康度过低但决策为SAFE";
        }

        if (!isValid) {
            logger.warn("路段 {} 清空决策验证失败: {} - 原因: {}",
                    variables.getSegmentId(), decision, reason);
        }

        return isValid;
    }

    // ==================== 内部类：清空上下文 ====================

    /**
     * 清空决策上下文类
     * 包含评估清空决策所需的所有信息
     */
    private static class ClearanceContext {
        SegmentVariables.Direction direction;
        int segmentId;
        Set<String> vehicleIds;
        int inCounter;
        int outCounter;
        int capacity;
        int segmentHealthScore;
        int consecutiveErrors;
        int errorCountMismatch;
        int errorCountIdLogic;
        double sensorFailureRate;
        LocalDateTime lastSwitchTime;
        double congestionLevel;
    }
}
