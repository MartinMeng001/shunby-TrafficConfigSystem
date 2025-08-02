package com.traffic.config.statemachinev3.variables;

import com.traffic.config.statemachinev3.enums.system.SystemStateV3;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.constants.SystemConstants;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 顶层系统变量集合
 * 对应数学模型中的变量集合 V_sys
 *
 * 这个类包含了顶层系统状态机运行时需要的所有变量
 *
 * @author System
 * @version 3.0.0
 */
public class SystemVariables {

    // ==================== 时间管理变量 (Time Management Variables) ====================

    /**
     * 当前状态开始时间
     */
    private volatile LocalDateTime stateStartTime;

    /**
     * 过渡开始时间
     */
    private volatile LocalDateTime transitionStartTime;

    /**
     * 最后故障时间
     */
    private volatile LocalDateTime lastFaultTime;

    /**
     * 恢复开始时间
     */
    private volatile LocalDateTime recoveryStartTime;

    /**
     * 最后健康度更新时间
     */
    private volatile LocalDateTime lastHealthUpdateTime;

    /**
     * 最后清空检查时间
     */
    private volatile LocalDateTime lastClearanceCheckTime;

    /**
     * 系统初始化开始时间
     */
    private volatile LocalDateTime systemInitStartTime;

    // ==================== 计数器和统计变量 (Counter and Statistics Variables) ====================

    /**
     * 时间窗口内错误计数
     */
    private final AtomicInteger errorCountWindow = new AtomicInteger(0);

    /**
     * 连续故障次数
     */
    private final AtomicInteger consecutiveFaults = new AtomicInteger(0);

    /**
     * 恢复尝试次数
     */
    private final AtomicInteger recoveryAttempts = new AtomicInteger(0);

    /**
     * 稳定运行时间（秒）
     */
    private final AtomicLong stableOperationTime = new AtomicLong(0);

    /**
     * 清空超时计数
     */
    private final AtomicInteger clearTimeoutCount = new AtomicInteger(0);

    /**
     * ID逻辑错误计数
     */
    private final AtomicInteger idLogicErrorCount = new AtomicInteger(0);

    /**
     * 计数器不匹配错误计数
     */
    private final AtomicInteger counterMismatchErrorCount = new AtomicInteger(0);

    // ==================== 状态标记变量 (State Flag Variables) ====================

    /**
     * 前一个状态
     */
    private volatile SystemStateV3 previousState;

    /**
     * 故障来源标识
     */
    private volatile FaultSource faultSource;

    /**
     * 维护类型
     */
    private volatile MaintenanceType maintenanceType;

    /**
     * 紧急级别
     */
    private volatile EmergencyLevel emergencyLevel;

    /**
     * 清空检测是否激活
     */
    private volatile boolean clearDetectionActive;

    /**
     * 自动恢复是否启用
     */
    private volatile boolean autoRecoveryEnabled;

    // ==================== 系统健康度变量 (System Health Variables) ====================

    /**
     * 系统健康度评分 (0-100)
     */
    private final AtomicInteger systemHealthScore = new AtomicInteger(SystemConstants.INITIAL_HEALTH_SCORE);

    /**
     * 性能降级程度 (0.0-1.0)
     */
    private volatile double performanceDegradation;

    /**
     * 最后稳定配置
     */
    private volatile Map<String, Object> lastStableConfig;

    /**
     * 通信状态是否正常
     */
    private volatile boolean communicationNormal;

    /**
     * 电源状态是否正常
     */
    private volatile boolean powerStatusNormal;

    // ==================== 路段管理变量 (Segment Management Variables) ====================

    /**
     * 路段总数
     */
    private volatile int segmentCount;

    /**
     * 路段清空状态数组
     */
    private final Map<Integer, ClearanceState> segmentClearanceStates = new ConcurrentHashMap<>();

    /**
     * 已清空路段数
     */
    private final AtomicInteger clearedSegmentCount = new AtomicInteger(0);

    /**
     * 所有路段是否就绪
     */
    private volatile boolean segmentsAllReady;

    // ==================== 控制模式变量 (Control Mode Variables) ====================

    /**
     * 当前控制模式
     */
    private volatile ControlMode currentControlMode;

    /**
     * 感应算法是否激活
     */
    private volatile boolean inductiveAlgorithmActive;

    /**
     * 手动控制是否激活
     */
    private volatile boolean manualControlActive;

    /**
     * 应急信号是否激活
     */
    private volatile boolean emergencySignalsActive;

    // ==================== 通信和外部接口变量 (Communication and External Interface Variables) ====================

    /**
     * 通信状态
     */
    private volatile CommunicationStatus communicationStatus;

    /**
     * 外部系统连接状态
     */
    private volatile boolean externalSystemConnected;

    /**
     * 电源状态
     */
    private volatile PowerStatus powerStatus;

    // ==================== 错误统计变量 (Error Statistics Variables) ====================

    /**
     * 错误历史记录
     */
    private final List<ErrorRecord> errorHistory = Collections.synchronizedList(new ArrayList<>());

    /**
     * 最大错误历史记录数
     */
    private static final int MAX_ERROR_HISTORY = 1000;

    // ==================== 枚举定义 ====================

    /**
     * 故障来源枚举
     */
    public enum FaultSource {
        SEGMENT("路段故障"),
        SYSTEM("系统故障"),
        EXTERNAL("外部故障"),
        UNKNOWN("未知故障");

        private final String description;

        FaultSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 维护类型枚举
     */
    public enum MaintenanceType {
        ROUTINE("例行维护"),
        EMERGENCY("紧急维护"),
        DEBUG("调试维护"),
        UPGRADE("升级维护");

        private final String description;

        MaintenanceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 紧急级别枚举
     */
    public enum EmergencyLevel {
        LOW(1, "低级"),
        MEDIUM(2, "中级"),
        HIGH(3, "高级"),
        CRITICAL(4, "关键");

        private final int level;
        private final String description;

        EmergencyLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 控制模式枚举
     */
    public enum ControlMode {
        INDUCTIVE("感应控制"),
        DEGRADED("降级控制"),
        MANUAL("手动控制"),
        EMERGENCY("紧急控制");

        private final String description;

        ControlMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通信状态枚举
     */
    public enum CommunicationStatus {
        NORMAL("正常"),
        DEGRADED("降级"),
        FAILED("失败");

        private final String description;

        CommunicationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 电源状态枚举
     */
    public enum PowerStatus {
        NORMAL("正常"),
        BACKUP("备用电源"),
        CRITICAL("电源危机");

        private final String description;

        PowerStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 内部类：路段清空状态 ====================

    /**
     * 路段清空状态类
     */
    public static class ClearanceState {
        private final int segmentId;
        private volatile ClearanceDecision upstreamDecision;
        private volatile ClearanceDecision downstreamDecision;
        private volatile ClearanceDecision overallDecision;
        private volatile LocalDateTime conservativeTimerStart;
        private volatile LocalDateTime lastUpdateTime;
        private volatile boolean faultDetected;
        private volatile boolean isReadyForSwitch;

        public ClearanceState(int segmentId) {
            this.segmentId = segmentId;
            this.upstreamDecision = ClearanceDecision.WAIT;
            this.downstreamDecision = ClearanceDecision.WAIT;
            this.overallDecision = ClearanceDecision.WAIT;
            this.conservativeTimerStart = null;
            this.lastUpdateTime = LocalDateTime.now();
            this.faultDetected = false;
            this.isReadyForSwitch = false;
        }

        // Getter和Setter方法
        public int getSegmentId() { return segmentId; }
        public ClearanceDecision getUpstreamDecision() { return upstreamDecision; }
        public void setUpstreamDecision(ClearanceDecision upstreamDecision) { this.upstreamDecision = upstreamDecision; }
        public ClearanceDecision getDownstreamDecision() { return downstreamDecision; }
        public void setDownstreamDecision(ClearanceDecision downstreamDecision) { this.downstreamDecision = downstreamDecision; }
        public ClearanceDecision getOverallDecision() { return overallDecision; }
        public void setOverallDecision(ClearanceDecision overallDecision) { this.overallDecision = overallDecision; }
        public LocalDateTime getConservativeTimerStart() { return conservativeTimerStart; }
        public void setConservativeTimerStart(LocalDateTime conservativeTimerStart) { this.conservativeTimerStart = conservativeTimerStart; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        public boolean isFaultDetected() { return faultDetected; }
        public void setFaultDetected(boolean faultDetected) { this.faultDetected = faultDetected; }
        public boolean isReadyForSwitch() { return isReadyForSwitch; }
        public void setReadyForSwitch(boolean readyForSwitch) { this.isReadyForSwitch = readyForSwitch; }

        @Override
        public String toString() {
            return String.format("ClearanceState{segmentId=%d, overall=%s, ready=%s}",
                    segmentId, overallDecision, isReadyForSwitch);
        }
    }

    // ==================== 内部类：错误记录 ====================

    /**
     * 错误记录类
     */
    public static class ErrorRecord {
        private final String errorType;
        private final String description;
        private final LocalDateTime timestamp;
        private final String source;

        public ErrorRecord(String errorType, String description, String source) {
            this.errorType = errorType;
            this.description = description;
            this.timestamp = LocalDateTime.now();
            this.source = source;
        }

        public String getErrorType() { return errorType; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getSource() { return source; }

        @Override
        public String toString() {
            return String.format("[%s] %s from %s: %s", timestamp, errorType, source, description);
        }
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     * 初始化所有变量为默认值
     */
    public SystemVariables() {
        initializeVariables();
    }

    /**
     * 初始化所有变量
     */
    private void initializeVariables() {
        LocalDateTime now = LocalDateTime.now();

        // 时间变量初始化
        this.stateStartTime = now;
        this.systemInitStartTime = now;
        this.transitionStartTime = null;
        this.lastFaultTime = null;
        this.recoveryStartTime = null;
        this.lastHealthUpdateTime = now;
        this.lastClearanceCheckTime = null;

        // 状态标记变量初始化
        this.previousState = null;
        this.faultSource = null;
        this.maintenanceType = null;
        this.emergencyLevel = EmergencyLevel.LOW;
        this.clearDetectionActive = false;
        this.autoRecoveryEnabled = true;

        // 路段管理变量初始化
        this.segmentCount = SystemConstants.TOTAL_SEGMENT_COUNT;
        this.segmentsAllReady = false;
        initializeSegmentClearanceStates();

        // 系统健康度变量初始化
        this.performanceDegradation = 0.0;
        this.lastStableConfig = new HashMap<>();
        this.communicationNormal = true;
        this.powerStatusNormal = true;

        // 控制模式变量初始化
        this.currentControlMode = ControlMode.MANUAL;
        this.inductiveAlgorithmActive = false;
        this.manualControlActive = false;
        this.emergencySignalsActive = false;

        // 通信和外部接口变量初始化
        this.communicationStatus = CommunicationStatus.NORMAL;
        this.externalSystemConnected = true;
        this.powerStatus = PowerStatus.NORMAL;
    }

    /**
     * 初始化路段清空状态
     */
    private void initializeSegmentClearanceStates() {
        segmentClearanceStates.clear();
        for (int i = 1; i <= segmentCount; i++) {
            segmentClearanceStates.put(i, new ClearanceState(i));
        }
    }

    // ==================== 时间相关方法 ====================

    /**
     * 更新状态开始时间
     * @param newState 新状态
     */
    public void updateStateStartTime(SystemStateV3 newState) {
        this.previousState = getCurrentStateFromTime(); // 简化实现
        this.stateStartTime = LocalDateTime.now();
    }

    /**
     * 获取当前状态持续时间（秒）
     * @return 持续时间
     */
    public long getCurrentStateDurationSeconds() {
        if (stateStartTime == null) {
            return 0;
        }
        return java.time.Duration.between(stateStartTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 获取过渡持续时间（秒）
     * @return 过渡持续时间，如果没有过渡则返回0
     */
    public long getTransitionDurationSeconds() {
        if (transitionStartTime == null) {
            return 0;
        }
        return java.time.Duration.between(transitionStartTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 检查过渡是否超时
     * @return 是否超时
     */
    public boolean isTransitionTimeout() {
        return getTransitionDurationSeconds() > SystemConstants.getTransitionTimeoutSeconds();
    }

    /**
     * 检查是否满足过渡完成条件
     * @return 是否满足条件
     */
    public boolean isTransitionComplete() {
        return getTransitionDurationSeconds() >= SystemConstants.TRANSITION_TIME && segmentsAllReady;
    }

    /**
     * 检查系统初始化是否超时
     * @return 是否超时
     */
    public boolean isSystemInitTimeout() {
        if (systemInitStartTime == null) {
            return false;
        }
        long duration = java.time.Duration.between(systemInitStartTime, LocalDateTime.now()).getSeconds();
        return duration > SystemConstants.SYSTEM_INIT_DELAY;
    }

    // ==================== 路段清空相关方法 ====================

    /**
     * 更新路段清空状态
     * @param segmentId 路段ID
     * @param upstreamDecision 上行清空决策
     * @param downstreamDecision 下行清空决策
     * @param overallDecision 综合清空决策
     */
    public void updateSegmentClearanceState(int segmentId, ClearanceDecision upstreamDecision,
                                            ClearanceDecision downstreamDecision, ClearanceDecision overallDecision) {
        ClearanceState state = segmentClearanceStates.get(segmentId);
        if (state != null) {
            state.setUpstreamDecision(upstreamDecision);
            state.setDownstreamDecision(downstreamDecision);
            state.setOverallDecision(overallDecision);
            state.setLastUpdateTime(LocalDateTime.now());
            state.setReadyForSwitch(overallDecision.isSafeForTransition());

            lastClearanceCheckTime = LocalDateTime.now();
            recalculateClearanceStatus();
        }
    }

    /**
     * 重新计算总体清空状态
     */
    private void recalculateClearanceStatus() {
        if (segmentClearanceStates.isEmpty()) {
            segmentsAllReady = false;
            clearedSegmentCount.set(0);
            return;
        }

        int readyCount = 0;
        for (ClearanceState state : segmentClearanceStates.values()) {
            if (state.isReadyForSwitch() || isConservativeTimerExpired(state)) {
                readyCount++;
            }
        }

        clearedSegmentCount.set(readyCount);
        segmentsAllReady = (readyCount == segmentCount);
    }

    /**
     * 检查保守清空计时器是否到期
     * @param state 清空状态
     * @return 是否到期
     */
    private boolean isConservativeTimerExpired(ClearanceState state) {
        if (state.getConservativeTimerStart() == null) {
            return false;
        }
        long duration = java.time.Duration.between(state.getConservativeTimerStart(), LocalDateTime.now()).getSeconds();
        return duration >= SystemConstants.MAX_RED_TIME; // 使用系统常量作为保守清空时间
    }

    /**
     * 获取清空完成百分比
     * @return 清空完成百分比 (0.0-1.0)
     */
    public double getClearanceCompletionPercentage() {
        if (segmentCount == 0) {
            return 0.0;
        }
        return (double) clearedSegmentCount.get() / segmentCount;
    }

    // ==================== 错误统计相关方法 ====================

    /**
     * 记录错误
     * @param errorType 错误类型
     * @param description 错误描述
     * @param source 错误来源
     */
    public void recordError(String errorType, String description, String source) {
        ErrorRecord error = new ErrorRecord(errorType, description, source);
        synchronized (errorHistory) {
            errorHistory.add(error);

            // 限制历史记录大小
            if (errorHistory.size() > MAX_ERROR_HISTORY) {
                errorHistory.remove(0);
            }
        }

        // 更新错误计数
        updateErrorCounts();
    }

    /**
     * 更新错误计数（基于时间窗口）
     */
    private void updateErrorCounts() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(SystemConstants.ERROR_WINDOW);

        synchronized (errorHistory) {
            // 清理过期的错误记录
            errorHistory.removeIf(error -> error.getTimestamp().isBefore(cutoffTime));

            // 重新计算错误计数
            errorCountWindow.set(errorHistory.size());

            long counterMismatchCount = errorHistory.stream()
                    .filter(error -> "COUNTER_MISMATCH".equals(error.getErrorType()))
                    .count();
            counterMismatchErrorCount.set((int) counterMismatchCount);

            long idLogicCount = errorHistory.stream()
                    .filter(error -> "ID_LOGIC_ERROR".equals(error.getErrorType()))
                    .count();
            idLogicErrorCount.set((int) idLogicCount);
        }
    }

    /**
     * 检查是否应该退出感应模式
     * @return 是否应该退出
     */
    public boolean shouldExitInductiveMode() {
        updateErrorCounts();
        return counterMismatchErrorCount.get() >= SystemConstants.MAX_MISMATCH_ERRORS ||
                idLogicErrorCount.get() >= SystemConstants.MAX_ID_ERRORS ||
                consecutiveFaults.get() >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT;
    }

    // ==================== 系统健康度相关方法 ====================

    /**
     * 更新系统健康度评分
     * @param delta 评分变化量
     */
    public void updateHealthScore(int delta) {
        int currentScore = systemHealthScore.get();
        int newScore = SystemConstants.clampHealthScore(currentScore + delta);
        systemHealthScore.set(newScore);
        lastHealthUpdateTime = LocalDateTime.now();
    }

    /**
     * 检查系统是否健康
     * @return 是否健康
     */
    public boolean isSystemHealthy() {
        return systemHealthScore.get() >= SystemConstants.RECOVERY_HEALTH_THRESHOLD &&
                communicationNormal && powerStatusNormal;
    }

    /**
     * 检查是否为严重故障状态
     * @return 是否为严重故障
     */
    public boolean isCriticalFault() {
        return systemHealthScore.get() < SystemConstants.CRITICAL_HEALTH_THRESHOLD ||
                !communicationNormal || !powerStatusNormal ||
                consecutiveFaults.get() >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT;
    }

    // ==================== 恢复条件检查方法 ====================

    /**
     * 检查恢复条件是否满足
     * @return 是否满足恢复条件
     */
    public boolean areRecoveryConditionsMet() {
        if (lastFaultTime == null) {
            return false;
        }

        long timeSinceLastFault = java.time.Duration.between(lastFaultTime, LocalDateTime.now()).getSeconds();
        return timeSinceLastFault >= SystemConstants.STABLE_OPERATION_TIME &&
                errorCountWindow.get() < (SystemConstants.MAX_MISMATCH_ERRORS * SystemConstants.ERROR_RESET_THRESHOLD) &&
                isSystemHealthy();
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据当前时间推断当前状态（简化实现）
     * @return 推断的当前状态
     */
    private SystemStateV3 getCurrentStateFromTime() {
        // 这里是简化实现，实际应该从状态机获取
        return SystemStateV3.SYSTEM_INIT;
    }

    /**
     * 重置所有计数器
     */
    public void resetCounters() {
        this.errorCountWindow.set(0);
        this.consecutiveFaults.set(0);
        this.recoveryAttempts.set(0);
        this.clearTimeoutCount.set(0);
        this.idLogicErrorCount.set(0);
        this.counterMismatchErrorCount.set(0);
        synchronized (errorHistory) {
            this.errorHistory.clear();
        }
    }

    /**
     * 保存当前配置为稳定配置
     * @param config 配置信息
     */
    public void saveStableConfig(Map<String, Object> config) {
        this.lastStableConfig = new HashMap<>(config);
    }

    /**
     * 获取系统状态摘要
     * @return 状态摘要字符串
     */
    public String getStatusSummary() {
        return String.format(
                "健康度: %d/100, 错误计数: %d, 连续故障: %d, 路段清空: %d/%d, 通信: %s, 电源: %s, 控制模式: %s",
                systemHealthScore.get(), errorCountWindow.get(), consecutiveFaults.get(),
                clearedSegmentCount.get(), segmentCount,
                communicationNormal ? "正常" : "异常",
                powerStatusNormal ? "正常" : "异常",
                currentControlMode != null ? currentControlMode.getDescription() : "未知"
        );
    }

    /**
     * 检查系统是否处于正常运行状态
     * @return 是否正常运行
     */
    public boolean isNormalOperation() {
        return currentControlMode == ControlMode.INDUCTIVE &&
                inductiveAlgorithmActive &&
                isSystemHealthy() &&
                !emergencySignalsActive;
    }

    /**
     * 检查系统是否需要进入降级模式
     * @return 是否需要降级
     */
    public boolean shouldEnterDegradedMode() {
        return shouldExitInductiveMode() ||
                systemHealthScore.get() < SystemConstants.NORMAL_HEALTH_THRESHOLD ||
                communicationStatus == CommunicationStatus.DEGRADED;
    }

    /**
     * 检查系统是否需要进入紧急模式
     * @return 是否需要紧急模式
     */
    public boolean shouldEnterEmergencyMode() {
        return isCriticalFault() ||
                emergencyLevel == EmergencyLevel.CRITICAL ||
                communicationStatus == CommunicationStatus.FAILED ||
                powerStatus == PowerStatus.CRITICAL;
    }

    // ==================== Getter和Setter方法 ====================

    // 时间变量的getter和setter
    public LocalDateTime getStateStartTime() { return stateStartTime; }
    public void setStateStartTime(LocalDateTime stateStartTime) { this.stateStartTime = stateStartTime; }

    public LocalDateTime getTransitionStartTime() { return transitionStartTime; }
    public void setTransitionStartTime(LocalDateTime transitionStartTime) { this.transitionStartTime = transitionStartTime; }

    public LocalDateTime getLastFaultTime() { return lastFaultTime; }
    public void setLastFaultTime(LocalDateTime lastFaultTime) { this.lastFaultTime = lastFaultTime; }

    public LocalDateTime getRecoveryStartTime() { return recoveryStartTime; }
    public void setRecoveryStartTime(LocalDateTime recoveryStartTime) { this.recoveryStartTime = recoveryStartTime; }

    public LocalDateTime getLastHealthUpdateTime() { return lastHealthUpdateTime; }
    public void setLastHealthUpdateTime(LocalDateTime lastHealthUpdateTime) { this.lastHealthUpdateTime = lastHealthUpdateTime; }

    public LocalDateTime getLastClearanceCheckTime() { return lastClearanceCheckTime; }
    public void setLastClearanceCheckTime(LocalDateTime lastClearanceCheckTime) { this.lastClearanceCheckTime = lastClearanceCheckTime; }

    public LocalDateTime getSystemInitStartTime() { return systemInitStartTime; }
    public void setSystemInitStartTime(LocalDateTime systemInitStartTime) { this.systemInitStartTime = systemInitStartTime; }

    // 计数器变量的getter和setter
    public int getErrorCountWindow() { return errorCountWindow.get(); }
    public void setErrorCountWindow(int errorCountWindow) { this.errorCountWindow.set(errorCountWindow); }

    public int getConsecutiveFaults() { return consecutiveFaults.get(); }
    public void setConsecutiveFaults(int consecutiveFaults) { this.consecutiveFaults.set(consecutiveFaults); }
    public void incrementConsecutiveFaults() { this.consecutiveFaults.incrementAndGet(); }

    public int getRecoveryAttempts() { return recoveryAttempts.get(); }
    public void setRecoveryAttempts(int recoveryAttempts) { this.recoveryAttempts.set(recoveryAttempts); }
    public void incrementRecoveryAttempts() { this.recoveryAttempts.incrementAndGet(); }

    public long getStableOperationTime() { return stableOperationTime.get(); }
    public void setStableOperationTime(long stableOperationTime) { this.stableOperationTime.set(stableOperationTime); }

    public int getClearTimeoutCount() { return clearTimeoutCount.get(); }
    public void setClearTimeoutCount(int clearTimeoutCount) { this.clearTimeoutCount.set(clearTimeoutCount); }
    public void incrementClearTimeoutCount() { this.clearTimeoutCount.incrementAndGet(); }

    // 状态标记变量的getter和setter
    public SystemStateV3 getPreviousState() { return previousState; }
    public void setPreviousState(SystemStateV3 previousState) { this.previousState = previousState; }

    public FaultSource getFaultSource() { return faultSource; }
    public void setFaultSource(FaultSource faultSource) { this.faultSource = faultSource; }

    public MaintenanceType getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(MaintenanceType maintenanceType) { this.maintenanceType = maintenanceType; }

    public EmergencyLevel getEmergencyLevel() { return emergencyLevel; }
    public void setEmergencyLevel(EmergencyLevel emergencyLevel) { this.emergencyLevel = emergencyLevel; }

    public boolean isClearDetectionActive() { return clearDetectionActive; }
    public void setClearDetectionActive(boolean clearDetectionActive) { this.clearDetectionActive = clearDetectionActive; }

    public boolean isAutoRecoveryEnabled() { return autoRecoveryEnabled; }
    public void setAutoRecoveryEnabled(boolean autoRecoveryEnabled) { this.autoRecoveryEnabled = autoRecoveryEnabled; }

    // 系统健康度变量的getter和setter
    public int getSystemHealthScore() { return systemHealthScore.get(); }
    public void setSystemHealthScore(int systemHealthScore) {
        this.systemHealthScore.set(SystemConstants.clampHealthScore(systemHealthScore));
        this.lastHealthUpdateTime = LocalDateTime.now();
    }

    public double getPerformanceDegradation() { return performanceDegradation; }
    public void setPerformanceDegradation(double performanceDegradation) {
        this.performanceDegradation = Math.max(0.0, Math.min(1.0, performanceDegradation));
    }

    public Map<String, Object> getLastStableConfig() { return new HashMap<>(lastStableConfig); }

    public boolean isCommunicationNormal() { return communicationNormal; }
    public void setCommunicationNormal(boolean communicationNormal) { this.communicationNormal = communicationNormal; }

    public boolean isPowerStatusNormal() { return powerStatusNormal; }
    public void setPowerStatusNormal(boolean powerStatusNormal) { this.powerStatusNormal = powerStatusNormal; }

    // 路段管理变量的getter
    public int getSegmentCount() { return segmentCount; }
    public void setSegmentCount(int segmentCount) {
        this.segmentCount = segmentCount;
        initializeSegmentClearanceStates();
    }

    public Map<Integer, ClearanceState> getSegmentClearanceStates() { return new HashMap<>(segmentClearanceStates); }

    public int getClearedSegmentCount() { return clearedSegmentCount.get(); }

    public boolean isSegmentsAllReady() { return segmentsAllReady; }

    // 控制模式变量的getter和setter
    public ControlMode getCurrentControlMode() { return currentControlMode; }
    public void setCurrentControlMode(ControlMode currentControlMode) { this.currentControlMode = currentControlMode; }

    public boolean isInductiveAlgorithmActive() { return inductiveAlgorithmActive; }
    public void setInductiveAlgorithmActive(boolean inductiveAlgorithmActive) { this.inductiveAlgorithmActive = inductiveAlgorithmActive; }

    public boolean isManualControlActive() { return manualControlActive; }
    public void setManualControlActive(boolean manualControlActive) { this.manualControlActive = manualControlActive; }

    public boolean isEmergencySignalsActive() { return emergencySignalsActive; }
    public void setEmergencySignalsActive(boolean emergencySignalsActive) { this.emergencySignalsActive = emergencySignalsActive; }

    // 通信和外部接口变量的getter和setter
    public CommunicationStatus getCommunicationStatus() { return communicationStatus; }
    public void setCommunicationStatus(CommunicationStatus communicationStatus) {
        this.communicationStatus = communicationStatus;
        this.communicationNormal = (communicationStatus == CommunicationStatus.NORMAL);
    }

    public boolean isExternalSystemConnected() { return externalSystemConnected; }
    public void setExternalSystemConnected(boolean externalSystemConnected) { this.externalSystemConnected = externalSystemConnected; }

    public PowerStatus getPowerStatus() { return powerStatus; }
    public void setPowerStatus(PowerStatus powerStatus) {
        this.powerStatus = powerStatus;
        this.powerStatusNormal = (powerStatus == PowerStatus.NORMAL);
    }

    // 错误统计相关的getter
    public List<ErrorRecord> getErrorHistory() {
        synchronized (errorHistory) {
            return new ArrayList<>(errorHistory);
        }
    }

    public int getIdLogicErrorCount() { return idLogicErrorCount.get(); }
    public int getCounterMismatchErrorCount() { return counterMismatchErrorCount.get(); }
}
