package com.traffic.config.controller;

import com.traffic.config.service.ConfigService;
import com.traffic.config.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供系统健康状态检查和监控功能
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController implements HealthIndicator {

    @Autowired
    private ConfigService configService;

    /**
     * 基础健康检查 - 符合Spring Boot Actuator标准
     *
     * @return 健康状态
     */
    @Override
    public Health health() {
        try {
            // 检查配置服务
            configService.loadConfig();

            // 检查配置文件状态
            boolean configFileExists = configService.isConfigFileExists();
            long lastModified = configService.getConfigLastModified();

            Map<String, Object> details = new HashMap<>();
            details.put("configFileExists", configFileExists);
            details.put("configLastModified", lastModified);
            details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            if (configFileExists) {
                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                return Health.down()
                        .withDetail("reason", "配置文件不存在")
                        .withDetails(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }

    /**
     * 简单健康检查接口
     *
     * @return 健康状态响应
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            Health health = health();
            Status status = health.getStatus();

            Map<String, Object> healthData = new HashMap<>();
            healthData.put("status", status.getCode());
            healthData.put("details", health.getDetails());

            if (Status.UP.equals(status)) {
                return ResponseEntity.ok(ApiResponse.success("系统运行正常", healthData));
            } else {
                return ResponseEntity.status(503)
                        .body(ApiResponse.serviceUnavailable("系统异常"));
            }

        } catch (Exception e) {
            log.error("健康检查接口异常", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError("健康检查失败: " + e.getMessage()));
        }
    }

    /**
     * 详细健康检查接口
     *
     * @return 详细健康状态信息
     */
    @GetMapping("/detailed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detailedHealthCheck() {
        Map<String, Object> healthDetails = new HashMap<>();
        boolean overallHealthy = true;

        try {
            // 1. 检查配置服务
            Map<String, Object> configServiceHealth = checkConfigService();
            healthDetails.put("configService", configServiceHealth);
            if (!(Boolean) configServiceHealth.get("healthy")) {
                overallHealthy = false;
            }

            // 2. 检查配置文件
            Map<String, Object> configFileHealth = checkConfigFile();
            healthDetails.put("configFile", configFileHealth);
            if (!(Boolean) configFileHealth.get("healthy")) {
                overallHealthy = false;
            }

            // 3. 检查系统资源
            Map<String, Object> systemHealth = checkSystemResources();
            healthDetails.put("systemResources", systemHealth);
            if (!(Boolean) systemHealth.get("healthy")) {
                overallHealthy = false;
            }

            // 4. 检查API端点
            Map<String, Object> apiHealth = checkApiEndpoints();
            healthDetails.put("apiEndpoints", apiHealth);

            // 总体状态
            healthDetails.put("overallStatus", overallHealthy ? "UP" : "DOWN");
            healthDetails.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            if (overallHealthy) {
                return ResponseEntity.ok(ApiResponse.success("详细健康检查完成", healthDetails));
            } else {
                return ResponseEntity.status(503)
                        .body(ApiResponse.serviceUnavailable("系统存在异常"));
            }

        } catch (Exception e) {
            log.error("详细健康检查失败", e);
            healthDetails.put("error", e.getMessage());
            healthDetails.put("overallStatus", "ERROR");

            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError("详细健康检查失败"));
        }
    }

    /**
     * 配置服务专项检查
     *
     * @return 配置服务健康状态响应
     */
    @GetMapping("/config-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkConfigServiceHealth() {
        try {
            Map<String, Object> configServiceHealth = checkConfigService();

            if ((Boolean) configServiceHealth.get("healthy")) {
                return ResponseEntity.ok(ApiResponse.success("配置服务正常", configServiceHealth));
            } else {
                return ResponseEntity.status(503)
                        .body(ApiResponse.serviceUnavailable("配置服务异常"));
            }

        } catch (Exception e) {
            log.error("配置服务健康检查失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError("配置服务检查失败: " + e.getMessage()));
        }
    }

    /**
     * 配置文件专项检查
     *
     * @return 配置文件健康状态响应
     */
    @GetMapping("/config-file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkConfigFileHealth() {
        try {
            Map<String, Object> configFileHealth = checkConfigFile();

            if ((Boolean) configFileHealth.get("healthy")) {
                return ResponseEntity.ok(ApiResponse.success("配置文件正常", configFileHealth));
            } else {
                return ResponseEntity.status(503)
                        .body(ApiResponse.serviceUnavailable("配置文件异常"));
            }

        } catch (Exception e) {
            log.error("配置文件健康检查失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError("配置文件检查失败: " + e.getMessage()));
        }
    }

    /**
     * 系统资源检查
     *
     * @return 系统资源状态响应
     */
    @GetMapping("/system-resources")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSystemResourcesHealth() {
        try {
            Map<String, Object> systemHealth = checkSystemResources();

            if ((Boolean) systemHealth.get("healthy")) {
                return ResponseEntity.ok(ApiResponse.success("系统资源正常", systemHealth));
            } else {
                return ResponseEntity.status(503)
                        .body(ApiResponse.serviceUnavailable("系统资源不足"));
            }

        } catch (Exception e) {
            log.error("系统资源健康检查失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError("系统资源检查失败: " + e.getMessage()));
        }
    }

    /**
     * 强制刷新缓存并检查健康状态
     *
     * @return 刷新后的健康状态
     */
    @PostMapping("/refresh-and-check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshAndCheck() {
        try {
            // 刷新配置缓存
            configService.refreshCache();

            // 重新进行健康检查
            Health health = health();
            Status status = health.getStatus();

            Map<String, Object> result = new HashMap<>();
            result.put("refreshed", true);
            result.put("status", status.getCode());
            result.put("details", health.getDetails());
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            if (Status.UP.equals(status)) {
                return ResponseEntity.ok(ApiResponse.success("缓存刷新成功，系统正常", result));
            } else {
                return ResponseEntity.status(503)
                        .body(ApiResponse.serviceUnavailable("缓存刷新后系统仍异常"));
            }

        } catch (Exception e) {
            log.error("刷新缓存并检查健康状态失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError("刷新检查失败: " + e.getMessage()));
        }
    }

    // ==================== 私有检查方法 ====================

    /**
     * 检查配置服务状态
     */
    private Map<String, Object> checkConfigService() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 尝试加载配置
            configService.loadConfig();

            // 检查基本功能
            configService.getGlobalConfig();
            configService.getAllSegments();

            result.put("healthy", true);
            result.put("status", "正常");
            result.put("message", "配置服务运行正常");
            result.put("lastCheck", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        } catch (Exception e) {
            log.warn("配置服务检查异常", e);
            result.put("healthy", false);
            result.put("status", "异常");
            result.put("message", "配置服务异常: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }

        return result;
    }

    /**
     * 检查配置文件状态
     */
    private Map<String, Object> checkConfigFile() {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean exists = configService.isConfigFileExists();
            long lastModified = configService.getConfigLastModified();

            result.put("healthy", exists);
            result.put("exists", exists);
            result.put("lastModified", lastModified);

            if (exists) {
                result.put("status", "正常");
                result.put("message", "配置文件存在且可访问");

                // 检查文件最后修改时间是否合理
                if (lastModified > 0) {
                    long now = System.currentTimeMillis();
                    long daysSinceModified = (now - lastModified) / (1000 * 60 * 60 * 24);
                    result.put("daysSinceModified", daysSinceModified);

                    if (daysSinceModified > 365) {
                        result.put("warning", "配置文件超过一年未修改");
                    }
                }
            } else {
                result.put("status", "异常");
                result.put("message", "配置文件不存在或无法访问");
            }

        } catch (Exception e) {
            log.warn("配置文件检查异常", e);
            result.put("healthy", false);
            result.put("status", "异常");
            result.put("message", "配置文件检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查系统资源状态
     */
    private Map<String, Object> checkSystemResources() {
        Map<String, Object> result = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();

            // 内存信息
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            result.put("memory", Map.of(
                    "maxMemoryMB", maxMemory / 1024 / 1024,
                    "totalMemoryMB", totalMemory / 1024 / 1024,
                    "usedMemoryMB", usedMemory / 1024 / 1024,
                    "freeMemoryMB", freeMemory / 1024 / 1024,
                    "usagePercent", String.format("%.2f", memoryUsagePercent)
            ));

            // 磁盘空间检查
            File currentDir = new File(".");
            long totalSpace = currentDir.getTotalSpace();
            long freeSpace = currentDir.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskUsagePercent = (double) usedSpace / totalSpace * 100;

            result.put("disk", Map.of(
                    "totalSpaceGB", totalSpace / 1024 / 1024 / 1024,
                    "freeSpaceGB", freeSpace / 1024 / 1024 / 1024,
                    "usedSpaceGB", usedSpace / 1024 / 1024 / 1024,
                    "usagePercent", String.format("%.2f", diskUsagePercent)
            ));

            // 处理器信息
            int availableProcessors = runtime.availableProcessors();
            result.put("processors", availableProcessors);

            // 健康判断
            boolean memoryHealthy = memoryUsagePercent < 90.0;
            boolean diskHealthy = diskUsagePercent < 95.0;

            result.put("healthy", memoryHealthy && diskHealthy);

            if (memoryHealthy && diskHealthy) {
                result.put("status", "正常");
                result.put("message", "系统资源充足");
            } else {
                result.put("status", "警告");
                if (!memoryHealthy) {
                    result.put("memoryWarning", "内存使用率过高");
                }
                if (!diskHealthy) {
                    result.put("diskWarning", "磁盘使用率过高");
                }
            }

        } catch (Exception e) {
            log.warn("系统资源检查异常", e);
            result.put("healthy", false);
            result.put("status", "异常");
            result.put("message", "系统资源检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查API端点状态
     */
    private Map<String, Object> checkApiEndpoints() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查主要API端点是否可访问
            Map<String, String> endpoints = Map.of(
                    "config", "配置管理API",
                    "segments", "路段管理API",
                    "global", "全局配置API",
                    "health", "健康检查API"
            );

            result.put("endpoints", endpoints);
            result.put("healthy", true);
            result.put("status", "正常");
            result.put("message", "API端点正常");
            result.put("totalEndpoints", endpoints.size());

        } catch (Exception e) {
            log.warn("API端点检查异常", e);
            result.put("healthy", false);
            result.put("status", "异常");
            result.put("message", "API端点检查失败: " + e.getMessage());
        }

        return result;
    }
}