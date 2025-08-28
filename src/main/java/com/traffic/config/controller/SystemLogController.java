package com.traffic.config.controller;

import com.traffic.config.entity.log.SystemLogEntity;
import com.traffic.config.entity.log.SystemLogEntity.LogType;
import com.traffic.config.entity.log.SystemLogEntity.LogLevel;
import com.traffic.config.service.log.SystemLogService;
import com.traffic.config.vo.log.LogQueryCondition;
import com.traffic.config.vo.log.LogStatistics;
import com.traffic.config.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统日志REST控制器
 * 提供日志的增删改查API接口
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
public class SystemLogController {

    @Autowired
    private SystemLogService logService;

    // ==================== 日志查询接口 ====================

    /**
     * 分页查询系统日志
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SystemLogEntity>>> getLogs(
            @RequestParam(value = "logType", required = false) LogType logType,
            @RequestParam(value = "logLevel", required = false) LogLevel logLevel,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        try {
            LogQueryCondition condition = LogQueryCondition.builder()
                    .logType(logType)
                    .logLevel(logLevel)
                    .startTime(startTime)
                    .endTime(endTime)
                    .module(module)
                    .entityId(entityId)
                    .page(page)
                    .size(size)
                    .build();

            Page<SystemLogEntity> result = logService.findByConditions(condition);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("查询系统日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询日志失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemLogEntity>> getLogById(@PathVariable Long id) {
        try {
            Optional<SystemLogEntity> log = logService.findById(id);
            if (log.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(log.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("日志不存在: " + id));
            }
        } catch (Exception e) {
            log.error("获取日志详情失败: ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取日志详情失败: " + e.getMessage()));
        }
    }

    /**
     * 内容搜索
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SystemLogEntity>>> searchLogs(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        try {
            if (!StringUtils.hasText(keyword)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("搜索关键词不能为空"));
            }

            Page<SystemLogEntity> result = logService.searchByContent(keyword, page, size);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("搜索日志失败, keyword={}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取最新日志
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SystemLogEntity>>> getRecentLogs(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {

        try {
            if (limit <= 0 || limit > 1000) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("限制数量必须在1-1000之间"));
            }

            List<SystemLogEntity> logs = logService.findRecentLogs(limit);
            return ResponseEntity.ok(ApiResponse.success(logs));

        } catch (Exception e) {
            log.error("获取最新日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取最新日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取系统健康状态日志
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<List<SystemLogEntity>>> getHealthLogs(
            @RequestParam(value = "hours", defaultValue = "24") int hours) {

        try {
            if (hours <= 0 || hours > 168) { // 最多7天
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("时间范围必须在1-168小时之间"));
            }

            List<SystemLogEntity> logs = logService.findSystemHealthLogs(hours);
            return ResponseEntity.ok(ApiResponse.success(logs));

        } catch (Exception e) {
            log.error("获取系统健康日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取系统健康日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取活跃告警日志
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SystemLogEntity>>> getActiveAlerts(
            @RequestParam(value = "minutes", defaultValue = "30") int minutes) {

        try {
            if (minutes <= 0 || minutes > 1440) { // 最多24小时
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("时间范围必须在1-1440分钟之间"));
            }

            List<SystemLogEntity> alerts = logService.findActiveAlerts(minutes);
            return ResponseEntity.ok(ApiResponse.success(alerts));

        } catch (Exception e) {
            log.error("获取活跃告警失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取活跃告警失败: " + e.getMessage()));
        }
    }

    // ==================== 日志添加接口 ====================

    /**
     * 添加单个日志
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SystemLogEntity>> addLog(@Valid @RequestBody SystemLogEntity logEntity) {
        try {
            SystemLogEntity savedLog = logService.addLog(logEntity);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(savedLog, "日志添加成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("参数错误: " + e.getMessage()));
        } catch (Exception e) {
            log.error("添加日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加日志失败: " + e.getMessage()));
        }
    }

    /**
     * 批量添加日志
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<SystemLogEntity>>> addLogs(
            @Valid @RequestBody List<SystemLogEntity> logEntities) {
        try {
            if (logEntities.size() > 1000) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("批量添加日志数量不能超过1000条"));
            }

            List<SystemLogEntity> savedLogs = logService.addLogs(logEntities);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(savedLogs, "批量添加日志成功，数量: " + savedLogs.size()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("参数错误: " + e.getMessage()));
        } catch (Exception e) {
            log.error("批量添加日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量添加日志失败: " + e.getMessage()));
        }
    }

    // ==================== 便捷日志记录接口 ====================

    /**
     * 记录车辆事件
     */
    @PostMapping("/vehicle")
    public ResponseEntity<ApiResponse<String>> logVehicleEvent(
            @RequestParam("level") LogLevel level,
            @RequestParam("content") String content,
            @RequestParam("vehicleId") String vehicleId) {

        try {
            logService.logVehicleEvent(level, content, vehicleId);
            return ResponseEntity.ok(ApiResponse.success("车辆事件日志记录成功"));

        } catch (Exception e) {
            log.error("记录车辆事件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("记录车辆事件失败: " + e.getMessage()));
        }
    }

    /**
     * 记录路段状态
     */
    @PostMapping("/segment")
    public ResponseEntity<ApiResponse<String>> logSegmentStatus(
            @RequestParam("level") LogLevel level,
            @RequestParam("content") String content,
            @RequestParam("segmentId") String segmentId) {

        try {
            logService.logSegmentStatus(level, content, segmentId);
            return ResponseEntity.ok(ApiResponse.success("路段状态日志记录成功"));

        } catch (Exception e) {
            log.error("记录路段状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("记录路段状态失败: " + e.getMessage()));
        }
    }

    /**
     * 记录系统事件
     */
    @PostMapping("/system")
    public ResponseEntity<ApiResponse<String>> logSystemEvent(
            @RequestParam("level") LogLevel level,
            @RequestParam("content") String content,
            @RequestParam(value = "module", required = false, defaultValue = "SystemCore") String module,
            @RequestParam(value = "entityId", required = false) String entityId) {

        try {
            logService.logSystemEvent(level, content, module, entityId);
            return ResponseEntity.ok(ApiResponse.success("系统事件日志记录成功"));

        } catch (Exception e) {
            log.error("记录系统事件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("记录系统事件失败: " + e.getMessage()));
        }
    }

    // ==================== 日志删除接口 ====================

    /**
     * 删除单个日志
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteLog(@PathVariable Long id) {
        try {
            boolean deleted = logService.deleteById(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("日志删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("日志不存在: " + id));
            }
        } catch (Exception e) {
            log.error("删除日志失败: ID={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除日志失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除日志
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<String>> deleteLogs(@RequestBody List<Long> ids) {
        try {
            if (ids.size() > 1000) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("批量删除日志数量不能超过1000条"));
            }

            int deletedCount = logService.deleteByIds(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除日志成功，数量: " + deletedCount));

        } catch (Exception e) {
            log.error("批量删除日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("批量删除日志失败: " + e.getMessage()));
        }
    }

    /**
     * 删除过期日志
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cleanupLogs(
            @RequestParam(value = "days", defaultValue = "30") int days) {

        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("保留天数必须在1-365之间"));
            }

            int deletedCount = logService.deleteOldLogs(days);

            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("cutoffDays", days);
            result.put("cleanupTime", LocalDateTime.now());

            return ResponseEntity.ok(ApiResponse.success(result, "日志清理成功，删除数量: " + deletedCount));

        } catch (Exception e) {
            log.error("清理日志失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("清理日志失败: " + e.getMessage()));
        }
    }

    // ==================== 日志统计接口 ====================

    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<LogStatistics>> getLogStatistics() {
        try {
            LogStatistics statistics = logService.getLogStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics));

        } catch (Exception e) {
            log.error("获取日志统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取日志类型枚举
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLogTypes() {
        try {
            Map<String, String> types = new HashMap<>();
            for (LogType type : LogType.values()) {
                types.put(type.name(), type.getDescription());
            }
            return ResponseEntity.ok(ApiResponse.success(types));

        } catch (Exception e) {
            log.error("获取日志类型失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取日志类型失败: " + e.getMessage()));
        }
    }

    /**
     * 获取日志级别枚举
     */
    @GetMapping("/levels")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLogLevels() {
        try {
            Map<String, String> levels = new HashMap<>();
            for (LogLevel level : LogLevel.values()) {
                levels.put(level.name(), level.getDescription());
            }
            return ResponseEntity.ok(ApiResponse.success(levels));

        } catch (Exception e) {
            log.error("获取日志级别失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取日志级别失败: " + e.getMessage()));
        }
    }
}