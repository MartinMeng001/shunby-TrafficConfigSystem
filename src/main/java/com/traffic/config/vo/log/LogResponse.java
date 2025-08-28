package com.traffic.config.vo.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日志响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 日志类型
     */
    private String logType;

    /**
     * 日志类型描述
     */
    private String logTypeDesc;

    /**
     * 日志级别
     */
    private String logLevel;

    /**
     * 日志级别描述
     */
    private String logLevelDesc;

    /**
     * 日志内容
     */
    private String logContent;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTime;

    /**
     * 相关模块
     */
    private String module;

    /**
     * 相关实体ID
     */
    private String entityId;

    /**
     * 操作用户
     */
    private String operator;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 相对时间（如：2分钟前）
     */
    private String relativeTime;
}
