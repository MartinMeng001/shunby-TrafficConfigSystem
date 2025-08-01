package com.traffic.config.statemachine.enums;

public enum IdErrorType {
    UNREGISTERED_SOURCE,    // 无源注册错误
    REVERSE_DIRECTION,      // 逆行错误
    INVALID_TRANSITION,     // 无效状态转移
    DUPLICATE_EXISTENCE,    // 重复存在错误
    ZOMBIE_VEHICLE          // 僵尸车辆错误
}
