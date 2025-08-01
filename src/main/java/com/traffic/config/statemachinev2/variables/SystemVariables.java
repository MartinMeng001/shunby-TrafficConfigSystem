package com.traffic.config.statemachinev2.variables;

import com.traffic.config.statemachinev2.enums.SystemStateV2;
import com.traffic.config.statemachinev2.constants.SystemConstants;
import com.traffic.config.statemachine.enums.ClearanceDecision;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统变量集合, 这里也有参数来源于配置文件
 * 对应数学模型中的变量集合 V_sys
 *
 * 这个类包含了系统状态机运行时需要的所有变量
 *
 * @author System
 * @version 2.0.0
 */
public class SystemVariables {

    // ==================== 时间变量 (Time Variables) ====================

    /**
     * 当前状态开始时间
     */
    private LocalDateTime stateStartTime;

    /**
     * 过渡开始时间
     */
    private LocalDateTime transitionStartTime;

    /**
     * 最后故障时间
     */
    private LocalDateTime lastFaultTime;

    /**
     * 恢复开始时间
     */
    private LocalDateTime recoveryStartTime;

    /**
     * 最后清空检查时间
     */
    private LocalDateTime lastClearCheckTime;

    /**
     * 系统初始化开始时间
     */
    private LocalDateTime systemInitStartTime;

    // ==================== 计数器变量 (Counter Variables) ====================

    /**
     * 时间窗口内错误计数
     */
    private int errorCountWindow;

    /**
     * 连续故障次数
     */
    private int consecutiveFaults;

    /**
     * 恢复尝试次数
     */
    private int recoveryAttempts;

    /**
     * 稳定运行时间（秒）
     */
    private long stableOperationTime;

    /**
     * 清空超时计数
     */
    private int clearTimeoutCount;

    /**
     * ID逻辑错误计数
     */
    private int idLogicErrorCount;

    /**
     * 计数器不匹配错误计数
     */
    private int counterMismatchErrorCount;

    // ==================== 状态标记变量 (State Flag Variables) ====================

    /**
     * 前一个状态
     */
    private SystemStateV2 previousState;

    /**
     * 故障来源标识
     */
    private String faultSource;

    /**
     * 维护类型
     */
    private String maintenanceType;

    /**
     * 紧急级别 (1-5, 5为最高)
     */
    private int emergencyLevel;

    /**
     * 清空检测是否激活
     */
    private boolean clearDetectionActive;

    /**
     * 自动恢复是否启用
     */
    private boolean autoRecoveryEnabled;

    // ==================== 路段清空相关变量 (Segment Clearance Variables) ====================

    /**
     * 所有路段是否已清空
     */
    private boolean segmentsCleared;

    /**
     * 路段总数
     */
    private int segmentCount;

    /**
     * 已清空路段数
     */
    private int clearedCount;

    /**
     * 各路段清空状态
     * Key: 路段ID, Value: 清空状态
     */
    private Map<String, ClearanceDecision> segmentClearStatus;

    /**
     * 路段状态最后更新时间
     * Key: 路段ID, Value: 更新时间
     */
    private Map<String, LocalDateTime> segmentLastUpdateTime;

    // ==================== 系统健康度变量 (System Health Variables) ====================

    /**
     * 系统健康度评分 (0-100)
     */
    private int systemHealthScore;

    /**
     * 性能降级程度 (0.0-1.0)
     */
    private double performanceDegradation;

    /**
     * 最后稳定配置
     */
    private Map<String, Object> lastStableConfig;

    /**
     * 通信状态是否正常
     */
    private boolean communicationNormal;

    /**
     * 电源状态是否正常
     */
    private boolean powerStatusNormal;

    // ==================== 错误统计变量 (Error Statistics Variables) ====================

    /**
     * 错误历史记录
     * 用于错误窗口统计
     */
    private List<ErrorRecord> errorHistory;

    /**
     * 最大错误历史记录数
     */
    private static final int MAX_ERROR_HISTORY = 1000;

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
        // 时间变量初始化
        LocalDateTime now = LocalDateTime.now();
        this.stateStartTime = now;
        this.systemInitStartTime = now;
        this.transitionStartTime = null;
        this.lastFaultTime = null;
        this.recoveryStartTime = null;
        this.lastClearCheckTime = null;

        // 计数器变量初始化
        this.errorCountWindow = 0;
        this.consecutiveFaults = 0;
        this.recoveryAttempts = 0;
        this.stableOperationTime = 0;
        this.clearTimeoutCount = 0;
        this.idLogicErrorCount = 0;
        this.counterMismatchErrorCount = 0;

        // 状态标记变量初始化
        this.previousState = null;
        this.faultSource = null;
        this.maintenanceType = null;
        this.emergencyLevel = 0;
        this.clearDetectionActive = false;
        this.autoRecoveryEnabled = true;

        // 路段清空变量初始化
        this.segmentsCleared = false;
        this.segmentCount = 0;
        this.clearedCount = 0;
        this.segmentClearStatus = new ConcurrentHashMap<>();
        this.segmentLastUpdateTime = new ConcurrentHashMap<>();

        // 系统健康度变量初始化
        this.systemHealthScore = SystemConstants.INITIAL_HEALTH_SCORE;
        this.performanceDegradation = 0.0;
        this.lastStableConfig = new HashMap<>();
        this.communicationNormal = true;
        this.powerStatusNormal = true;

        // 错误统计变量初始化
        this.errorHistory = new ArrayList<>();
    }

    // ==================== 时间相关方法 ====================

    /**
     * 更新状态开始时间
     * @param newState 新状态
     */
    public void updateStateStartTime(SystemStateV2 newState) {
        this.previousState = getCurrentStateFromTime(); // 根据当前时间推断当前状态
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
        return getTransitionDurationSeconds() >= SystemConstants.TRANSITION_TIME && segmentsCleared;
    }

    // ==================== 路段清空相关方法 ====================

    /**
     * 更新路段清空状态
     * @param segmentId 路段ID
     * @param clearanceDecision 清空决策
     */
    public void updateSegmentClearStatus(String segmentId, ClearanceDecision clearanceDecision) {
        segmentClearStatus.put(segmentId, clearanceDecision);
        segmentLastUpdateTime.put(segmentId, LocalDateTime.now());
        lastClearCheckTime = LocalDateTime.now();

        // 重新计算总体清空状态
        recalculateClearanceStatus();
    }

    /**
     * 重新计算总体清空状态
     */
    private void recalculateClearanceStatus() {
        if (segmentClearStatus.isEmpty()) {
            segmentsCleared = false;
            clearedCount = 0;
            return;
        }

        long safelyClearedCount = segmentClearStatus.values().stream()
                .mapToLong(status -> status == ClearanceDecision.CLEARED_SAFE ? 1 : 0)
                .sum();

        clearedCount = (int) safelyClearedCount;
        segmentsCleared = (clearedCount == segmentCount) && (segmentCount > 0);
    }

    /**
     * 设置路段总数
     * @param count 路段总数
     */
    public void setSegmentCount(int count) {
        this.segmentCount = count;
        recalculateClearanceStatus();
    }

    /**
     * 获取清空完成百分比
     * @return 清空完成百分比 (0.0-1.0)
     */
    public double getClearanceCompletionPercentage() {
        if (segmentCount == 0) {
            return 0.0;
        }
        return (double) clearedCount / segmentCount;
    }

    // ==================== 错误统计相关方法 ====================

    /**
     * 记录错误
     * @param errorType 错误类型
     * @param description 错误描述
     */
    public void recordError(String errorType, String description) {
        ErrorRecord error = new ErrorRecord(errorType, description, LocalDateTime.now());
        errorHistory.add(error);

        // 限制历史记录大小
        if (errorHistory.size() > MAX_ERROR_HISTORY) {
            errorHistory.remove(0);
        }

        // 更新错误计数
        updateErrorCounts();
    }

    /**
     * 更新错误计数（基于时间窗口）
     */
    private void updateErrorCounts() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(SystemConstants.ERROR_WINDOW);

        // 清理过期的错误记录
        errorHistory.removeIf(error -> error.getTimestamp().isBefore(cutoffTime));

        // 重新计算错误计数
        errorCountWindow = errorHistory.size();
        counterMismatchErrorCount = (int) errorHistory.stream()
                .filter(error -> "COUNTER_MISMATCH".equals(error.getErrorType()))
                .count();
        idLogicErrorCount = (int) errorHistory.stream()
                .filter(error -> "ID_LOGIC_ERROR".equals(error.getErrorType()))
                .count();
    }

    /**
     * 检查是否应该退出感应模式
     * @return 是否应该退出
     */
    public boolean shouldExitInductiveMode() {
        updateErrorCounts();
        return counterMismatchErrorCount >= SystemConstants.MAX_MISMATCH_ERRORS ||
                idLogicErrorCount >= SystemConstants.MAX_ID_ERRORS ||
                consecutiveFaults >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT;
    }

    // ==================== 系统健康度相关方法 ====================

    /**
     * 更新系统健康度评分
     * @param delta 评分变化量
     */
    public void updateHealthScore(int delta) {
        systemHealthScore = SystemConstants.clampHealthScore(systemHealthScore + delta);
    }

    /**
     * 检查系统是否健康
     * @return 是否健康
     */
    public boolean isSystemHealthy() {
        return systemHealthScore >= SystemConstants.RECOVERY_HEALTH_THRESHOLD &&
                communicationNormal && powerStatusNormal;
    }

    /**
     * 检查是否为严重故障状态
     * @return 是否为严重故障
     */
    public boolean isCriticalFault() {
        return systemHealthScore < SystemConstants.CRITICAL_HEALTH_THRESHOLD ||
                !communicationNormal || !powerStatusNormal ||
                consecutiveFaults >= SystemConstants.CONSECUTIVE_TIMEOUT_LIMIT;
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
                errorCountWindow < (SystemConstants.MAX_MISMATCH_ERRORS * SystemConstants.ERROR_RESET_THRESHOLD) &&
                isSystemHealthy();
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据当前时间推断当前状态（简化实现）
     * @return 推断的当前状态
     */
    private SystemStateV2 getCurrentStateFromTime() {
        // 这里是简化实现，实际应该从状态机获取
        return SystemStateV2.SYSTEM_INIT;
    }

    /**
     * 重置所有计数器
     */
    public void resetCounters() {
        this.errorCountWindow = 0;
        this.consecutiveFaults = 0;
        this.recoveryAttempts = 0;
        this.clearTimeoutCount = 0;
        this.idLogicErrorCount = 0;
        this.counterMismatchErrorCount = 0;
        this.errorHistory.clear();
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
                "健康度: %d/100, 错误计数: %d, 连续故障: %d, 路段清空: %d/%d, 通信: %s, 电源: %s",
                systemHealthScore, errorCountWindow, consecutiveFaults,
                clearedCount, segmentCount,
                communicationNormal ? "正常" : "异常",
                powerStatusNormal ? "正常" : "异常"
        );
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

    public LocalDateTime getLastClearCheckTime() { return lastClearCheckTime; }
    public void setLastClearCheckTime(LocalDateTime lastClearCheckTime) { this.lastClearCheckTime = lastClearCheckTime; }

    // 计数器变量的getter和setter
    public int getErrorCountWindow() { return errorCountWindow; }
    public void setErrorCountWindow(int errorCountWindow) { this.errorCountWindow = errorCountWindow; }

    public int getConsecutiveFaults() { return consecutiveFaults; }
    public void setConsecutiveFaults(int consecutiveFaults) { this.consecutiveFaults = consecutiveFaults; }

    public int getRecoveryAttempts() { return recoveryAttempts; }
    public void setRecoveryAttempts(int recoveryAttempts) { this.recoveryAttempts = recoveryAttempts; }

    public long getStableOperationTime() { return stableOperationTime; }
    public void setStableOperationTime(long stableOperationTime) { this.stableOperationTime = stableOperationTime; }

    // 状态标记变量的getter和setter
    public SystemStateV2 getPreviousState() { return previousState; }
    public void setPreviousState(SystemStateV2 previousState) { this.previousState = previousState; }

    public String getFaultSource() { return faultSource; }
    public void setFaultSource(String faultSource) { this.faultSource = faultSource; }

    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }

    public int getEmergencyLevel() { return emergencyLevel; }
    public void setEmergencyLevel(int emergencyLevel) { this.emergencyLevel = emergencyLevel; }

    public boolean isClearDetectionActive() { return clearDetectionActive; }
    public void setClearDetectionActive(boolean clearDetectionActive) { this.clearDetectionActive = clearDetectionActive; }

    public boolean isAutoRecoveryEnabled() { return autoRecoveryEnabled; }
    public void setAutoRecoveryEnabled(boolean autoRecoveryEnabled) { this.autoRecoveryEnabled = autoRecoveryEnabled; }

    // 路段清空变量的getter
    public boolean isSegmentsCleared() { return segmentsCleared; }
    public int getSegmentCount() { return segmentCount; }
    public int getClearedCount() { return clearedCount; }
    public Map<String, ClearanceDecision> getSegmentClearStatus() { return new HashMap<>(segmentClearStatus); }

    // 系统健康度变量的getter和setter
    public int getSystemHealthScore() { return systemHealthScore; }
    public void setSystemHealthScore(int systemHealthScore) {
        this.systemHealthScore = SystemConstants.clampHealthScore(systemHealthScore);
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

    // ==================== 内部类：错误记录 ====================

    /**
     * 错误记录类
     */
    public static class ErrorRecord {
        private final String errorType;
        private final String description;
        private final LocalDateTime timestamp;

        public ErrorRecord(String errorType, String description, LocalDateTime timestamp) {
            this.errorType = errorType;
            this.description = description;
            this.timestamp = timestamp;
        }

        public String getErrorType() { return errorType; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", timestamp, errorType, description);
        }
    }
}
