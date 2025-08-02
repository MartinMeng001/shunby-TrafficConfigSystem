package com.traffic.config.statemachinev2.enums.ext;

public enum ClearanceDecision {
    CLEARED_SAFE,           // 安全清空
    CLEARED_WITH_WARNING,   // 清空但有警告
    CONSERVATIVE_CLEAR,     // 保守清空
    NOT_CLEARED            // 未清空
}
