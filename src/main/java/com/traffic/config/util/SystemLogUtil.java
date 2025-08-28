package com.traffic.config.util;

import com.traffic.config.entity.log.SystemLogEntity;
import com.traffic.config.entity.log.SystemLogEntity.LogLevel;
import com.traffic.config.entity.log.SystemLogEntity.LogType;
import com.traffic.config.service.log.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 系统日志工具类
 * 提供便捷的日志记录方法
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
//@ConditionalOnProperty(name = "traffic.log.database.enabled", havingValue = "true", matchIfMissing = true)
public class SystemLogUtil {

    @Autowired
    private SystemLogService logService;

    // ==================== 车辆事件日志 ====================

    public void logVehicleInfo(String content, String vehicleId) {
        logService.logVehicleEvent(LogLevel.INFO, content, vehicleId);
    }

    public void logVehicleWarn(String content, String vehicleId) {
        logService.logVehicleEvent(LogLevel.WARN, content, vehicleId);
    }

    public void logVehicleError(String content, String vehicleId) {
        logService.logVehicleEvent(LogLevel.ERROR, content, vehicleId);
    }

    public void logVehicleDebug(String content, String vehicleId) {
        logService.logVehicleEvent(LogLevel.DEBUG, content, vehicleId);
    }

    // ==================== 路段状态日志 ====================

    public void logSegmentInfo(String content, String segmentId) {
        logService.logSegmentStatus(LogLevel.INFO, content, segmentId);
    }

    public void logSegmentWarn(String content, String segmentId) {
        logService.logSegmentStatus(LogLevel.WARN, content, segmentId);
    }

    public void logSegmentError(String content, String segmentId) {
        logService.logSegmentStatus(LogLevel.ERROR, content, segmentId);
    }

    public void logSegmentDebug(String content, String segmentId) {
        logService.logSegmentStatus(LogLevel.DEBUG, content, segmentId);
    }

    // ==================== 冲突告警日志 ====================

    public void logConflictWarn(String content, String conflictId) {
        logService.logConflictAlert(LogLevel.WARN, content, conflictId);
    }

    public void logConflictError(String content, String conflictId) {
        logService.logConflictAlert(LogLevel.ERROR, content, conflictId);
    }

    public void logConflictInfo(String content, String conflictId) {
        logService.logConflictAlert(LogLevel.INFO, content, conflictId);
    }

    // ==================== 检测器错误日志 ====================

    public void logDetectorError(String content, String detectorId) {
        logService.logDetectorError(LogLevel.ERROR, content, detectorId);
    }

    public void logDetectorWarn(String content, String detectorId) {
        logService.logDetectorError(LogLevel.WARN, content, detectorId);
    }

    public void logDetectorInfo(String content, String detectorId) {
        logService.logDetectorError(LogLevel.INFO, content, detectorId);
    }

    // ==================== 传感器状态日志 ====================

    public void logSensorInfo(String content, String sensorId) {
        logService.logSensorStatus(LogLevel.INFO, content, sensorId);
    }

    public void logSensorWarn(String content, String sensorId) {
        logService.logSensorStatus(LogLevel.WARN, content, sensorId);
    }

    public void logSensorError(String content, String sensorId) {
        logService.logSensorStatus(LogLevel.ERROR, content, sensorId);
    }

    public void logSensorDebug(String content, String sensorId) {
        logService.logSensorStatus(LogLevel.DEBUG, content, sensorId);
    }

    // ==================== 系统事件日志 ====================

    public void logSystemInfo(String content) {
        logService.logSystemEvent(LogLevel.INFO, content);
    }

    public void logSystemWarn(String content) {
        logService.logSystemEvent(LogLevel.WARN, content);
    }

    public void logSystemError(String content) {
        logService.logSystemEvent(LogLevel.ERROR, content);
    }

    public void logSystemDebug(String content) {
        logService.logSystemEvent(LogLevel.DEBUG, content);
    }

    public void logSystemInfo(String content, String module, String entityId) {
        logService.logSystemEvent(LogLevel.INFO, content, module, entityId);
    }

    public void logSystemWarn(String content, String module, String entityId) {
        logService.logSystemEvent(LogLevel.WARN, content, module, entityId);
    }

    public void logSystemError(String content, String module, String entityId) {
        logService.logSystemEvent(LogLevel.ERROR, content, module, entityId);
    }

    // ==================== 批量日志记录 ====================

    public void logBatch(LogType type, LogLevel level, String content, String entityId, String module) {
        try {
            SystemLogEntity logEntity = SystemLogEntity.builder()
                    .logType(type)
                    .logLevel(level)
                    .logContent(content)
                    .entityId(entityId)
                    .module(module)
                    .createTime(LocalDateTime.now())
                    .build();

            logService.addLog(logEntity);
        } catch (Exception e) {
            log.error("记录系统日志失败: type={}, level={}, content={}", type, level, content, e);
        }
    }

    // ==================== 状态机集成方法 ====================

    /**
     * 记录状态机转换日志
     */
    public void logStateTransition(String fromState, String toState, String event, String segmentId) {
        String content = String.format("状态转换: %s -> %s (事件: %s)", fromState, toState, event);
        logSegmentInfo(content, segmentId);
    }

    /**
     * 记录系统启动日志
     */
    public void logSystemStart() {
        logSystemInfo("交通控制系统启动", "SystemCore", null);
    }

    /**
     * 记录系统关闭日志
     */
    public void logSystemShutdown() {
        logSystemInfo("交通控制系统关闭", "SystemCore", null);
    }

    /**
     * 记录配置加载日志
     */
    public void logConfigLoaded(String configPath) {
        String content = String.format("配置文件加载成功: %s", configPath);
        logSystemInfo(content, "ConfigService", null);
    }

    /**
     * 记录配置加载失败日志
     */
    public void logConfigLoadFailed(String configPath, String error) {
        String content = String.format("配置文件加载失败: %s, 错误: %s", configPath, error);
        logSystemError(content, "ConfigService", null);
    }
}