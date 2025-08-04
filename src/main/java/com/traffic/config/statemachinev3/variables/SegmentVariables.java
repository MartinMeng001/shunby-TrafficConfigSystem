package com.traffic.config.statemachinev3.variables;

import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.constants.SegmentConstants;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 路段变量集合
 * 对应数学模型中的变量集合 V
 *
 * 这个类包含了路段状态机运行时需要的所有变量
 *
 * @author System
 * @version 3.0.0
 */
public class SegmentVariables {

    // ==================== 路段标识 (Segment Identity) ====================

    /**
     * 路段ID
     */
    private final int segmentId;

    // ==================== 时间管理变量 (Time Management Variables) ====================

    /**
     * 绿灯开始时间
     */
    private volatile LocalDateTime greenStartTime;

    /**
     * 红灯开始时间
     */
    private volatile LocalDateTime redStartTime;

    /**
     * 最后切换时间
     */
    private volatile LocalDateTime lastSwitchTime;

    /**
     * 保守清空计时器开始时间
     */
    private volatile LocalDateTime conservativeTimerStart;

    // ==================== 分方向车辆管理变量 (Directional Vehicle Management Variables) ====================

    /**
     * 上行车辆ID集合
     */
    private final Set<String> upstreamVehicleIds = ConcurrentHashMap.newKeySet();

    /**
     * 下行车辆ID集合
     */
    private final Set<String> downstreamVehicleIds = ConcurrentHashMap.newKeySet();

    /**
     * 上行进入计数器
     */
    private final AtomicInteger upstreamInCounter = new AtomicInteger(0);

    /**
     * 上行离开计数器
     */
    private final AtomicInteger upstreamOutCounter = new AtomicInteger(0);

    /**
     * 下行进入计数器
     */
    private final AtomicInteger downstreamInCounter = new AtomicInteger(0);

    /**
     * 下行离开计数器
     */
    private final AtomicInteger downstreamOutCounter = new AtomicInteger(0);

    // ==================== 通行请求管理变量 (Traffic Request Management Variables) ====================

    /**
     * 上行通行请求标志
     */
    private volatile boolean upstreamRequest;

    /**
     * 下行通行请求标志
     */
    private volatile boolean downstreamRequest;

    /**
     * 上行请求产生时间
     */
    private volatile LocalDateTime upstreamRequestTime;

    /**
     * 下行请求产生时间
     */
    private volatile LocalDateTime downstreamRequestTime;

    // ==================== 清空决策相关变量 (Clearance Decision Variables) ====================

    /**
     * 上行清空决策
     */
    private volatile ClearanceDecision upstreamClearanceDecision;

    /**
     * 下行清空决策
     */
    private volatile ClearanceDecision downstreamClearanceDecision;

    /**
     * 综合清空决策
     */
    private volatile ClearanceDecision overallClearanceDecision;

    // ==================== 优先级和调度变量 (Priority and Scheduling Variables) ====================

    /**
     * 最后服务方向
     */
    private volatile Direction lastServedDirection;

    /**
     * 上行等待时间（秒）
     */
    private volatile double upstreamWaitingTime;

    /**
     * 下行等待时间（秒）
     */
    private volatile double downstreamWaitingTime;

    /**
     * 上行优先级得分
     */
    private volatile double priorityScoreUpstream;

    /**
     * 下行优先级得分
     */
    private volatile double priorityScoreDownstream;

    // ==================== 错误统计变量 (Error Statistics Variables) ====================

    /**
     * 计数器不匹配错误次数
     */
    private final AtomicInteger errorCountMismatch = new AtomicInteger(0);

    /**
     * ID逻辑错误次数
     */
    private final AtomicInteger errorCountIdLogic = new AtomicInteger(0);

    /**
     * 错误统计窗口开始时间
     */
    private volatile LocalDateTime errorTimeWindowStart;

    /**
     * 连续错误次数
     */
    private final AtomicInteger consecutiveErrors = new AtomicInteger(0);

    // ==================== 传感器和健康状态变量 (Sensor and Health Status Variables) ====================

    /**
     * 传感器状态映射
     */
    private final Map<String, SensorState> sensorStatus = new ConcurrentHashMap<>();

    /**
     * 路段健康度评分 (0-100)
     */
    private final AtomicInteger segmentHealthScore = new AtomicInteger(100);

    /**
     * 故障检测标志
     */
    private volatile boolean faultDetected;

    /**
     * 最后维护时间
     */
    private volatile LocalDateTime lastMaintenanceTime;

    // ==================== 性能统计变量 (Performance Statistics Variables) ====================

    /**
     * 总服务车辆数
     */
    private final AtomicLong totalVehiclesServed = new AtomicLong(0);

    /**
     * 平均等待时间（秒）
     */
    private volatile double averageWaitingTime;

    /**
     * 通行效率（车辆/秒）
     */
    private volatile double throughputRate;

    /**
     * 拥堵程度 (0.0-1.0)
     */
    private volatile double congestionLevel;

    // ==================== 容量配置变量 (Capacity Configuration Variables) ====================

    /**
     * 上行会车区容量
     */
    private volatile int upstreamCapacity;

    /**
     * 下行会车区容量
     */
    private volatile int downstreamCapacity;

    // ==================== 车辆进入时间记录 (Vehicle Entry Time Records) ====================

    /**
     * 车辆进入时间记录
     */
    private final Map<String, LocalDateTime> vehicleEntryTimes = new ConcurrentHashMap<>();

    // ==================== 枚举定义 ====================

    /**
     * 方向枚举
     */
    public enum Direction {
        UPSTREAM("上行"),
        DOWNSTREAM("下行"),
        NONE("无");

        private final String description;

        Direction(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 传感器状态枚举
     */
    public enum SensorState {
        NORMAL("正常"),
        DEGRADED("降级"),
        FAILED("失败");

        private final String description;

        SensorState(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * @param segmentId 路段ID
     */
    public SegmentVariables(int segmentId) {
        this.segmentId = segmentId;
        initializeVariables();
    }

    /**
     * 初始化所有变量
     */
    private void initializeVariables() {
        LocalDateTime now = LocalDateTime.now();

        // 时间变量初始化
        this.greenStartTime = null;
        this.redStartTime = null;
        this.lastSwitchTime = null;
        this.conservativeTimerStart = null;

        // 通行请求变量初始化
        this.upstreamRequest = false;
        this.downstreamRequest = false;
        this.upstreamRequestTime = null;
        this.downstreamRequestTime = null;

        // 清空决策变量初始化
        this.upstreamClearanceDecision = ClearanceDecision.WAIT;
        this.downstreamClearanceDecision = ClearanceDecision.WAIT;
        this.overallClearanceDecision = ClearanceDecision.WAIT;

        // 优先级和调度变量初始化
        this.lastServedDirection = Direction.NONE;
        this.upstreamWaitingTime = 0.0;
        this.downstreamWaitingTime = 0.0;
        this.priorityScoreUpstream = 0.0;
        this.priorityScoreDownstream = 0.0;

        // 错误统计变量初始化
        this.errorTimeWindowStart = now;

        // 健康状态变量初始化
        this.faultDetected = false;
        this.lastMaintenanceTime = now;

        // 性能统计变量初始化
        this.averageWaitingTime = 0.0;
        this.throughputRate = 0.0;
        this.congestionLevel = 0.0;

        // 容量配置变量初始化
        this.upstreamCapacity = SegmentConstants.DEFAULT_UPSTREAM_CAPACITY;
        this.downstreamCapacity = SegmentConstants.DEFAULT_DOWNSTREAM_CAPACITY;
    }

    // ==================== 时间相关方法 ====================

    /**
     * 开始绿灯计时
     * @param direction 绿灯方向
     */
    public void startGreenTimer(Direction direction) {
        LocalDateTime now = LocalDateTime.now();
        this.greenStartTime = now;
        this.redStartTime = null;
        this.lastSwitchTime = now;
        this.lastServedDirection = direction;
    }

    /**
     * 开始红灯计时
     */
    public void startRedTimer() {
        LocalDateTime now = LocalDateTime.now();
        this.redStartTime = now;
        this.greenStartTime = null;
        this.lastSwitchTime = now;
    }
    /**
     * 重置绿灯计时器
     * 当状态从绿灯状态转换到全红清空状态时调用
     */
    public void resetGreenTimer() {
        this.greenStartTime = null;
        this.lastSwitchTime = LocalDateTime.now();

        // 同时重置相关的优先级计算
        // 因为绿灯结束意味着当前方向的服务结束
        if (lastServedDirection == Direction.UPSTREAM) {
            // 重新计算下行方向的等待时间和优先级
            calculateDownstreamPriority();
        } else if (lastServedDirection == Direction.DOWNSTREAM) {
            // 重新计算上行方向的等待时间和优先级
            calculateUpstreamPriority();
        }
    }
    /**
     * 重置红灯计时器
     * 当状态从全红清空状态转换到绿灯状态时调用
     */
    public void resetRedTimer() {
        this.redStartTime = null;
        this.conservativeTimerStart = null; // 同时重置保守清空计时器
        this.lastSwitchTime = LocalDateTime.now();
    }
    /**
     * 重置所有计时器
     * 用于系统重置或故障恢复时
     */
    public void resetAllTimers() {
        this.greenStartTime = null;
        this.redStartTime = null;
        this.conservativeTimerStart = null;
        this.lastSwitchTime = LocalDateTime.now();

        // 重置所有时间相关的状态
        this.upstreamRequestTime = null;
        this.downstreamRequestTime = null;
        this.upstreamWaitingTime = 0.0;
        this.downstreamWaitingTime = 0.0;
        this.priorityScoreUpstream = 0.0;
        this.priorityScoreDownstream = 0.0;
    }
    /**
     * 重置所有计数器
     */
    public void resetCounters() {
        upstreamInCounter.set(0);
        upstreamOutCounter.set(0);
        downstreamInCounter.set(0);
        downstreamOutCounter.set(0);
        totalVehiclesServed.set(0);
    }
    /**
     * 增加ID逻辑错误次数
     */
    public void incrementErrorCountIdLogic() {
        errorCountIdLogic.incrementAndGet();
    }
    /**
     * 获取当前绿灯持续时间（秒）
     * @return 绿灯持续时间
     */
    public long getCurrentGreenDurationSeconds() {
        if (greenStartTime == null) {
            return 0;
        }
        return java.time.Duration.between(greenStartTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 获取当前红灯持续时间（秒）
     * @return 红灯持续时间
     */
    public long getCurrentRedDurationSeconds() {
        if (redStartTime == null) {
            return 0;
        }
        return java.time.Duration.between(redStartTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 检查绿灯是否超时
     * @return 是否超时
     */
    public boolean isGreenTimeout() {
        return getCurrentGreenDurationSeconds() > SegmentConstants.MAX_GREEN_TIME;
    }

    /**
     * 检查红灯是否超时
     * @return 是否超时
     */
    public boolean isRedTimeout() {
        return getCurrentRedDurationSeconds() > SegmentConstants.MAX_RED_TIME;
    }

    /**
     * 检查绿灯时间是否达到最小值
     * @return 是否达到最小值
     */
    public boolean isMinGreenTimeReached() {
        return getCurrentGreenDurationSeconds() >= SegmentConstants.MIN_GREEN_TIME;
    }
    /**
     * 检查当前是否处于绿灯状态
     * @return 是否处于绿灯状态
     */
    public boolean isInGreenState() {
        return greenStartTime != null && redStartTime == null;
    }

    /**
     * 检查当前是否处于红灯状态
     * @return 是否处于红灯状态
     */
    public boolean isInRedState() {
        return redStartTime != null && greenStartTime == null;
    }

    /**
     * 开始保守清空计时
     */
    public void startConservativeTimer() {
        this.conservativeTimerStart = LocalDateTime.now();
    }

    /**
     * 检查保守清空计时器是否到期
     * @return 是否到期
     */
    public boolean isConservativeTimerExpired() {
        if (conservativeTimerStart == null) {
            return false;
        }
        long duration = java.time.Duration.between(conservativeTimerStart, LocalDateTime.now()).getSeconds();
        return duration >= SegmentConstants.CONSERVATIVE_CLEAR_TIME;
    }

    /**
     * 获取上次状态切换后的时间（秒）
     * @return 状态切换后的时间
     */
    public long getTimeSinceLastSwitch() {
        if (lastSwitchTime == null) {
            return 0;
        }
        return java.time.Duration.between(lastSwitchTime, LocalDateTime.now()).getSeconds();
    }

    // ==================== 车辆管理相关方法 ====================

    /**
     * 添加上行车辆
     * @param vehicleId 车辆ID
     */
    public void addUpstreamVehicle(String vehicleId) {
        upstreamVehicleIds.add(vehicleId);
        upstreamInCounter.incrementAndGet();
        vehicleEntryTimes.put(vehicleId, LocalDateTime.now());
        totalVehiclesServed.incrementAndGet();
        updateClearanceDecisions();
    }

    /**
     * 移除上行车辆
     * @param vehicleId 车辆ID
     */
    public void removeUpstreamVehicle(String vehicleId) {
        if (upstreamVehicleIds.remove(vehicleId)) {
            upstreamOutCounter.incrementAndGet();
            updateVehicleWaitingTime(vehicleId);
            vehicleEntryTimes.remove(vehicleId);
            updateClearanceDecisions();
        }
    }

    /**
     * 添加下行车辆
     * @param vehicleId 车辆ID
     */
    public void addDownstreamVehicle(String vehicleId) {
        downstreamVehicleIds.add(vehicleId);
        downstreamInCounter.incrementAndGet();
        vehicleEntryTimes.put(vehicleId, LocalDateTime.now());
        totalVehiclesServed.incrementAndGet();
        updateClearanceDecisions();
    }

    /**
     * 移除下行车辆
     * @param vehicleId 车辆ID
     */
    public void removeDownstreamVehicle(String vehicleId) {
        if (downstreamVehicleIds.remove(vehicleId)) {
            downstreamOutCounter.incrementAndGet();
            updateVehicleWaitingTime(vehicleId);
            vehicleEntryTimes.remove(vehicleId);
            updateClearanceDecisions();
        }
    }

    /**
     * 更新车辆等待时间统计
     * @param vehicleId 车辆ID
     */
    private void updateVehicleWaitingTime(String vehicleId) {
        LocalDateTime entryTime = vehicleEntryTimes.get(vehicleId);
        if (entryTime != null) {
            long waitingSeconds = java.time.Duration.between(entryTime, LocalDateTime.now()).getSeconds();
            // 使用移动平均更新平均等待时间
            averageWaitingTime = averageWaitingTime * SegmentConstants.WAITING_TIME_SMOOTH_FACTOR +
                    waitingSeconds * (1 - SegmentConstants.WAITING_TIME_SMOOTH_FACTOR);
        }
    }

    /**
     * 检查车辆ID是否存在重复
     * @param vehicleId 车辆ID
     * @return 是否重复
     */
    public boolean isDuplicateVehicleId(String vehicleId) {
        return upstreamVehicleIds.contains(vehicleId) || downstreamVehicleIds.contains(vehicleId);
    }

    /**
     * 获取总车辆数
     * @return 总车辆数
     */
    public int getTotalVehicleCount() {
        return upstreamVehicleIds.size() + downstreamVehicleIds.size();
    }

    /**
     * 检查是否达到容量限制
     * @param direction 方向
     * @return 是否达到容量限制
     */
    public boolean isCapacityReached(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> upstreamVehicleIds.size() >= upstreamCapacity;
            case DOWNSTREAM -> downstreamVehicleIds.size() >= downstreamCapacity;
            case NONE -> getTotalVehicleCount() >= (upstreamCapacity + downstreamCapacity);
        };
    }

    // ==================== 通行请求相关方法 ====================

    /**
     * 生成上行通行请求
     */
    public void generateUpstreamRequest() {
        if (!upstreamRequest && !upstreamVehicleIds.isEmpty()) {
            upstreamRequest = true;
            upstreamRequestTime = LocalDateTime.now();
            calculateUpstreamPriority();
        }
    }

    /**
     * 生成下行通行请求
     */
    public void generateDownstreamRequest() {
        if (!downstreamRequest && !downstreamVehicleIds.isEmpty()) {
            downstreamRequest = true;
            downstreamRequestTime = LocalDateTime.now();
            calculateDownstreamPriority();
        }
    }

    /**
     * 清除上行通行请求
     */
    public void clearUpstreamRequest() {
        upstreamRequest = false;
        upstreamRequestTime = null;
        priorityScoreUpstream = 0.0;
        upstreamWaitingTime = 0.0;
    }

    /**
     * 清除下行通行请求
     */
    public void clearDownstreamRequest() {
        downstreamRequest = false;
        downstreamRequestTime = null;
        priorityScoreDownstream = 0.0;
        downstreamWaitingTime = 0.0;
    }

    /**
     * 计算上行优先级得分
     */
    private void calculateUpstreamPriority() {
        if (upstreamRequestTime != null) {
            upstreamWaitingTime = java.time.Duration.between(upstreamRequestTime, LocalDateTime.now()).getSeconds();
        }

        double timeFactor = Math.min(1.0, upstreamWaitingTime / SegmentConstants.MAX_REASONABLE_WAIT_TIME);
        double loadFactor = (double) upstreamVehicleIds.size() / upstreamCapacity;
        double alternationFactor = (lastServedDirection == Direction.DOWNSTREAM) ? 1.0 : 0.5;

        priorityScoreUpstream = SegmentConstants.DEFAULT_TIME_PRIORITY_WEIGHT * timeFactor +
                SegmentConstants.DEFAULT_LOAD_BALANCE_WEIGHT * loadFactor +
                SegmentConstants.DEFAULT_ALTERNATION_WEIGHT * alternationFactor;

        // 紧急情况加权
        if (upstreamWaitingTime > SegmentConstants.EMERGENCY_OVERRIDE_THRESHOLD) {
            priorityScoreUpstream *= SegmentConstants.EMERGENCY_BOOST_FACTOR;
        }
    }

    /**
     * 计算下行优先级得分
     */
    private void calculateDownstreamPriority() {
        if (downstreamRequestTime != null) {
            downstreamWaitingTime = java.time.Duration.between(downstreamRequestTime, LocalDateTime.now()).getSeconds();
        }

        double timeFactor = Math.min(1.0, downstreamWaitingTime / SegmentConstants.MAX_REASONABLE_WAIT_TIME);
        double loadFactor = (double) downstreamVehicleIds.size() / downstreamCapacity;
        double alternationFactor = (lastServedDirection == Direction.UPSTREAM) ? 1.0 : 0.5;

        priorityScoreDownstream = SegmentConstants.DEFAULT_TIME_PRIORITY_WEIGHT * timeFactor +
                SegmentConstants.DEFAULT_LOAD_BALANCE_WEIGHT * loadFactor +
                SegmentConstants.DEFAULT_ALTERNATION_WEIGHT * alternationFactor;

        // 紧急情况加权
        if (downstreamWaitingTime > SegmentConstants.EMERGENCY_OVERRIDE_THRESHOLD) {
            priorityScoreDownstream *= SegmentConstants.EMERGENCY_BOOST_FACTOR;
        }
    }

    /**
     * 判断优先服务方向
     * @return 优先服务方向
     */
    public Direction determinePriorityDirection() {
        if (upstreamRequest && !downstreamRequest) {
            return Direction.UPSTREAM;
        } else if (!upstreamRequest && downstreamRequest) {
            return Direction.DOWNSTREAM;
        } else if (upstreamRequest && downstreamRequest) {
            // 重新计算优先级得分
            calculateUpstreamPriority();
            calculateDownstreamPriority();

            // 如果得分接近，使用交替策略
            if (Math.abs(priorityScoreUpstream - priorityScoreDownstream) < SegmentConstants.PRIORITY_SCORE_THRESHOLD) {
                return (lastServedDirection == Direction.UPSTREAM) ? Direction.DOWNSTREAM : Direction.UPSTREAM;
            }

            return (priorityScoreUpstream > priorityScoreDownstream) ? Direction.UPSTREAM : Direction.DOWNSTREAM;
        } else {
            return Direction.NONE;
        }
    }

    // ==================== 清空决策相关方法 ====================

    /**
     * 更新清空决策
     */
    private void updateClearanceDecisions() {
        // 计算上行清空决策
        boolean upstreamIdsEmpty = upstreamVehicleIds.isEmpty();
        boolean upstreamCountersBalanced = (upstreamInCounter.get() == upstreamOutCounter.get());
        upstreamClearanceDecision = ClearanceDecision.calculateDecision(upstreamIdsEmpty, upstreamCountersBalanced);

        // 计算下行清空决策
        boolean downstreamIdsEmpty = downstreamVehicleIds.isEmpty();
        boolean downstreamCountersBalanced = (downstreamInCounter.get() == downstreamOutCounter.get());
        downstreamClearanceDecision = ClearanceDecision.calculateDecision(downstreamIdsEmpty, downstreamCountersBalanced);

        // 计算综合清空决策
        overallClearanceDecision = ClearanceDecision.calculateOverallDecision(upstreamClearanceDecision, downstreamClearanceDecision);

        // 如果是保守清空且计时器未启动，则启动计时器
        if (overallClearanceDecision == ClearanceDecision.CONSERVATIVE && conservativeTimerStart == null) {
            startConservativeTimer();
        }
    }

    /**
     * 设置上行清空决策
     * @param decision 清空决策
     */
    public void setUpstreamClearanceDecision(ClearanceDecision decision) {
        this.upstreamClearanceDecision = decision;
        // 设置后重新计算综合决策
        recalculateOverallClearanceDecision();
    }

    /**
     * 设置下行清空决策
     * @param decision 清空决策
     */
    public void setDownstreamClearanceDecision(ClearanceDecision decision) {
        this.downstreamClearanceDecision = decision;
        // 设置后重新计算综合决策
        recalculateOverallClearanceDecision();
    }

    /**
     * 手动设置综合清空决策
     * @param decision 清空决策
     */
    public void setOverallClearanceDecision(ClearanceDecision decision) {
        this.overallClearanceDecision = decision;

        // 如果设置为保守清空且计时器未启动，则启动计时器
        if (decision == ClearanceDecision.CONSERVATIVE && conservativeTimerStart == null) {
            startConservativeTimer();
        }
        // 如果设置为非保守清空，则重置计时器
        else if (decision != ClearanceDecision.CONSERVATIVE && conservativeTimerStart != null) {
            conservativeTimerStart = null;
        }
    }

    /**
     * 同时设置上行和下行清空决策
     * @param upstreamDecision 上行清空决策
     * @param downstreamDecision 下行清空决策
     */
    public void setClearanceDecisions(ClearanceDecision upstreamDecision, ClearanceDecision downstreamDecision) {
        this.upstreamClearanceDecision = upstreamDecision;
        this.downstreamClearanceDecision = downstreamDecision;
        // 重新计算综合决策
        recalculateOverallClearanceDecision();
    }

    /**
     * 重新计算综合清空决策
     * 基于当前的上行和下行清空决策
     */
    private void recalculateOverallClearanceDecision() {
        ClearanceDecision newOverallDecision = ClearanceDecision.calculateOverallDecision(
                upstreamClearanceDecision, downstreamClearanceDecision);

        // 只有当决策发生变化时才更新
        if (overallClearanceDecision != newOverallDecision) {
            ClearanceDecision oldDecision = overallClearanceDecision;
            overallClearanceDecision = newOverallDecision;

            // 处理保守清空计时器
            handleConservativeTimerTransition(oldDecision, newOverallDecision);
        }
    }

    /**
     * 处理保守清空计时器的状态转换
     * @param oldDecision 旧的清空决策
     * @param newDecision 新的清空决策
     */
    private void handleConservativeTimerTransition(ClearanceDecision oldDecision, ClearanceDecision newDecision) {
        // 从非保守状态转换到保守状态 - 启动计时器
        if (oldDecision != ClearanceDecision.CONSERVATIVE &&
                newDecision == ClearanceDecision.CONSERVATIVE &&
                conservativeTimerStart == null) {
            startConservativeTimer();
        }
        // 从保守状态转换到非保守状态 - 重置计时器
        else if (oldDecision == ClearanceDecision.CONSERVATIVE &&
                newDecision != ClearanceDecision.CONSERVATIVE &&
                conservativeTimerStart != null) {
            conservativeTimerStart = null;
        }
    }

    /**
     * 强制设置所有清空决策为安全状态
     * 通常在强制清空或系统重置时使用
     */
    public void forceSetAllDecisionsToSafe() {
        this.upstreamClearanceDecision = ClearanceDecision.SAFE;
        this.downstreamClearanceDecision = ClearanceDecision.SAFE;
        this.overallClearanceDecision = ClearanceDecision.SAFE;
        this.conservativeTimerStart = null;
    }

    /**
     * 强制设置所有清空决策为等待状态
     * 通常在故障检测或紧急情况时使用
     */
    public void forceSetAllDecisionsToWait() {
        this.upstreamClearanceDecision = ClearanceDecision.WAIT;
        this.downstreamClearanceDecision = ClearanceDecision.WAIT;
        this.overallClearanceDecision = ClearanceDecision.WAIT;
        this.conservativeTimerStart = null;
    }

    /**
     * 根据方向设置特定方向的清空决策
     * @param direction 方向
     * @param decision 清空决策
     */
    public void setClearanceDecisionByDirection(Direction direction, ClearanceDecision decision) {
        switch (direction) {
            case UPSTREAM:
                setUpstreamClearanceDecision(decision);
                break;
            case DOWNSTREAM:
                setDownstreamClearanceDecision(decision);
                break;
            case NONE:
                // 对于NONE方向，不做任何操作或抛出异常
                throw new IllegalArgumentException("Cannot set clearance decision for NONE direction");
        }
    }

    /**
     * 获取指定方向的清空决策
     * @param direction 方向
     * @return 对应方向的清空决策
     */
    public ClearanceDecision getClearanceDecisionByDirection(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> upstreamClearanceDecision;
            case DOWNSTREAM -> downstreamClearanceDecision;
            case NONE -> overallClearanceDecision; // 对于NONE，返回综合决策
        };
    }
    /**
     * 强制清空路段
     */
    public void forceClearSegment() {
        upstreamVehicleIds.clear();
        downstreamVehicleIds.clear();
        vehicleEntryTimes.clear();
        conservativeTimerStart = null;

        // 更新清空决策
        upstreamClearanceDecision = ClearanceDecision.SAFE;
        downstreamClearanceDecision = ClearanceDecision.SAFE;
        overallClearanceDecision = ClearanceDecision.SAFE;

        // 降低健康度评分
        segmentHealthScore.addAndGet(-10);
        if (segmentHealthScore.get() < 0) {
            segmentHealthScore.set(0);
        }
    }
    /**
     * 检查清空决策是否允许状态转换
     * @return 是否允许状态转换
     */
    public boolean isClearanceDecisionAllowingTransition() {
        return overallClearanceDecision != null && overallClearanceDecision.isSafeForTransition();
    }

    /**
     * 检查是否需要保守清空处理
     * @return 是否需要保守清空处理
     */
    public boolean requiresConservativeClearance() {
        return overallClearanceDecision != null && overallClearanceDecision.requiresConservativeHandling();
    }

    /**
     * 检查是否需要等待清空
     * @return 是否需要等待清空
     */
    public boolean requiresWaitingForClearance() {
        return overallClearanceDecision != null && overallClearanceDecision.requiresWaiting();
    }
    /**
     * 获取清空决策的详细状态信息
     * @return 清空决策状态信息
     */
    public String getClearanceDecisionStatus() {
        return String.format(
                "清空决策状态 - 上行: %s, 下行: %s, 综合: %s, 保守计时器: %s",
                upstreamClearanceDecision != null ? upstreamClearanceDecision.getChineseName() : "未设置",
                downstreamClearanceDecision != null ? downstreamClearanceDecision.getChineseName() : "未设置",
                overallClearanceDecision != null ? overallClearanceDecision.getChineseName() : "未设置",
                conservativeTimerStart != null ? "已启动" : "未启动"
        );
    }
    // ==================== 错误处理相关方法 ====================

    /**
     * 记录计数器不匹配错误
     */
    public void recordCounterMismatchError() {
        errorCountMismatch.incrementAndGet();
        consecutiveErrors.incrementAndGet();
        updateErrorWindow();
        updateHealthScore(-5);
    }

    /**
     * 记录ID逻辑错误
     * @param vehicleId 相关车辆ID
     * @param errorType 错误类型
     */
    public void recordIdLogicError(String vehicleId, String errorType) {
        errorCountIdLogic.incrementAndGet();
        consecutiveErrors.incrementAndGet();
        updateErrorWindow();
        updateHealthScore(-3);
    }

    /**
     * 更新错误统计窗口
     */
    private void updateErrorWindow() {
        LocalDateTime now = LocalDateTime.now();
        if (errorTimeWindowStart == null ||
                java.time.Duration.between(errorTimeWindowStart, now).getSeconds() > SegmentConstants.PERFORMANCE_WINDOW) {
            errorTimeWindowStart = now;
            // 重置错误计数（简化实现，实际应该基于时间窗口）
            if (consecutiveErrors.get() > SegmentConstants.MAX_CONSECUTIVE_ERRORS) {
                faultDetected = true;
            }
        }
    }

    /**
     * 检查是否应该报告故障
     * @return 是否应该报告故障
     */
    public boolean shouldReportFault() {
        return errorCountMismatch.get() >= SegmentConstants.MAX_COUNTER_MISMATCH_ERRORS ||
                errorCountIdLogic.get() >= SegmentConstants.MAX_ID_LOGIC_ERRORS ||
                consecutiveErrors.get() >= SegmentConstants.MAX_CONSECUTIVE_ERRORS ||
                segmentHealthScore.get() < 30; // 健康度阈值
    }

    /**
     * 重置错误计数器
     */
    public void resetErrorCounters() {
        errorCountMismatch.set(0);
        errorCountIdLogic.set(0);
        consecutiveErrors.set(0);
        faultDetected = false;
        errorTimeWindowStart = LocalDateTime.now();
    }
    /**
     * 设置错误统计窗口开始时间
     * @param errorTimeWindowStart 错误统计窗口开始时间
     */
    public void setErrorTimeWindowStart(LocalDateTime errorTimeWindowStart) {
        this.errorTimeWindowStart = errorTimeWindowStart;
    }

    // ==================== 传感器管理相关方法 ====================

    /**
     * 更新传感器状态
     * @param sensorId 传感器ID
     * @param state 传感器状态
     */
    public void updateSensorStatus(String sensorId, SensorState state) {
        sensorStatus.put(sensorId, state);

        // 检查传感器故障对系统健康度的影响
        long failedSensors = sensorStatus.values().stream()
                .filter(s -> s == SensorState.FAILED)
                .count();

        if (failedSensors > 0) {
            updateHealthScore(-((int) failedSensors * 5));
        }
    }

    /**
     * 检查关键传感器是否正常
     * @return 关键传感器是否正常
     */
    public boolean areCriticalSensorsNormal() {
        return sensorStatus.values().stream()
                .noneMatch(state -> state == SensorState.FAILED);
    }
// ==================== 车辆时间记录方法补充 ====================

    /**
     * 记录车辆进入时间
     * @param vehicleId 车辆ID
     */
    public void recordVehicleEntryTime(String vehicleId) {
        recordVehicleEntryTime(vehicleId, LocalDateTime.now());
    }

    /**
     * 记录车辆进入时间（指定时间）
     * @param vehicleId 车辆ID
     * @param entryTime 进入时间
     */
    public void recordVehicleEntryTime(String vehicleId, LocalDateTime entryTime) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or empty");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null");
        }

        vehicleEntryTimes.put(vehicleId, entryTime);
        logVehicleTimeEvent("记录车辆进入时间", vehicleId, entryTime);
    }

    /**
     * 移除车辆进入时间记录
     * @param vehicleId 车辆ID
     * @return 被移除的进入时间，如果不存在则返回null
     */
    public LocalDateTime removeVehicleEntryTime(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return null;
        }

        LocalDateTime removedTime = vehicleEntryTimes.remove(vehicleId);
        if (removedTime != null) {
            logVehicleTimeEvent("移除车辆进入时间记录", vehicleId, removedTime);
        }

        return removedTime;
    }

    /**
     * 获取车辆进入时间
     * @param vehicleId 车辆ID
     * @return 车辆进入时间，如果不存在则返回null
     */
    public LocalDateTime getVehicleEntryTime(String vehicleId) {
        return vehicleEntryTimes.get(vehicleId);
    }

    /**
     * 检查车辆是否已记录进入时间
     * @param vehicleId 车辆ID
     * @return 是否已记录
     */
    public boolean hasVehicleEntryTime(String vehicleId) {
        return vehicleEntryTimes.containsKey(vehicleId);
    }

    /**
     * 计算车辆在路段的停留时间（秒）
     * @param vehicleId 车辆ID
     * @return 停留时间，如果车辆不存在则返回0
     */
    public long calculateVehicleStayTime(String vehicleId) {
        LocalDateTime entryTime = vehicleEntryTimes.get(vehicleId);
        if (entryTime == null) {
            return 0;
        }

        return java.time.Duration.between(entryTime, LocalDateTime.now()).getSeconds();
    }
    /**
     * 增加上行进入计数器
     */
    public void incrementUpstreamInCounter() {
        upstreamInCounter.incrementAndGet();
    }
    /**
     * 增加上行离开计数器
     */
    public void incrementUpstreamOutCounter() {
        upstreamOutCounter.incrementAndGet();
    }
    /**
     * 增加下行进入计数器
     */
    public void incrementDownstreamInCounter() {
        downstreamInCounter.incrementAndGet();
    }

    /**
     * 增加下行离开计数器
     */
    public void incrementDownstreamOutCounter() {
        downstreamOutCounter.incrementAndGet();
    }
    /**
     * 计算车辆在路段的停留时间（指定结束时间）
     * @param vehicleId 车辆ID
     * @param exitTime 离开时间
     * @return 停留时间，如果车辆不存在则返回0
     */
    public long calculateVehicleStayTime(String vehicleId, LocalDateTime exitTime) {
        LocalDateTime entryTime = vehicleEntryTimes.get(vehicleId);
        if (entryTime == null || exitTime == null) {
            return 0;
        }

        return java.time.Duration.between(entryTime, exitTime).getSeconds();
    }

    /**
     * 获取所有车辆的停留时间统计
     * @return 车辆ID到停留时间的映射
     */
    public Map<String, Long> getAllVehicleStayTimes() {
        Map<String, Long> stayTimes = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, LocalDateTime> entry : vehicleEntryTimes.entrySet()) {
            String vehicleId = entry.getKey();
            LocalDateTime entryTime = entry.getValue();
            long stayTime = java.time.Duration.between(entryTime, now).getSeconds();
            stayTimes.put(vehicleId, stayTime);
        }

        return stayTimes;
    }

    /**
     * 获取平均停留时间（秒）
     * @return 平均停留时间
     */
    public double getAverageStayTime() {
        if (vehicleEntryTimes.isEmpty()) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        long totalStayTime = 0;

        for (LocalDateTime entryTime : vehicleEntryTimes.values()) {
            totalStayTime += java.time.Duration.between(entryTime, now).getSeconds();
        }

        return (double) totalStayTime / vehicleEntryTimes.size();
    }

    /**
     * 获取最长停留时间的车辆
     * @return 车辆ID，如果没有车辆则返回null
     */
    public String getLongestStayVehicle() {
        if (vehicleEntryTimes.isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        String longestStayVehicle = null;
        long maxStayTime = 0;

        for (Map.Entry<String, LocalDateTime> entry : vehicleEntryTimes.entrySet()) {
            long stayTime = java.time.Duration.between(entry.getValue(), now).getSeconds();
            if (stayTime > maxStayTime) {
                maxStayTime = stayTime;
                longestStayVehicle = entry.getKey();
            }
        }

        return longestStayVehicle;
    }

    /**
     * 获取停留时间超过阈值的车辆列表
     * @param thresholdSeconds 阈值（秒）
     * @return 超时车辆ID列表
     */
    public List<String> getVehiclesExceedingStayTime(long thresholdSeconds) {
        List<String> exceedingVehicles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, LocalDateTime> entry : vehicleEntryTimes.entrySet()) {
            long stayTime = java.time.Duration.between(entry.getValue(), now).getSeconds();
            if (stayTime > thresholdSeconds) {
                exceedingVehicles.add(entry.getKey());
            }
        }

        return exceedingVehicles;
    }

    /**
     * 清理超时的车辆进入时间记录
     * @param timeoutSeconds 超时阈值（秒）
     * @return 被清理的车辆数量
     */
    public int cleanupTimeoutVehicleRecords(long timeoutSeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusSeconds(timeoutSeconds);

        List<String> vehiclesToRemove = new ArrayList<>();

        for (Map.Entry<String, LocalDateTime> entry : vehicleEntryTimes.entrySet()) {
            if (entry.getValue().isBefore(cutoffTime)) {
                vehiclesToRemove.add(entry.getKey());
            }
        }

        for (String vehicleId : vehiclesToRemove) {
            removeVehicleEntryTime(vehicleId);
            logVehicleTimeEvent("清理超时车辆记录", vehicleId, vehicleEntryTimes.get(vehicleId));
        }

        return vehiclesToRemove.size();
    }

    /**
     * 批量记录车辆进入时间
     * @param vehicleEntries 车辆ID到进入时间的映射
     */
    public void recordVehicleEntryTimes(Map<String, LocalDateTime> vehicleEntries) {
        if (vehicleEntries == null || vehicleEntries.isEmpty()) {
            return;
        }

        for (Map.Entry<String, LocalDateTime> entry : vehicleEntries.entrySet()) {
            recordVehicleEntryTime(entry.getKey(), entry.getValue());
        }

        logBatchVehicleTimeEvent("批量记录车辆进入时间", vehicleEntries.size());
    }

    /**
     * 清空所有车辆进入时间记录
     * @return 被清空的记录数量
     */
    public int clearAllVehicleEntryTimes() {
        int count = vehicleEntryTimes.size();
        vehicleEntryTimes.clear();

        logBatchVehicleTimeEvent("清空所有车辆进入时间记录", count);

        return count;
    }

    /**
     * 更新车辆的等待时间统计（改进版）
     * @param vehicleId 车辆ID
     * @param exitTime 离开时间（可选，为null时使用当前时间）
     */
    public void updateVehicleWaitingTimeStatistics(String vehicleId, LocalDateTime exitTime) {
        LocalDateTime entryTime = vehicleEntryTimes.get(vehicleId);
        if (entryTime == null) {
            return;
        }

        LocalDateTime actualExitTime = (exitTime != null) ? exitTime : LocalDateTime.now();
        long waitingSeconds = java.time.Duration.between(entryTime, actualExitTime).getSeconds();

        // 使用移动平均更新平均等待时间
        double smoothingFactor = SegmentConstants.WAITING_TIME_SMOOTH_FACTOR;
        averageWaitingTime = averageWaitingTime * smoothingFactor +
                waitingSeconds * (1 - smoothingFactor);

        logVehicleWaitingTime(vehicleId, waitingSeconds, averageWaitingTime);
    }

    /**
     * 获取车辆进入时间记录统计信息
     * @return 统计信息字符串
     */
    public String getVehicleTimeStatistics() {
        int totalVehicles = vehicleEntryTimes.size();
        double avgStayTime = getAverageStayTime();
        String longestStayVehicle = getLongestStayVehicle();
        long longestStayTime = 0;

        if (longestStayVehicle != null) {
            longestStayTime = calculateVehicleStayTime(longestStayVehicle);
        }

        return String.format(
                "车辆时间统计 - 总车辆数: %d, 平均停留时间: %.1f秒, 最长停留车辆: %s(%.1f秒)",
                totalVehicles, avgStayTime,
                longestStayVehicle != null ? longestStayVehicle : "无",
                (double) longestStayTime
        );
    }

    /**
     * 检查是否有长时间停留的车辆
     * @param thresholdSeconds 阈值（秒）
     * @return 是否有长时间停留的车辆
     */
    public boolean hasLongStayVehicles(long thresholdSeconds) {
        return !getVehiclesExceedingStayTime(thresholdSeconds).isEmpty();
    }

    /**
     * 记录车辆时间事件日志
     * @param eventType 事件类型
     * @param vehicleId 车辆ID
     * @param time 时间
     */
    private void logVehicleTimeEvent(String eventType, String vehicleId, LocalDateTime time) {
        // 这里可以添加具体的日志记录逻辑
        // System.out.printf("路段%d %s: 车辆%s, 时间: %s%n",
        //     segmentId, eventType, vehicleId, time);
    }

    /**
     * 记录批量车辆时间事件日志
     * @param eventType 事件类型
     * @param count 数量
     */
    private void logBatchVehicleTimeEvent(String eventType, int count) {
        // 这里可以添加具体的日志记录逻辑
        // System.out.printf("路段%d %s: 数量%d, 时间: %s%n",
        //     segmentId, eventType, count, LocalDateTime.now());
    }

    /**
     * 记录车辆等待时间日志
     * @param vehicleId 车辆ID
     * @param waitingTime 等待时间
     * @param avgWaitingTime 平均等待时间
     */
    private void logVehicleWaitingTime(String vehicleId, long waitingTime, double avgWaitingTime) {
        // 这里可以添加具体的日志记录逻辑
        // System.out.printf("路段%d 车辆%s等待时间: %d秒, 平均等待时间: %.1f秒%n",
        //     segmentId, vehicleId, waitingTime, avgWaitingTime);
    }

// ==================== 与现有方法的集成优化 ====================

    /**
     * 优化的添加上行车辆方法（使用新的时间记录方法）
     * @param vehicleId 车辆ID
     * @param entryTime 进入时间（可选，为null时使用当前时间）
     */
    public void addUpstreamVehicleWithTime(String vehicleId, LocalDateTime entryTime) {
        upstreamVehicleIds.add(vehicleId);
        upstreamInCounter.incrementAndGet();

        // 使用新的时间记录方法
        recordVehicleEntryTime(vehicleId, entryTime != null ? entryTime : LocalDateTime.now());

        totalVehiclesServed.incrementAndGet();
        updateClearanceDecisions();
    }

    /**
     * 优化的添加下行车辆方法（使用新的时间记录方法）
     * @param vehicleId 车辆ID
     * @param entryTime 进入时间（可选，为null时使用当前时间）
     */
    public void addDownstreamVehicleWithTime(String vehicleId, LocalDateTime entryTime) {
        downstreamVehicleIds.add(vehicleId);
        downstreamInCounter.incrementAndGet();

        // 使用新的时间记录方法
        recordVehicleEntryTime(vehicleId, entryTime != null ? entryTime : LocalDateTime.now());

        totalVehiclesServed.incrementAndGet();
        updateClearanceDecisions();
    }

    /**
     * 优化的移除上行车辆方法（使用新的时间记录方法）
     * @param vehicleId 车辆ID
     * @param exitTime 离开时间（可选，为null时使用当前时间）
     */
    public void removeUpstreamVehicleWithTime(String vehicleId, LocalDateTime exitTime) {
        if (upstreamVehicleIds.remove(vehicleId)) {
            upstreamOutCounter.incrementAndGet();

            // 使用新的等待时间统计方法
            updateVehicleWaitingTimeStatistics(vehicleId, exitTime);

            // 移除时间记录
            removeVehicleEntryTime(vehicleId);

            updateClearanceDecisions();
        }
    }

    /**
     * 优化的移除下行车辆方法（使用新的时间记录方法）
     * @param vehicleId 车辆ID
     * @param exitTime 离开时间（可选，为null时使用当前时间）
     */
    public void removeDownstreamVehicleWithTime(String vehicleId, LocalDateTime exitTime) {
        if (downstreamVehicleIds.remove(vehicleId)) {
            downstreamOutCounter.incrementAndGet();

            // 使用新的等待时间统计方法
            updateVehicleWaitingTimeStatistics(vehicleId, exitTime);

            // 移除时间记录
            removeVehicleEntryTime(vehicleId);

            updateClearanceDecisions();
        }
    }
    // ==================== 总服务车辆数管理方法补充 ====================

    /**
     * 增加总服务车辆数
     * 对应数学模型中的 total_vehicles_served 变量
     */
    public void incrementTotalVehiclesServed() {
        long newCount = totalVehiclesServed.incrementAndGet();
        updateThroughputRate();
        logVehicleServiceEvent("增加服务车辆计数", 1, newCount);
    }

    /**
     * 批量增加总服务车辆数
     * @param count 增加的数量
     */
    public void incrementTotalVehiclesServed(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Vehicle count increment must be positive");
        }

        long newCount = totalVehiclesServed.addAndGet(count);
        updateThroughputRate();
        logVehicleServiceEvent("批量增加服务车辆计数", count, newCount);
    }

    /**
     * 减少总服务车辆数（用于纠错）
     * @param count 减少的数量
     */
    public void decrementTotalVehiclesServed(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Vehicle count decrement must be positive");
        }

        long currentCount = totalVehiclesServed.get();
        long newCount = Math.max(0, currentCount - count);
        totalVehiclesServed.set(newCount);
        updateThroughputRate();
        logVehicleServiceEvent("减少服务车辆计数", -count, newCount);
    }

    /**
     * 设置总服务车辆数
     * @param count 新的总数
     */
    public void setTotalVehiclesServed(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Total vehicles served cannot be negative");
        }

        long oldCount = totalVehiclesServed.get();
        totalVehiclesServed.set(count);
        updateThroughputRate();
        logVehicleServiceEvent("设置服务车辆总数", (int)(count - oldCount), count);
    }

    /**
     * 重置总服务车辆数
     */
    public void resetTotalVehiclesServed() {
        long oldCount = totalVehiclesServed.get();
        totalVehiclesServed.set(0);
        throughputRate = 0.0;
        logVehicleServiceEvent("重置服务车辆计数", (int)(-oldCount), 0);
    }



// ==================== 通行效率管理方法 ====================

    /**
     * 更新通行效率
     * 基于服务车辆数和时间窗口计算
     */
    public void updateThroughputRate() {
        if (lastSwitchTime != null) {
            long timeWindow = java.time.Duration.between(lastSwitchTime, LocalDateTime.now()).getSeconds();
            if (timeWindow > 0) {
                throughputRate = (double) totalVehiclesServed.get() / timeWindow;
            }
        }
    }

    /**
     * 设置通行效率
     * @param rate 通行效率（车辆/秒）
     */
    public void setThroughputRate(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Throughput rate cannot be negative");
        }

        this.throughputRate = rate;
        logPerformanceMetricChange("通行效率", rate);
    }


    /**
     * 计算指定时间窗口的通行效率
     * @param windowStartTime 窗口开始时间
     * @param windowEndTime 窗口结束时间
     * @param vehicleCount 时间窗口内的车辆数
     * @return 通行效率
     */
    public double calculateThroughputRate(LocalDateTime windowStartTime,
                                          LocalDateTime windowEndTime,
                                          long vehicleCount) {
        if (windowStartTime == null || windowEndTime == null) {
            return 0.0;
        }

        long timeWindowSeconds = java.time.Duration.between(windowStartTime, windowEndTime).getSeconds();
        if (timeWindowSeconds <= 0) {
            return 0.0;
        }

        return (double) vehicleCount / timeWindowSeconds;
    }

// ==================== 平均等待时间管理方法 ====================

    /**
     * 设置平均等待时间
     * @param waitingTime 平均等待时间（秒）
     */
    public void setAverageWaitingTime(double waitingTime) {
        if (waitingTime < 0) {
            throw new IllegalArgumentException("Average waiting time cannot be negative");
        }

        this.averageWaitingTime = waitingTime;
        logPerformanceMetricChange("平均等待时间", waitingTime);
    }

    /**
     * 更新平均等待时间（使用移动平均）
     * @param newWaitingTime 新的等待时间
     */
    public void updateAverageWaitingTime(double newWaitingTime) {
        if (newWaitingTime < 0) {
            return; // 忽略负值
        }

        double smoothingFactor = SegmentConstants.WAITING_TIME_SMOOTH_FACTOR;
        this.averageWaitingTime = this.averageWaitingTime * smoothingFactor +
                newWaitingTime * (1 - smoothingFactor);

        logPerformanceMetricChange("平均等待时间更新", this.averageWaitingTime);
    }

    /**
     * 重置平均等待时间
     */
    public void resetAverageWaitingTime() {
        this.averageWaitingTime = 0.0;
        logPerformanceMetricChange("平均等待时间重置", 0.0);
    }

// ==================== 拥堵程度管理方法 ====================

    /**
     * 设置拥堵程度
     * @param level 拥堵程度 (0.0-1.0)
     */
    public void setCongestionLevel(double level) {
        if (level < 0.0 || level > 1.0) {
            throw new IllegalArgumentException("Congestion level must be between 0.0 and 1.0");
        }

        this.congestionLevel = level;

        // 高拥堵时影响健康度
        if (level > SegmentConstants.HIGH_CONGESTION_THRESHOLD) {
            decreaseHealthScore(2);
        }

        logPerformanceMetricChange("拥堵程度", level);
    }

    /**
     * 更新拥堵程度
     * 基于当前车辆数和容量计算
     */
    public void updateCongestionLevel() {
        int totalCapacity = upstreamCapacity + downstreamCapacity;
        if (totalCapacity > 0) {
            double newLevel = (double) getTotalVehicleCount() / totalCapacity;
            setCongestionLevel(Math.min(1.0, newLevel));
        }
    }


    /**
     * 检查是否高度拥堵
     * @return 是否高度拥堵
     */
    public boolean isHighlyCongested() {
        return congestionLevel > SegmentConstants.HIGH_CONGESTION_THRESHOLD;
    }

    /**
     * 获取拥堵程度描述
     * @return 拥堵程度描述
     */
    public String getCongestionDescription() {
        if (congestionLevel <= 0.2) {
            return "畅通";
        } else if (congestionLevel <= 0.4) {
            return "轻微拥堵";
        } else if (congestionLevel <= 0.6) {
            return "中度拥堵";
        } else if (congestionLevel <= 0.8) {
            return "严重拥堵";
        } else {
            return "极度拥堵";
        }
    }

// ==================== 综合性能统计方法 ====================

    /**
     * 更新所有性能统计指标
     */
    public void updateAllPerformanceStatistics() {
        updateThroughputRate();
        updateCongestionLevel();

        // 综合统计日志
        logComprehensivePerformanceUpdate();
    }

    /**
     * 重置所有性能统计指标
     */
    public void resetAllPerformanceStatistics() {
        resetTotalVehiclesServed();
        resetAverageWaitingTime();
        throughputRate = 0.0;
        congestionLevel = 0.0;

        logPerformanceMetricChange("所有性能统计重置", 0.0);
    }

    /**
     * 获取性能统计摘要
     * @return 性能统计摘要字符串
     */
    public String getPerformanceStatisticsSummary() {
        return String.format(
                "性能统计 - 总服务车辆: %d, 通行效率: %.3f车辆/秒, 平均等待: %.1f秒, 拥堵程度: %.1f%%(%s)",
                totalVehiclesServed.get(),
                throughputRate,
                averageWaitingTime,
                congestionLevel * 100,
                getCongestionDescription()
        );
    }

    /**
     * 计算性能评分
     * @return 性能评分 (0-100)
     */
    public int calculatePerformanceScore() {
        int baseScore = 100;

        // 基于通行效率的评分（效率越高分数越高）
        int efficiencyScore = (int) Math.min(20, throughputRate * 100);

        // 基于等待时间的评分（等待时间越短分数越高）
        int waitingScore = Math.max(0, 30 - (int)(averageWaitingTime / 10));

        // 基于拥堵程度的评分（拥堵程度越低分数越高）
        int congestionScore = (int) (30 * (1 - congestionLevel));

        // 基于健康度的评分
        int healthScore = (int) (segmentHealthScore.get() * 0.2);

        return Math.min(100, efficiencyScore + waitingScore + congestionScore + healthScore);
    }

    /**
     * 检查性能是否良好
     * @return 性能是否良好
     */
    public boolean isPerformanceGood() {
        return calculatePerformanceScore() >= 70 &&
                !isHighlyCongested() &&
                averageWaitingTime < 120; // 2分钟
    }

// ==================== 日志记录方法 ====================

    /**
     * 记录车辆服务事件
     * @param eventType 事件类型
     * @param delta 变化量
     * @param newTotal 新的总数
     */
    private void logVehicleServiceEvent(String eventType, int delta, long newTotal) {
        // 这里可以添加具体的日志记录逻辑
        // System.out.printf("路段%d %s: 变化%+d, 总计: %d, 时间: %s%n",
        //     segmentId, eventType, delta, newTotal, LocalDateTime.now());
    }

    /**
     * 记录性能指标变化
     * @param metricName 指标名称
     * @param value 新值
     */
    private void logPerformanceMetricChange(String metricName, double value) {
        // 这里可以添加具体的日志记录逻辑
        // System.out.printf("路段%d %s: %.3f, 时间: %s%n",
        //     segmentId, metricName, value, LocalDateTime.now());
    }

    /**
     * 记录综合性能更新
     */
    private void logComprehensivePerformanceUpdate() {
        // 这里可以添加具体的日志记录逻辑
        // System.out.printf("路段%d 性能统计更新: %s%n",
        //     segmentId, getPerformanceStatisticsSummary());
    }

// ==================== 与现有方法的集成优化 ====================

    /**
     * 优化的添加上行车辆方法（使用新的计数方法）
     * @param vehicleId 车辆ID
     */
    public void addUpstreamVehicleOptimized(String vehicleId) {
        upstreamVehicleIds.add(vehicleId);
        upstreamInCounter.incrementAndGet();
        recordVehicleEntryTime(vehicleId);

        // 使用新的增加方法
        incrementTotalVehiclesServed();

        updateClearanceDecisions();
    }

    /**
     * 优化的添加下行车辆方法（使用新的计数方法）
     * @param vehicleId 车辆ID
     */
    public void addDownstreamVehicleOptimized(String vehicleId) {
        downstreamVehicleIds.add(vehicleId);
        downstreamInCounter.incrementAndGet();
        recordVehicleEntryTime(vehicleId);

        // 使用新的增加方法
        incrementTotalVehiclesServed();

        updateClearanceDecisions();
    }
    // ==================== 性能统计相关方法 ====================

    /**
     * 更新性能统计
     */
    public void updatePerformanceStatistics() {
        LocalDateTime now = LocalDateTime.now();

        // 更新通行效率
        if (lastSwitchTime != null) {
            long timeWindow = java.time.Duration.between(lastSwitchTime, now).getSeconds();
            if (timeWindow > 0) {
                throughputRate = (double) totalVehiclesServed.get() / timeWindow;
            }
        }

        // 更新拥堵程度
        int totalCapacity = upstreamCapacity + downstreamCapacity;
        congestionLevel = (double) getTotalVehicleCount() / totalCapacity;

        // 如果拥堵程度过高，影响健康度
        if (congestionLevel > SegmentConstants.HIGH_CONGESTION_THRESHOLD) {
            updateHealthScore(-2);
        }
    }

    /**
     * 更新健康度评分
     * @param delta 变化量
     */
    private void updateHealthScore(int delta) {
        int currentScore = segmentHealthScore.get();
        int newScore = Math.max(0, Math.min(100, currentScore + delta));
        segmentHealthScore.set(newScore);
    }

    /**
     * 降低健康度评分
     * @param amount 降低的数值
     */
    public void decreaseHealthScore(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Health score decrease amount must be positive");
        }

        int currentScore = segmentHealthScore.get();
        int newScore = Math.max(0, currentScore - amount);
        segmentHealthScore.set(newScore);

        // 记录健康度变化
        logHealthScoreChange(currentScore, newScore, -amount, "健康度降低");

        // 如果健康度过低，设置故障标志
        if (newScore < SegmentConstants.CRITICAL_HEALTH_THRESHOLD) {
            faultDetected = true;
        }
    }
    /**
     * 提高健康度评分
     * @param amount 提高的数值
     */
    public void increaseHealthScore(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Health score increase amount must be positive");
        }

        int currentScore = segmentHealthScore.get();
        int newScore = Math.min(100, currentScore + amount);
        segmentHealthScore.set(newScore);

        // 记录健康度变化
        logHealthScoreChange(currentScore, newScore, amount, "健康度提高");

        // 如果健康度恢复到安全水平，可能清除故障标志
        if (newScore >= SegmentConstants.RECOVERY_HEALTH_THRESHOLD && faultDetected) {
            // 只有在其他条件也满足时才清除故障标志
            checkAndClearFaultFlag();
        }
    }

    /**
     * 设置健康度评分
     * @param score 新的健康度评分 (0-100)
     */
    public void setHealthScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Health score must be between 0 and 100");
        }

        int currentScore = segmentHealthScore.get();
        segmentHealthScore.set(score);

        // 记录健康度变化
        logHealthScoreChange(currentScore, score, score - currentScore, "健康度设置");

        // 根据新的健康度判断故障状态
        if (score < SegmentConstants.CRITICAL_HEALTH_THRESHOLD) {
            faultDetected = true;
        } else if (score >= SegmentConstants.RECOVERY_HEALTH_THRESHOLD) {
            checkAndClearFaultFlag();
        }
    }

    /**
     * 重置健康度到满分
     */
    public void resetHealthScore() {
        int currentScore = segmentHealthScore.get();
        segmentHealthScore.set(100);

        // 记录健康度变化
        logHealthScoreChange(currentScore, 100, 100 - currentScore, "健康度重置");

        // 清除故障标志
        checkAndClearFaultFlag();
    }

    /**
     * 根据错误类型降低健康度
     * @param errorType 错误类型
     */
    public void decreaseHealthScoreByErrorType(String errorType) {
        int decreaseAmount = switch (errorType.toLowerCase()) {
            case "counter_mismatch", "计数器不匹配" -> 5;
            case "id_logic_error", "id逻辑错误" -> 3;
            case "sensor_failure", "传感器故障" -> 10;
            case "clearance_timeout", "清空超时" -> 8;
            case "communication_error", "通信错误" -> 6;
            case "data_inconsistency", "数据不一致" -> 4;
            default -> 2; // 默认轻微错误
        };

        decreaseHealthScore(decreaseAmount);
    }

    /**
     * 检查并清除故障标志
     */
    private void checkAndClearFaultFlag() {
        // 只有当健康度足够高且错误次数较少时才清除故障标志
        if (segmentHealthScore.get() >= SegmentConstants.RECOVERY_HEALTH_THRESHOLD &&
                consecutiveErrors.get() < SegmentConstants.MAX_CONSECUTIVE_ERRORS / 2 &&
                errorCountMismatch.get() < SegmentConstants.MAX_COUNTER_MISMATCH_ERRORS / 2 &&
                errorCountIdLogic.get() < SegmentConstants.MAX_ID_LOGIC_ERRORS / 2) {
            faultDetected = false;
        }
    }

    /**
     * 记录健康度变化
     * @param oldScore 旧分数
     * @param newScore 新分数
     * @param delta 变化量
     * @param reason 变化原因
     */
    private void logHealthScoreChange(int oldScore, int newScore, int delta, String reason) {
        // 这里可以添加日志记录逻辑
        // System.out.printf("路段%d健康度变化: %d -> %d (变化%+d) 原因: %s%n",
        //     segmentId, oldScore, newScore, delta, reason);
    }

    /**
     * 增加连续错误次数
     */
    public void incrementConsecutiveErrors() {
        int newCount = consecutiveErrors.incrementAndGet();

        // 连续错误过多时降低健康度
        if (newCount % 5 == 0) { // 每5次连续错误降低健康度
            decreaseHealthScore(3);
        }

        // 连续错误达到阈值时设置故障标志
        if (newCount >= SegmentConstants.MAX_CONSECUTIVE_ERRORS) {
            faultDetected = true;
        }

        // 记录错误
        logConsecutiveErrorChange(newCount, "连续错误增加");
    }

    /**
     * 重置连续错误次数
     */
    public void resetConsecutiveErrors() {
        int oldCount = consecutiveErrors.get();
        consecutiveErrors.set(0);

        // 如果连续错误被重置，稍微提高健康度
        if (oldCount > 0) {
            increaseHealthScore(Math.min(5, oldCount / 2));
            logConsecutiveErrorChange(0, "连续错误重置");
        }
    }
    /**
     * 设置连续错误次数
     * @param count 错误次数
     */
    public void setConsecutiveErrors(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Consecutive error count cannot be negative");
        }

        int oldCount = consecutiveErrors.get();
        consecutiveErrors.set(count);

        // 根据新的错误次数调整健康度和故障状态
        if (count >= SegmentConstants.MAX_CONSECUTIVE_ERRORS) {
            faultDetected = true;
        }

        logConsecutiveErrorChange(count, "连续错误设置");
    }

    /**
     * 增加计数器不匹配错误次数
     */
    public void incrementCounterMismatchErrors() {
        int newCount = errorCountMismatch.incrementAndGet();
        incrementConsecutiveErrors();
        updateErrorWindow();
        decreaseHealthScoreByErrorType("counter_mismatch");

        logErrorCountChange("计数器不匹配", newCount);
    }

    /**
     * 增加ID逻辑错误次数
     */
    public void incrementIdLogicErrors() {
        int newCount = errorCountIdLogic.incrementAndGet();
        incrementConsecutiveErrors();
        updateErrorWindow();
        decreaseHealthScoreByErrorType("id_logic_error");

        logErrorCountChange("ID逻辑错误", newCount);
    }

    /**
     * 设置计数器不匹配错误次数
     * @param count 错误次数
     */
    public void setCounterMismatchErrors(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Error count cannot be negative");
        }

        errorCountMismatch.set(count);
        logErrorCountChange("计数器不匹配", count);
    }

    /**
     * 设置ID逻辑错误次数
     * @param count 错误次数
     */
    public void setIdLogicErrors(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Error count cannot be negative");
        }

        errorCountIdLogic.set(count);
        logErrorCountChange("ID逻辑错误", count);
    }

    /**
     * 记录连续错误变化
     * @param newCount 新的连续错误次数
     * @param reason 变化原因
     */
    private void logConsecutiveErrorChange(int newCount, String reason) {
        // 这里可以添加日志记录逻辑
        // System.out.printf("路段%d连续错误次数: %d 原因: %s%n", segmentId, newCount, reason);
    }

    /**
     * 记录错误次数变化
     * @param errorType 错误类型
     * @param newCount 新的错误次数
     */
    private void logErrorCountChange(String errorType, int newCount) {
        // 这里可以添加日志记录逻辑
        // System.out.printf("路段%d %s错误次数: %d%n", segmentId, errorType, newCount);
    }

    /**
     * 计算综合健康度评分
     * 基于各种因素重新计算健康度
     */
    public void recalculateHealthScore() {
        int baseScore = 100;

        // 基于错误次数降低分数
        int errorPenalty = (errorCountMismatch.get() + errorCountIdLogic.get()) * 2;
        int consecutivePenalty = consecutiveErrors.get() * 3;

        // 基于传感器状态降低分数
        long failedSensors = sensorStatus.values().stream()
                .filter(state -> state == SensorState.FAILED)
                .count();
        int sensorPenalty = (int) failedSensors * 5;

        // 基于拥堵程度调整分数
        int congestionPenalty = (congestionLevel > SegmentConstants.HIGH_CONGESTION_THRESHOLD) ? 10 : 0;

        // 基于通行效率提高分数
        int efficiencyBonus = (int) Math.min(10, throughputRate * 5);

        // 计算最终分数
        int finalScore = Math.max(0, Math.min(100,
                baseScore - errorPenalty - consecutivePenalty - sensorPenalty - congestionPenalty + efficiencyBonus));

        setHealthScore(finalScore);
    }

    /**
     * 获取健康度状态描述
     * @return 健康度状态描述
     */
    public String getHealthStatusDescription() {
        int score = segmentHealthScore.get();
        String level;

        if (score >= 80) {
            level = "优秀";
        } else if (score >= 60) {
            level = "良好";
        } else if (score >= 40) {
            level = "一般";
        } else if (score >= 20) {
            level = "较差";
        } else {
            level = "危险";
        }

        return String.format("健康度: %d/100 (%s), 连续错误: %d, 故障状态: %s",
                score, level, consecutiveErrors.get(), faultDetected ? "是" : "否");
    }

    /**
     * 检查是否需要维护
     * @return 是否需要维护
     */
    public boolean requiresMaintenance() {
        return segmentHealthScore.get() < 50 ||
                consecutiveErrors.get() >= SegmentConstants.MAX_CONSECUTIVE_ERRORS / 2 ||
                errorCountMismatch.get() >= SegmentConstants.MAX_COUNTER_MISMATCH_ERRORS / 2 ||
                faultDetected;
    }

    /**
     * 执行维护后的恢复操作
     */
    public void performMaintenanceRecovery() {
        // 重置错误计数
        resetErrorCounters();

        // 提高健康度
        increaseHealthScore(20);

        // 更新维护时间
        lastMaintenanceTime = LocalDateTime.now();

        // 重置传感器状态（如果需要）
        sensorStatus.replaceAll((id, state) ->
                (state == SensorState.FAILED) ? SensorState.NORMAL : state);
    }

    /**
     * 紧急健康度恢复
     * 用于紧急情况下的快速恢复
     */
    public void emergencyHealthRecovery() {
        // 强制重置所有错误计数
        resetErrorCounters();

        // 设置健康度到安全水平
        setHealthScore(SegmentConstants.RECOVERY_HEALTH_THRESHOLD + 10);

        // 清除故障标志
        faultDetected = false;

        // 重置时间相关状态
        resetAllTimers();

        // 记录紧急恢复事件
        logEmergencyRecovery();
    }

    /**
     * 记录紧急恢复事件
     */
    private void logEmergencyRecovery() {
        // 这里可以添加日志记录逻辑
        // System.out.printf("路段%d执行紧急健康度恢复 时间: %s%n",
        //     segmentId, LocalDateTime.now());
    }

    // ==================== 状态检查相关方法 ====================

    /**
     * 检查是否可以接受车辆进入
     * @param direction 方向
     * @param currentState 当前路段状态
     * @return 是否可以接受
     */
    public boolean canAcceptVehicle(Direction direction, SegmentState currentState) {
        // 检查容量限制
        if (isCapacityReached(direction)) {
            return false;
        }

        // 检查状态兼容性
        return switch (direction) {
            case UPSTREAM -> currentState.allowsUpstreamEntry();
            case DOWNSTREAM -> currentState.allowsDownstreamEntry();
            case NONE -> false;
        };
    }

    /**
     * 检查是否应该生成通行请求
     * @param direction 方向
     * @return 是否应该生成请求
     */
    public boolean shouldGenerateRequest(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> !upstreamRequest &&
                    upstreamVehicleIds.size() >= SegmentConstants.calculateRequestTriggerThreshold(upstreamCapacity);
            case DOWNSTREAM -> !downstreamRequest &&
                    downstreamVehicleIds.size() >= SegmentConstants.calculateRequestTriggerThreshold(downstreamCapacity);
            case NONE -> false;
        };
    }

    /**
     * 检查是否应该清除通行请求
     * @param direction 方向
     * @return 是否应该清除请求
     */
    public boolean shouldClearRequest(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> upstreamRequest && upstreamVehicleIds.isEmpty() &&
                    upstreamInCounter.get() == upstreamOutCounter.get();
            case DOWNSTREAM -> downstreamRequest && downstreamVehicleIds.isEmpty() &&
                    downstreamInCounter.get() == downstreamOutCounter.get();
            case NONE -> false;
        };
    }

    /**
     * 设置上行通行请求标志
     * @param request 请求标志
     */
    public void setUpstreamRequest(boolean request) {
        boolean oldRequest = this.upstreamRequest;
        this.upstreamRequest = request;

        // 如果从false变为true，自动设置请求时间和计算优先级
        if (!oldRequest && request) {
            if (upstreamRequestTime == null) {
                upstreamRequestTime = LocalDateTime.now();
            }
            calculateUpstreamPriority();
        }
        // 如果从true变为false，清除相关数据
        else if (oldRequest && !request) {
            upstreamRequestTime = null;
            priorityScoreUpstream = 0.0;
            upstreamWaitingTime = 0.0;
        }
    }
    /**
     * 设置下行通行请求标志
     * @param request 请求标志
     */
    public void setDownstreamRequest(boolean request) {
        boolean oldRequest = this.downstreamRequest;
        this.downstreamRequest = request;

        // 如果从false变为true，自动设置请求时间和计算优先级
        if (!oldRequest && request) {
            if (downstreamRequestTime == null) {
                downstreamRequestTime = LocalDateTime.now();
            }
            calculateDownstreamPriority();
        }
        // 如果从true变为false，清除相关数据
        else if (oldRequest && !request) {
            downstreamRequestTime = null;
            priorityScoreDownstream = 0.0;
            downstreamWaitingTime = 0.0;
        }
    }
    /**
     * 设置上行请求产生时间
     * @param requestTime 请求产生时间
     */
    public void setUpstreamRequestTime(LocalDateTime requestTime) {
        this.upstreamRequestTime = requestTime;

        // 如果设置了时间但请求标志为false，自动设置为true
        if (requestTime != null && !upstreamRequest) {
            upstreamRequest = true;
        }
        // 如果时间设置为null但请求标志为true，自动设置为false
        else if (requestTime == null && upstreamRequest) {
            upstreamRequest = false;
        }

        // 重新计算优先级
        if (upstreamRequest) {
            calculateUpstreamPriority();
        }
    }

    /**
     * 设置下行请求产生时间
     * @param requestTime 请求产生时间
     */
    public void setDownstreamRequestTime(LocalDateTime requestTime) {
        this.downstreamRequestTime = requestTime;

        // 如果设置了时间但请求标志为false，自动设置为true
        if (requestTime != null && !downstreamRequest) {
            downstreamRequest = true;
        }
        // 如果时间设置为null但请求标志为true，自动设置为false
        else if (requestTime == null && downstreamRequest) {
            downstreamRequest = false;
        }

        // 重新计算优先级
        if (downstreamRequest) {
            calculateDownstreamPriority();
        }
    }

    /**
     * 同时设置上行请求标志和时间
     * @param request 请求标志
     * @param requestTime 请求时间（如果为null且request为true，则使用当前时间）
     */
    public void setUpstreamRequestAndTime(boolean request, LocalDateTime requestTime) {
        this.upstreamRequest = request;

        if (request) {
            this.upstreamRequestTime = (requestTime != null) ? requestTime : LocalDateTime.now();
            calculateUpstreamPriority();
        } else {
            this.upstreamRequestTime = null;
            this.priorityScoreUpstream = 0.0;
            this.upstreamWaitingTime = 0.0;
        }
    }

    /**
     * 同时设置下行请求标志和时间
     * @param request 请求标志
     * @param requestTime 请求时间（如果为null且request为true，则使用当前时间）
     */
    public void setDownstreamRequestAndTime(boolean request, LocalDateTime requestTime) {
        this.downstreamRequest = request;

        if (request) {
            this.downstreamRequestTime = (requestTime != null) ? requestTime : LocalDateTime.now();
            calculateDownstreamPriority();
        } else {
            this.downstreamRequestTime = null;
            this.priorityScoreDownstream = 0.0;
            this.downstreamWaitingTime = 0.0;
        }
    }

    /**
     * 根据方向设置通行请求
     * @param direction 方向
     * @param request 请求标志
     */
    public void setRequestByDirection(Direction direction, boolean request) {
        switch (direction) {
            case UPSTREAM:
                setUpstreamRequest(request);
                break;
            case DOWNSTREAM:
                setDownstreamRequest(request);
                break;
            case NONE:
                throw new IllegalArgumentException("Cannot set request for NONE direction");
        }
    }

    /**
     * 根据方向设置通行请求时间
     * @param direction 方向
     * @param requestTime 请求时间
     */
    public void setRequestTimeByDirection(Direction direction, LocalDateTime requestTime) {
        switch (direction) {
            case UPSTREAM:
                setUpstreamRequestTime(requestTime);
                break;
            case DOWNSTREAM:
                setDownstreamRequestTime(requestTime);
                break;
            case NONE:
                throw new IllegalArgumentException("Cannot set request time for NONE direction");
        }
    }

    /**
     * 根据方向同时设置请求标志和时间
     * @param direction 方向
     * @param request 请求标志
     * @param requestTime 请求时间
     */
    public void setRequestAndTimeByDirection(Direction direction, boolean request, LocalDateTime requestTime) {
        switch (direction) {
            case UPSTREAM:
                setUpstreamRequestAndTime(request, requestTime);
                break;
            case DOWNSTREAM:
                setDownstreamRequestAndTime(request, requestTime);
                break;
            case NONE:
                throw new IllegalArgumentException("Cannot set request and time for NONE direction");
        }
    }

    /**
     * 重置所有通行请求
     */
    public void resetAllRequests() {
        this.upstreamRequest = false;
        this.downstreamRequest = false;
        this.upstreamRequestTime = null;
        this.downstreamRequestTime = null;
        this.priorityScoreUpstream = 0.0;
        this.priorityScoreDownstream = 0.0;
        this.upstreamWaitingTime = 0.0;
        this.downstreamWaitingTime = 0.0;
    }

    /**
     * 强制生成指定方向的通行请求（即使没有车辆）
     * @param direction 方向
     * @param requestTime 请求时间（可选，为null时使用当前时间）
     */
    public void forceGenerateRequest(Direction direction, LocalDateTime requestTime) {
        LocalDateTime time = (requestTime != null) ? requestTime : LocalDateTime.now();

        switch (direction) {
            case UPSTREAM:
                this.upstreamRequest = true;
                this.upstreamRequestTime = time;
                calculateUpstreamPriority();
                break;
            case DOWNSTREAM:
                this.downstreamRequest = true;
                this.downstreamRequestTime = time;
                calculateDownstreamPriority();
                break;
            case NONE:
                throw new IllegalArgumentException("Cannot force generate request for NONE direction");
        }
    }

    /**
     * 检查指定方向是否有通行请求
     * @param direction 方向
     * @return 是否有通行请求
     */
    public boolean hasRequestByDirection(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> upstreamRequest;
            case DOWNSTREAM -> downstreamRequest;
            case NONE -> upstreamRequest || downstreamRequest; // 任一方向有请求
        };
    }

    /**
     * 获取指定方向的请求时间
     * @param direction 方向
     * @return 请求时间
     */
    public LocalDateTime getRequestTimeByDirection(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> upstreamRequestTime;
            case DOWNSTREAM -> downstreamRequestTime;
            case NONE -> null; // NONE方向没有特定的请求时间
        };
    }

    /**
     * 获取指定方向的等待时间
     * @param direction 方向
     * @return 等待时间（秒）
     */
    public double getWaitingTimeByDirection(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> upstreamWaitingTime;
            case DOWNSTREAM -> downstreamWaitingTime;
            case NONE -> Math.max(upstreamWaitingTime, downstreamWaitingTime); // 返回较大的等待时间
        };
    }

    /**
     * 获取指定方向的优先级得分
     * @param direction 方向
     * @return 优先级得分
     */
    public double getPriorityScoreByDirection(Direction direction) {
        return switch (direction) {
            case UPSTREAM -> priorityScoreUpstream;
            case DOWNSTREAM -> priorityScoreDownstream;
            case NONE -> Math.max(priorityScoreUpstream, priorityScoreDownstream); // 返回较高的优先级得分
        };
    }

    /**
     * 手动设置优先级得分
     * @param direction 方向
     * @param score 优先级得分
     */
    public void setPriorityScoreByDirection(Direction direction, double score) {
        switch (direction) {
            case UPSTREAM:
                this.priorityScoreUpstream = Math.max(0.0, score); // 确保非负
                break;
            case DOWNSTREAM:
                this.priorityScoreDownstream = Math.max(0.0, score); // 确保非负
                break;
            case NONE:
                throw new IllegalArgumentException("Cannot set priority score for NONE direction");
        }
    }

    /**
     * 手动设置等待时间
     * @param direction 方向
     * @param waitingTime 等待时间（秒）
     */
    public void setWaitingTimeByDirection(Direction direction, double waitingTime) {
        switch (direction) {
            case UPSTREAM:
                this.upstreamWaitingTime = Math.max(0.0, waitingTime); // 确保非负
                break;
            case DOWNSTREAM:
                this.downstreamWaitingTime = Math.max(0.0, waitingTime); // 确保非负
                break;
            case NONE:
                throw new IllegalArgumentException("Cannot set waiting time for NONE direction");
        }
    }

    /**
     * 更新所有方向的等待时间和优先级
     */
    public void updateAllPriorities() {
        if (upstreamRequest) {
            calculateUpstreamPriority();
        }
        if (downstreamRequest) {
            calculateDownstreamPriority();
        }
    }

    /**
     * 获取路段状态摘要
     * @return 状态摘要字符串
     */
    public String getStatusSummary() {
        return String.format(
                "路段%d: 健康度=%d, 上行车辆=%d, 下行车辆=%d, 清空决策=%s, 故障=%s",
                segmentId, segmentHealthScore.get(), upstreamVehicleIds.size(),
                downstreamVehicleIds.size(), overallClearanceDecision, faultDetected
        );
    }

    /**
     * 获取通行请求状态摘要
     * @return 请求状态摘要
     */
    public String getRequestStatusSummary() {
        return String.format(
                "通行请求状态 - 上行: %s(%s), 下行: %s(%s), 优先方向: %s",
                upstreamRequest ? "有请求" : "无请求",
                upstreamRequest && upstreamRequestTime != null ?
                        String.format("%.1f秒前", java.time.Duration.between(upstreamRequestTime, LocalDateTime.now()).getSeconds()) : "N/A",
                downstreamRequest ? "有请求" : "无请求",
                downstreamRequest && downstreamRequestTime != null ?
                        String.format("%.1f秒前", java.time.Duration.between(downstreamRequestTime, LocalDateTime.now()).getSeconds()) : "N/A",
                determinePriorityDirection().getDescription()
        );
    }

    // ==================== Getter和Setter方法 ====================

    // 基础属性
    public int getSegmentId() { return segmentId; }

    // 时间相关
    public LocalDateTime getGreenStartTime() { return greenStartTime; }
    public void setGreenStartTime(LocalDateTime greenStartTime) { this.greenStartTime = greenStartTime; }

    public LocalDateTime getRedStartTime() { return redStartTime; }
    public void setRedStartTime(LocalDateTime redStartTime) { this.redStartTime = redStartTime; }

    public LocalDateTime getLastSwitchTime() { return lastSwitchTime; }
    public void setLastSwitchTime(LocalDateTime lastSwitchTime) { this.lastSwitchTime = lastSwitchTime; }

    public LocalDateTime getConservativeTimerStart() { return conservativeTimerStart; }
    public void setConservativeTimerStart(LocalDateTime conservativeTimerStart) { this.conservativeTimerStart = conservativeTimerStart; }

    // 车辆相关
    public Set<String> getUpstreamVehicleIds() { return new HashSet<>(upstreamVehicleIds); }
    public Set<String> getDownstreamVehicleIds() { return new HashSet<>(downstreamVehicleIds); }

    public int getUpstreamInCounter() { return upstreamInCounter.get(); }
    public int getUpstreamOutCounter() { return upstreamOutCounter.get(); }
    public int getDownstreamInCounter() { return downstreamInCounter.get(); }
    public int getDownstreamOutCounter() { return downstreamOutCounter.get(); }

    // 通行请求相关
    public boolean isUpstreamRequest() { return upstreamRequest; }
    public boolean isDownstreamRequest() { return downstreamRequest; }
    public LocalDateTime getUpstreamRequestTime() { return upstreamRequestTime; }
    public LocalDateTime getDownstreamRequestTime() { return downstreamRequestTime; }

    // 清空决策相关
    public ClearanceDecision getUpstreamClearanceDecision() { return upstreamClearanceDecision; }
    public ClearanceDecision getDownstreamClearanceDecision() { return downstreamClearanceDecision; }
    public ClearanceDecision getOverallClearanceDecision() { return overallClearanceDecision; }

    // 优先级相关
    public Direction getLastServedDirection() { return lastServedDirection; }
    public void setLastServedDirection(Direction lastServedDirection) { this.lastServedDirection = lastServedDirection; }

    public double getUpstreamWaitingTime() { return upstreamWaitingTime; }
    public double getDownstreamWaitingTime() { return downstreamWaitingTime; }
    public double getPriorityScoreUpstream() { return priorityScoreUpstream; }
    public double getPriorityScoreDownstream() { return priorityScoreDownstream; }

    // 错误统计相关
    public int getErrorCountMismatch() { return errorCountMismatch.get(); }
    public int getErrorCountIdLogic() { return errorCountIdLogic.get(); }
    public int getConsecutiveErrors() { return consecutiveErrors.get(); }
    public LocalDateTime getErrorTimeWindowStart() { return errorTimeWindowStart; }

    // 健康状态相关
    public Map<String, SensorState> getSensorStatus() { return new HashMap<>(sensorStatus); }
    public int getSegmentHealthScore() { return segmentHealthScore.get(); }
    public boolean isFaultDetected() { return faultDetected; }
    public void setFaultDetected(boolean faultDetected) { this.faultDetected = faultDetected; }
    public LocalDateTime getLastMaintenanceTime() { return lastMaintenanceTime; }
    public void setLastMaintenanceTime(LocalDateTime lastMaintenanceTime) { this.lastMaintenanceTime = lastMaintenanceTime; }

    // 性能统计相关
    public long getTotalVehiclesServed() { return totalVehiclesServed.get(); }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getThroughputRate() { return throughputRate; }
    public double getCongestionLevel() { return congestionLevel; }

    // 容量配置相关
    public int getUpstreamCapacity() { return upstreamCapacity; }
    public void setUpstreamCapacity(int upstreamCapacity) {
        this.upstreamCapacity = SegmentConstants.clampCapacity(upstreamCapacity);
    }

    public int getDownstreamCapacity() { return downstreamCapacity; }
    public void setDownstreamCapacity(int downstreamCapacity) {
        this.downstreamCapacity = SegmentConstants.clampCapacity(downstreamCapacity);
    }

    // 车辆进入时间记录
    public Map<String, LocalDateTime> getVehicleEntryTimes() { return new HashMap<>(vehicleEntryTimes); }
}
