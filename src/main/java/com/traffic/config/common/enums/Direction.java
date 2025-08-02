package com.traffic.config.common.enums;

/**
 * 方向枚举
 */
public enum Direction {
    UPSTREAM("UPSTREAM", "上行", "↑"),
    DOWNSTREAM("DOWNSTREAM", "下行", "↓"),
    BIDIRECTIONAL("BIDIRECTIONAL", "双向", "↕");

    private final String code;
    private final String description;
    private final String symbol;

    Direction(String code, String description, String symbol) {
        this.code = code;
        this.description = description;
        this.symbol = symbol;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getSymbol() { return symbol; }

    public Direction opposite() {
        switch (this) {
            case UPSTREAM: return DOWNSTREAM;
            case DOWNSTREAM: return UPSTREAM;
            case BIDIRECTIONAL: return BIDIRECTIONAL;
            default: throw new IllegalStateException("Unknown direction: " + this);
        }
    }
}
