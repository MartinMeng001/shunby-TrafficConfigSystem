package com.traffic.config.vo.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.traffic.config.entity.log.SystemLogEntity.LogType;
import com.traffic.config.entity.log.SystemLogEntity.LogLevel;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 日志查询条件VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogQueryCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志类型
     */
    private LogType logType;

    /**
     * 日志级别
     */
    private LogLevel logLevel;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 实体ID
     */
    private String entityId;

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private int page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private int size = 20;

    // ==================== 便捷构造方法 ====================

    /**
     * 创建时间范围查询条件
     */
    public static LogQueryCondition timeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return LogQueryCondition.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    /**
     * 创建类型查询条件
     */
    public static LogQueryCondition type(LogType logType) {
        return LogQueryCondition.builder()
                .logType(logType)
                .build();
    }

    /**
     * 创建级别查询条件
     */
    public static LogQueryCondition level(LogLevel logLevel) {
        return LogQueryCondition.builder()
                .logLevel(logLevel)
                .build();
    }

    /**
     * 创建实体查询条件
     */
    public static LogQueryCondition entity(String entityId) {
        return LogQueryCondition.builder()
                .entityId(entityId)
                .build();
    }
}