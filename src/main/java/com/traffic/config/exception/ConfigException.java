package com.traffic.config.exception;

import lombok.Getter;

@Getter
public class ConfigException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误消息
     */
    private final String errorMessage;

    /**
     * 详细信息
     */
    private final Object details;

    /**
     * 构造函数 - 只包含消息
     */
    public ConfigException(String message) {
        super(message);
        this.errorCode = "CONFIG_ERROR";
        this.errorMessage = message;
        this.details = null;
    }

    /**
     * 构造函数 - 包含消息和原因
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CONFIG_ERROR";
        this.errorMessage = message;
        this.details = null;
    }

    /**
     * 构造函数 - 包含错误码和消息
     */
    public ConfigException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.details = null;
    }

    /**
     * 构造函数 - 包含错误码、消息和原因
     */
    public ConfigException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.details = null;
    }

    /**
     * 构造函数 - 完整参数
     */
    public ConfigException(String errorCode, String message, Object details) {
        super(message);
        self.errorCode = errorCode;
        this.errorMessage = message;
        this.details = details;
    }

    /**
     * 构造函数 - 完整参数包含原因
     */
    public ConfigException(String errorCode, String message, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.details = details;
    }

    // 预定义的异常类型

    /**
     * 配置文件未找到异常
     */
    public static ConfigException fileNotFound(String filePath) {
        return new ConfigException("CONFIG_FILE_NOT_FOUND",
                "配置文件未找到: " + filePath, filePath);
    }

    /**
     * 配置文件解析异常
     */
    public static ConfigException parseError(String filePath, Throwable cause) {
        return new ConfigException("CONFIG_PARSE_ERROR",
                "配置文件解析失败: " + filePath, filePath, cause);
    }

    /**
     * 配置文件保存异常
     */
    public static ConfigException saveError(String filePath, Throwable cause) {
        return new ConfigException("CONFIG_SAVE_ERROR",
                "配置文件保存失败: " + filePath, filePath, cause);
    }

    /**
     * 配置验证异常
     */
    public static ConfigException validationError(String field, Object value) {
        return new ConfigException("CONFIG_VALIDATION_ERROR",
                "配置验证失败: " + field + " = " + value,
                java.util.Map.of("field", field, "value", value));
    }

    /**
     * 路段不存在异常
     */
    public static ConfigException segmentNotFound(String sigid) {
        return new ConfigException("SEGMENT_NOT_FOUND",
                "路段不存在: " + sigid, sigid);
    }

    /**
     * 路段已存在异常
     */
    public static ConfigException segmentAlreadyExists(String sigid) {
        return new ConfigException("SEGMENT_ALREADY_EXISTS",
                "路段已存在: " + sigid, sigid);
    }

    /**
     * 配置锁定异常
     */
    public static ConfigException configLocked() {
        return new ConfigException("CONFIG_LOCKED", "配置文件被其他进程锁定");
    }

    /**
     * 配置版本冲突异常
     */
    public static ConfigException versionConflict(long expectedVersion, long actualVersion) {
        return new ConfigException("CONFIG_VERSION_CONFLICT",
                "配置版本冲突",
                java.util.Map.of("expected", expectedVersion, "actual", actualVersion));
    }
}