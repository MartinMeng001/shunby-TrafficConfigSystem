package com.traffic.config.statemachinev3.constants;

/**
 * 顶层系统状态机常量定义
 * 对应数学模型中的 CONSTANTS 集合
 *
 * @author System
 * @version 3.0.0
 */
public final class SystemConstants {

    // ==================== 时间常量 (Time Constants) ====================
    /**
     * 最大全红时间（秒）
     */
    public static final int MAX_RED_TIME = 60;
    /**
     * 系统全红过渡时间（秒）
     */
    public static final int TRANSITION_TIME = 5;

    /**
     * 错误统计时间窗口（秒） - 60分钟
     */
    public static final int ERROR_WINDOW = 3600;

    /**
     * 系统初始化延迟（秒）
     */
    public static final int SYSTEM_INIT_DELAY = 2;

    /**
     * 恢复条件检查间隔（秒）
     */
    public static final int RECOVERY_CHECK_INTERVAL = 30;

    /**
     * 稳定运行时间要求（秒） - 5分钟
     */
    public static final int STABLE_OPERATION_TIME = 300;

    /**
     * 最大响应时间（秒）
     */
    public static final int MAX_RESPONSE_TIME = 10;

    /**
     * 过渡超时倍数
     */
    public static final int TRANSITION_TIMEOUT_MULTIPLIER = 2;

    // ==================== 错误阈值常量 (Error Threshold Constants) ====================

    /**
     * 60分钟内最大计数器错误次数
     */
    public static final int MAX_MISMATCH_ERRORS = 5;

    /**
     * 60分钟内最大ID错误次数
     */
    public static final int MAX_ID_ERRORS = 10;

    /**
     * 连续超时限制次数
     */
    public static final int CONSECUTIVE_TIMEOUT_LIMIT = 3;

    /**
     * 系统降级阈值（0.0-1.0）
     */
    public static final double SYSTEM_DEGRADATION_THRESHOLD = 0.8;

    /**
     * 错误重置阈值（0.0-1.0）
     */
    public static final double ERROR_RESET_THRESHOLD = 0.1;

    // ==================== 系统配置常量 (System Configuration Constants) ====================

    /**
     * 路段总数
     */
    public static final int TOTAL_SEGMENT_COUNT = 4;

    /**
     * 最大恢复尝试次数
     */
    public static final int MAX_RECOVERY_ATTEMPTS = 3;

    /**
     * 最大清空超时次数
     */
    public static final int MAX_CLEAR_TIMEOUT_COUNT = 3;

    // ==================== 系统健康度常量 (System Health Constants) ====================

    /**
     * 系统健康度最小值
     */
    public static final int MIN_HEALTH_SCORE = 0;

    /**
     * 系统健康度最大值
     */
    public static final int MAX_HEALTH_SCORE = 100;

    /**
     * 初始健康度评分
     */
    public static final int INITIAL_HEALTH_SCORE = 50;

    /**
     * 严重故障健康度阈值
     */
    public static final int CRITICAL_HEALTH_THRESHOLD = 20;

    /**
     * 正常运行健康度阈值
     */
    public static final int NORMAL_HEALTH_THRESHOLD = 70;

    /**
     * 恢复健康度阈值
     */
    public static final int RECOVERY_HEALTH_THRESHOLD = 70;

    /**
     * 恢复健康度奖励
     */
    public static final int RECOVERY_HEALTH_BONUS = 30;

    // ==================== 私有构造函数 ====================

    /**
     * 私有构造函数，防止实例化
     */
    private SystemConstants() {
        throw new UnsupportedOperationException("Constants class should not be instantiated");
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取过渡超时时间
     * @return 过渡超时时间（秒）
     */
    public static int getTransitionTimeoutSeconds() {
        return TRANSITION_TIME * TRANSITION_TIMEOUT_MULTIPLIER;
    }

    /**
     * 获取错误窗口时间（分钟）
     * @return 错误窗口时间（分钟）
     */
    public static int getErrorWindowMinutes() {
        return ERROR_WINDOW / 60;
    }

    /**
     * 获取稳定运行时间（分钟）
     * @return 稳定运行时间（分钟）
     */
    public static int getStableOperationTimeMinutes() {
        return STABLE_OPERATION_TIME / 60;
    }

    /**
     * 验证健康度评分是否有效
     * @param score 健康度评分
     * @return 是否有效
     */
    public static boolean isValidHealthScore(int score) {
        return score >= MIN_HEALTH_SCORE && score <= MAX_HEALTH_SCORE;
    }

    /**
     * 限制健康度评分在有效范围内
     * @param score 原始评分
     * @return 限制后的评分
     */
    public static int clampHealthScore(int score) {
        return Math.max(MIN_HEALTH_SCORE, Math.min(MAX_HEALTH_SCORE, score));
    }

    /**
     * 检查是否为严重故障健康度
     * @param score 健康度评分
     * @return 是否为严重故障
     */
    public static boolean isCriticalHealth(int score) {
        return score < CRITICAL_HEALTH_THRESHOLD;
    }

    /**
     * 检查是否为正常健康度
     * @param score 健康度评分
     * @return 是否为正常健康度
     */
    public static boolean isNormalHealth(int score) {
        return score >= NORMAL_HEALTH_THRESHOLD;
    }
}
