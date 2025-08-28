package com.traffic.config.service.log;

import com.traffic.config.entity.log.SystemLogEntity;
import com.traffic.config.entity.log.SystemLogEntity.LogType;
import com.traffic.config.entity.log.SystemLogEntity.LogLevel;
import com.traffic.config.repository.SystemLogRepository;
import com.traffic.config.vo.log.LogQueryCondition;
import com.traffic.config.vo.log.LogStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统日志业务服务
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
public class SystemLogService {

    @Autowired
    private SystemLogRepository logRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 日志添加方法 ====================

    /**
     * 添加系统日志
     */
    public SystemLogEntity addLog(SystemLogEntity logEntity) {
        if (logEntity == null) {
            throw new IllegalArgumentException("日志实体不能为空");
        }

        // 设置默认值
        if (logEntity.getCreateTime() == null) {
            logEntity.setCreateTime(LocalDateTime.now());
        }

        try {
            SystemLogEntity savedLog = logRepository.save(logEntity);
            log.debug("添加系统日志成功: ID={}, 类型={}, 级别={}",
                    savedLog.getId(), savedLog.getLogType(), savedLog.getLogLevel());
            return savedLog;
        } catch (Exception e) {
            log.error("添加系统日志失败", e);
            throw new RuntimeException("添加系统日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量添加日志
     */
    public List<SystemLogEntity> addLogs(List<SystemLogEntity> logEntities) {
        if (logEntities == null || logEntities.isEmpty()) {
            throw new IllegalArgumentException("日志实体列表不能为空");
        }

        // 设置默认创建时间
        LocalDateTime now = LocalDateTime.now();
        logEntities.forEach(entity -> {
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(now);
            }
        });

        try {
            List<SystemLogEntity> savedLogs = logRepository.saveAll(logEntities);
            log.debug("批量添加系统日志成功，数量: {}", savedLogs.size());
            return savedLogs;
        } catch (Exception e) {
            log.error("批量添加系统日志失败", e);
            throw new RuntimeException("批量添加系统日志失败: " + e.getMessage(), e);
        }
    }

    // ==================== 便捷日志记录方法 ====================

    /**
     * 记录车辆事件日志
     */
    public void logVehicleEvent(LogLevel level, String content, String vehicleId) {
        SystemLogEntity logEntity = SystemLogEntity.createVehicleLog(level, content, vehicleId);
        logEntity.setModule("VehicleManager");
        addLog(logEntity);
    }

    /**
     * 记录路段状态日志
     */
    public void logSegmentStatus(LogLevel level, String content, String segmentId) {
        SystemLogEntity logEntity = SystemLogEntity.createSegmentLog(level, content, segmentId);
        logEntity.setModule("SegmentStateMachine");
        addLog(logEntity);
    }

    /**
     * 记录冲突告警日志
     */
    public void logConflictAlert(LogLevel level, String content, String conflictId) {
        SystemLogEntity logEntity = SystemLogEntity.builder()
                .logType(LogType.CONFLICT_ALERT)
                .logLevel(level)
                .logContent(content)
                .entityId(conflictId)
                .module("ConflictDetector")
                .createTime(LocalDateTime.now())
                .build();
        addLog(logEntity);
    }

    /**
     * 记录检测器错误日志
     */
    public void logDetectorError(LogLevel level, String content, String detectorId) {
        SystemLogEntity logEntity = SystemLogEntity.builder()
                .logType(LogType.DETECTOR_ERROR)
                .logLevel(level)
                .logContent(content)
                .entityId(detectorId)
                .module("DetectorManager")
                .createTime(LocalDateTime.now())
                .build();
        addLog(logEntity);
    }

    /**
     * 记录传感器状态日志
     */
    public void logSensorStatus(LogLevel level, String content, String sensorId) {
        SystemLogEntity logEntity = SystemLogEntity.builder()
                .logType(LogType.SENSOR_STATUS)
                .logLevel(level)
                .logContent(content)
                .entityId(sensorId)
                .module("SensorManager")
                .createTime(LocalDateTime.now())
                .build();
        addLog(logEntity);
    }

    /**
     * 记录系统事件日志
     */
    public void logSystemEvent(LogLevel level, String content) {
        logSystemEvent(level, content, "SystemCore", null);
    }

    /**
     * 记录系统事件日志（带模块和实体ID）
     */
    public void logSystemEvent(LogLevel level, String content, String module, String entityId) {
        SystemLogEntity logEntity = SystemLogEntity.createSystemLog(level, content);
        logEntity.setModule(module);
        logEntity.setEntityId(entityId);
        addLog(logEntity);
    }

    // ==================== 日志查询方法 ====================

    /**
     * 根据ID查询日志
     */
    @Transactional(readOnly = true)
    public Optional<SystemLogEntity> findById(Long id) {
        return logRepository.findById(id);
    }

    /**
     * 条件查询日志（分页）
     */
    @Transactional(readOnly = true)
    public Page<SystemLogEntity> findByConditions(LogQueryCondition condition) {
        Pageable pageable = PageRequest.of(
                condition.getPage(),
                condition.getSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        return logRepository.findByConditions(
                condition.getLogType(),
                condition.getLogLevel(),
                condition.getStartTime(),
                condition.getEndTime(),
                condition.getModule(),
                condition.getEntityId(),
                pageable
        );
    }

    /**
     * 内容搜索
     */
    @Transactional(readOnly = true)
    public Page<SystemLogEntity> searchByContent(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return logRepository.findByContentContaining(keyword, pageable);
    }

    /**
     * 获取最近N条日志
     */
    @Transactional(readOnly = true)
    public List<SystemLogEntity> findRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return logRepository.findTopNByOrderByCreateTimeDesc(pageable);
    }

    /**
     * 获取系统健康状态日志
     */
    @Transactional(readOnly = true)
    public List<SystemLogEntity> findSystemHealthLogs(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return logRepository.findSystemHealthLogs(startTime);
    }

    /**
     * 获取活跃告警日志
     */
    @Transactional(readOnly = true)
    public List<SystemLogEntity> findActiveAlerts(int minutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        return logRepository.findActiveAlerts(startTime);
    }

    // ==================== 日志删除方法 ====================

    /**
     * 根据ID删除日志
     */
    public boolean deleteById(Long id) {
        try {
            if (logRepository.existsById(id)) {
                logRepository.deleteById(id);
                log.debug("删除日志成功: ID={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除日志失败: ID={}", id, e);
            throw new RuntimeException("删除日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除日志
     */
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        try {
            List<SystemLogEntity> logs = logRepository.findAllById(ids);
            logRepository.deleteAll(logs);
            log.debug("批量删除日志成功，数量: {}", logs.size());
            return logs.size();
        } catch (Exception e) {
            log.error("批量删除日志失败", e);
            throw new RuntimeException("批量删除日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除指定时间之前的日志
     */
    public int deleteOldLogs(LocalDateTime cutoffTime) {
        try {
            int deletedCount = logRepository.deleteByCreateTimeBefore(cutoffTime);
            log.info("删除过期日志成功，删除数量: {}, 截止时间: {}",
                    deletedCount, cutoffTime.format(FORMATTER));
            return deletedCount;
        } catch (Exception e) {
            log.error("删除过期日志失败", e);
            throw new RuntimeException("删除过期日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除指定天数之前的日志
     */
    public int deleteOldLogs(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        return deleteOldLogs(cutoffTime);
    }

    // ==================== 统计方法 ====================

    /**
     * 获取日志统计信息
     */
    @Transactional(readOnly = true)
    public LogStatistics getLogStatistics() {
        LogStatistics statistics = new LogStatistics();

        // 总数统计
        statistics.setTotalCount(logRepository.count());
        statistics.setErrorCount(logRepository.countErrorLogs());
        statistics.setWarnCount(logRepository.countWarnLogs());

        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        logRepository.countByLogType().forEach(result -> {
            LogType type = (LogType) result[0];
            Long count = (Long) result[1];
            typeStats.put(type.name(), count);
        });
        statistics.setTypeStatistics(typeStats);

        // 按级别统计
        Map<String, Long> levelStats = new HashMap<>();
        logRepository.countByLogLevel().forEach(result -> {
            LogLevel level = (LogLevel) result[0];
            Long count = (Long) result[1];
            levelStats.put(level.name(), count);
        });
        statistics.setLevelStatistics(levelStats);

        // 今日统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        statistics.setTodayCount(logRepository.countByTimeRange(todayStart, todayEnd));

        return statistics;
    }

    // ==================== 日志清理方法 ====================

    /**
     * 自动清理过期日志
     * 删除指定天数之前的日志，但保留ERROR级别的日志更长时间
     */
    public Map<String, Integer> autoCleanLogs(int normalRetainDays, int errorRetainDays) {
        Map<String, Integer> result = new HashMap<>();

        try {
            // 删除普通日志（INFO, DEBUG, WARN）
            LocalDateTime normalCutoff = LocalDateTime.now().minusDays(normalRetainDays);
            int normalDeleted = deleteOldNormalLogs(normalCutoff);
            result.put("normal", normalDeleted);

            // 删除错误日志
            LocalDateTime errorCutoff = LocalDateTime.now().minusDays(errorRetainDays);
            int errorDeleted = deleteOldErrorLogs(errorCutoff);
            result.put("error", errorDeleted);

            log.info("自动清理日志完成 - 普通日志: {}条, 错误日志: {}条", normalDeleted, errorDeleted);

        } catch (Exception e) {
            log.error("自动清理日志失败", e);
            throw new RuntimeException("自动清理日志失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 删除指定时间之前的普通级别日志
     */
    private int deleteOldNormalLogs(LocalDateTime cutoffTime) {
        // 这里需要自定义查询，删除非ERROR级别的旧日志
        // 由于JPA限制，使用原生SQL或者分批处理
        List<SystemLogEntity> oldNormalLogs = logRepository.findAll().stream()
                .filter(log -> log.getCreateTime().isBefore(cutoffTime))
                .filter(log -> !LogLevel.ERROR.equals(log.getLogLevel()))
                .toList();

        if (!oldNormalLogs.isEmpty()) {
            logRepository.deleteAll(oldNormalLogs);
        }

        return oldNormalLogs.size();
    }

    /**
     * 删除指定时间之前的错误级别日志
     */
    private int deleteOldErrorLogs(LocalDateTime cutoffTime) {
        List<SystemLogEntity> oldErrorLogs = logRepository.findAll().stream()
                .filter(log -> log.getCreateTime().isBefore(cutoffTime))
                .filter(log -> LogLevel.ERROR.equals(log.getLogLevel()))
                .toList();

        if (!oldErrorLogs.isEmpty()) {
            logRepository.deleteAll(oldErrorLogs);
        }

        return oldErrorLogs.size();
    }
}