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
