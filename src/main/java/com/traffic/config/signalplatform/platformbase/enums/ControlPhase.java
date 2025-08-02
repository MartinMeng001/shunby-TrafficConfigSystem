package com.traffic.config.signalplatform.platformbase.enums;

public enum ControlPhase {
    EAST_FULL_GREEN(7, "东全放"),
    WEST_FULL_GREEN(8, "西全放"),
    SOUTH_FULL_GREEN(9, "南全放"),
    NORTH_FULL_GREEN(10, "北全放"),
    ALL_RED(52, "全红"),
    YELLOW_FLASH(51, "黄闪");

    private final int value;
    private final String label;

    ControlPhase(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static ControlPhase fromValue(int value) {
        for (ControlPhase phase : values()) {
            if (phase.getValue() == value) {
                return phase;
            }
        }
        throw new IllegalArgumentException("No enum constant found with value: " + value);
    }
}
