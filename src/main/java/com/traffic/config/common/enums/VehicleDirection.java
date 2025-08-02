package com.traffic.config.common.enums;

public enum VehicleDirection {
    OUT(0, "车出"),
    IN(1, "车入");

    private final int code;
    private final String description;

    VehicleDirection(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static VehicleDirection fromCode(int code) {
        for (VehicleDirection direction : values()) {
            if (direction.code == code) return direction;
        }
        throw new IllegalArgumentException("Unknown direction code: " + code);
    }
}

