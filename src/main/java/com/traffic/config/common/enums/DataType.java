package com.traffic.config.common.enums;

public enum DataType {
    HEARTBEAT(0, "心跳"),
    NORMAL_DATA(1, "正常数据");

    private final int code;
    private final String description;

    DataType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static DataType fromCode(int code) {
        for (DataType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown data type code: " + code);
    }
}

