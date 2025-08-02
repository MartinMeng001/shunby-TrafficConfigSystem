package com.traffic.config.common.enums;

/**
 * 事件类型枚举
 */
public enum EventType {
    ENTER("ENTER", "进入"),
    EXIT("EXIT", "离开"),
    PASS("PASS", "通过"),
    STOP("STOP", "停止"),
    RESUME("RESUME", "恢复");

    private final String code;
    private final String description;

    EventType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}

