package com.traffic.config.vo.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 日志统计信息VO
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总日志数量
     */
    private Long totalCount;

    /**
     * 错误日志数量
     */
    private Long errorCount;

    /**
     * 警告日志数量
     */
    private Long warnCount;

    /**
     * 今日日志数量
     */
    private Long todayCount;

    /**
     * 按类型统计
     */
    private Map<String, Long> typeStatistics;

    /**
     * 按级别统计
     */
    private Map<String, Long> levelStatistics;

    /**
     * 统计生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generateTime;

    /**
     * 设置默认生成时间
     */
    public LogStatistics() {
        this.generateTime = LocalDateTime.now();
    }
}
