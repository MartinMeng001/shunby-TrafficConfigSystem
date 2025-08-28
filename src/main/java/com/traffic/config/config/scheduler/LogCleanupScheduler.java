package com.traffic.config.scheduler;

import com.traffic.config.configuration.SystemLogProperties;
import com.traffic.config.service.log.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 日志定时清理任务
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "traffic.log.database.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class LogCleanupScheduler {

    @Autowired
    private SystemLogService logService;

    @Autowired
    private SystemLogProperties logProperties;

    /**
     * 定时清理过期日志
     * 默认每天凌晨2点执行
     */
    @Scheduled(cron = "${traffic.log.database.cleanup.cron:0 0 2 * * ?}")
    public void cleanupExpiredLogs() {
        int normalRetainDays = logProperties.getDatabase().getCleanup().getNormalRetainDays();
        int errorRetainDays = logProperties.getDatabase().getCleanup().getErrorRetainDays();

        log.info("开始执行日志清理任务，普通日志保留{}天，错误日志保留{}天", normalRetainDays, errorRetainDays);

        try {
            Map<String, Integer> result = logService.autoCleanLogs(normalRetainDays, errorRetainDays);

            int totalDeleted = result.values().stream().mapToInt(Integer::intValue).sum();
            log.info("日志清理任务完成，共删除{}条日志 - 普通日志: {}条, 错误日志: {}条",
                    totalDeleted, result.get("normal"), result.get("error"));

        } catch (Exception e) {
            log.error("日志清理任务执行失败", e);
        }
    }

    /**
     * 每小时统计日志情况
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void logStatistics() {
        try {
            long totalCount = logService.getLogStatistics().getTotalCount();
            long errorCount = logService.getLogStatistics().getErrorCount();
            long warnCount = logService.getLogStatistics().getWarnCount();

            log.info("系统日志统计 - 总数: {}, 错误: {}, 警告: {}", totalCount, errorCount, warnCount);

            // 如果错误日志过多，记录警告
            if (errorCount > 1000) {
                log.warn("系统错误日志数量过多: {} 条，建议检查系统状态", errorCount);
            }

        } catch (Exception e) {
            log.error("获取日志统计信息失败", e);
        }
    }
}