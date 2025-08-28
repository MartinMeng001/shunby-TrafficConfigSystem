package com.traffic.config.entity.log;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库运行日志实体类
 */
@Entity
@Table(name = "system_log",
        indexes = {
                @Index(name = "idx_log_type", columnList = "log_type"),
                @Index(name = "idx_log_level", columnList = "log_level"),
                @Index(name = "idx_create_time", columnList = "create_time"),
                @Index(name = "idx_type_level", columnList = "log_type,log_level")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false, length = 50)
    private LogType logType;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false, length = 20)
    private LogLevel logLevel;

    @Column(name = "log_content", nullable = false, columnDefinition = "TEXT")
    private String logContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "operator", length = 100)
    private String operator;

    @Column(name = "ext_info", columnDefinition = "TEXT")
    private String extInfo;

    // 枚举定义
    public enum LogType {
        VEHICLE_EVENT("车辆事件"),
        SEGMENT_STATUS("路段状态"),
        CONFLICT_ALERT("冲突告警"),
        DETECTOR_ERROR("检测器错误"),
        SENSOR_STATUS("传感器状态"),
        SYSTEM_EVENT("系统事件");

        private final String description;

        LogType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum LogLevel {
        DEBUG("调试"),
        INFO("信息"),
        WARN("警告"),
        ERROR("错误");

        private final String description;

        LogLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    // 便捷创建方法
    public static SystemLogEntity createVehicleLog(LogLevel level, String content, String entityId) {
        return SystemLogEntity.builder()
                .logType(LogType.VEHICLE_EVENT)
                .logLevel(level)
                .logContent(content)
                .entityId(entityId)
                .createTime(LocalDateTime.now())
                .build();
    }

    public static SystemLogEntity createSegmentLog(LogLevel level, String content, String entityId) {
        return SystemLogEntity.builder()
                .logType(LogType.SEGMENT_STATUS)
                .logLevel(level)
                .logContent(content)
                .entityId(entityId)
                .createTime(LocalDateTime.now())
                .build();
    }

    public static SystemLogEntity createSystemLog(LogLevel level, String content) {
        return SystemLogEntity.builder()
                .logType(LogType.SYSTEM_EVENT)
                .logLevel(level)
                .logContent(content)
                .createTime(LocalDateTime.now())
                .build();
    }
}