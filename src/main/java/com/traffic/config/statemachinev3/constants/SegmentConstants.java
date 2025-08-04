package com.traffic.config.statemachinev3.constants;

/**
 * 路段状态机常量定义
 * 对应数学模型中路段级的常量集合
 *
 * @author System
 * @version 3.0.0
 */
public final class SegmentConstants {

    // ==================== 时间常量 (Time Constants) ====================

    /**
     * 最小绿灯时间（秒）
     */
    public static final int MIN_GREEN_TIME = 30;

    /**
     * 最大绿灯时间（秒）
     */
    public static final int MAX_GREEN_TIME = 120;

    /**
     * 最大全红时间（秒）
     */
    public static final int MAX_RED_TIME = 60;

    /**
     * 保守清空时长（秒）
     */
    public static final int CONSERVATIVE_CLEAR_TIME = 30;

    /**
     * 通行请求生成阈值时间（秒）
     */
    public static final int REQUEST_TIME_THRESHOLD = 60;

    /**
     * 紧急覆盖阈值时间（秒）
     */
    public static final int EMERGENCY_OVERRIDE_THRESHOLD = 300;

    /**
     * 传感器超时时间（秒）
     */
    public static final int SENSOR_TIMEOUT = 5;

    /**
     * 请求清除宽限期（秒）
     */
    public static final int REQUEST_CLEAR_GRACE_PERIOD = 10;

    // ==================== 容量常量 (Capacity Constants) ====================

    /**
     * 默认上行会车区容量
     */
    public static final int DEFAULT_UPSTREAM_CAPACITY = 5;

    /**
     * 默认下行会车区容量
     */
    public static final int DEFAULT_DOWNSTREAM_CAPACITY = 5;

    /**
     * 最小路段容量
     */
    public static final int MIN_SEGMENT_CAPACITY = 1;

    /**
     * 最大路段容量
     */
    public static final int MAX_SEGMENT_CAPACITY = 20;

    // ==================== 请求触发常量 (Request Trigger Constants) ====================

    /**
     * 请求触发阈值系数（容量的百分比）
     */
    public static final double REQUEST_TRIGGER_RATIO = 0.3;

    /**
     * 紧急加权因子
     */
    public static final double EMERGENCY_BOOST_FACTOR = 2.0;

    /**
     * 最大合理等待时间（秒）
     */
    public static final int MAX_REASONABLE_WAIT_TIME = 180;

    // ==================== 优先级权重常量 (Priority Weight Constants) ====================

    /**
     * 默认时间优先权重
     */
    public static final double DEFAULT_TIME_PRIORITY_WEIGHT = 0.4;

    /**
     * 默认负载均衡权重
     */
    public static final double DEFAULT_LOAD_BALANCE_WEIGHT = 0.3;

    /**
     * 默认交替服务权重
     */
    public static final double DEFAULT_ALTERNATION_WEIGHT = 0.3;

    /**
     * 优先级得分相近阈值
     */
    public static final double PRIORITY_SCORE_THRESHOLD = 0.1;

    // ==================== 错误检测常量 (Error Detection Constants) ====================

    /**
     * 最大计数器不匹配错误次数
     */
    public static final int MAX_COUNTER_MISMATCH_ERRORS = 3;

    /**
     * 最大ID逻辑错误次数
     */
    public static final int MAX_ID_LOGIC_ERRORS = 5;

    /**
     * 最大连续错误次数
     */
    public static final int MAX_CONSECUTIVE_ERRORS = 5;

    /**
     * 错误历史记录最大数量
     */
    public static final int MAX_ERROR_HISTORY = 1000;

    // ==================== 传感器配置常量 (Sensor Configuration Constants) ====================

    /**
     * 最小检测置信度阈值
     */
    public static final double MIN_DETECTION_CONFIDENCE = 0.7;

    /**
     * 传感器错误率阈值
     */
    public static final double SENSOR_ERROR_RATE_THRESHOLD = 0.1;

    /**
     * 初始传感器置信度
     */
    public static final double INITIAL_SENSOR_CONFIDENCE = 0.9;

    /**
     * 置信度衰减步长
     */
    public static final double CONFIDENCE_DECAY_STEP = 0.1;

    /**
     * 置信度恢复步长
     */
    public static final double CONFIDENCE_RECOVERY_STEP = 0.05;

    /**
     * 最大相同读数阈值
     */
    public static final int MAX_IDENTICAL_READINGS_THRESHOLD = 10;

    // ==================== 性能监控常量 (Performance Monitoring Constants) ====================

    /**
     * 性能统计时间窗口（秒）
     */
    public static final int PERFORMANCE_WINDOW = 300;

    /**
     * 拥堵程度高阈值
     */
    public static final double HIGH_CONGESTION_THRESHOLD = 0.8;

    /**
     * 通行效率计算窗口（秒）
     */
    public static final int THROUGHPUT_CALCULATION_WINDOW = 60;

    /*
     * 错误统计窗口
     */
    public static final int ERROR_WINDOW = 10;
    /**
     * 平均等待时间移动平均系数
     */
    public static final double WAITING_TIME_SMOOTH_FACTOR = 0.8;
    /*
     * 正常健康阈值
     */
    public static final int NORMAL_HEALTH_THRESHOLD = 70;
    /*
     * 安全健康阈值
     */
    public static final int RECOVERY_HEALTH_THRESHOLD = 60;
    /*
     * 健康度过低阈值
     */
    public static final int CRITICAL_HEALTH_THRESHOLD = 20;


    // ==================== 私有构造函数 ====================

    /**
     * 私有构造函数，防止实例化
     */
    private SegmentConstants() {
        throw new UnsupportedOperationException("Constants class should not be instantiated");
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算请求触发阈值
     * @param capacity 路段容量
     * @return 请求触发阈值
     */
    public static int calculateRequestTriggerThreshold(int capacity) {
        return Math.max(1, (int) (capacity * REQUEST_TRIGGER_RATIO));
    }

    /**
     * 验证容量是否有效
     * @param capacity 容量值
     * @return 是否有效
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity >= MIN_SEGMENT_CAPACITY && capacity <= MAX_SEGMENT_CAPACITY;
    }

    /**
     * 限制容量在有效范围内
     * @param capacity 原始容量
     * @return 限制后的容量
     */
    public static int clampCapacity(int capacity) {
        return Math.max(MIN_SEGMENT_CAPACITY, Math.min(MAX_SEGMENT_CAPACITY, capacity));
    }

    /**
     * 验证优先级权重是否有效
     * @param timeWeight 时间权重
     * @param loadWeight 负载权重
     * @param alternationWeight 交替权重
     * @return 是否有效
     */
    public static boolean areValidPriorityWeights(double timeWeight, double loadWeight, double alternationWeight) {
        double sum = timeWeight + loadWeight + alternationWeight;
        return Math.abs(sum - 1.0) < 1e-6 &&
                timeWeight >= 0 && loadWeight >= 0 && alternationWeight >= 0;
    }

    /**
     * 验证时间是否有效
     * @param time 时间值（秒）
     * @return 是否有效
     */
    public static boolean isValidTime(int time) {
        return time > 0;
    }

    /**
     * 验证绿灯时间是否有效
     * @param greenTime 绿灯时间
     * @return 是否有效
     */
    public static boolean isValidGreenTime(int greenTime) {
        return greenTime >= MIN_GREEN_TIME && greenTime <= MAX_GREEN_TIME;
    }

    /**
     * 检查是否为紧急等待时间
     * @param waitingTime 等待时间
     * @return 是否为紧急等待
     */
    public static boolean isEmergencyWaitingTime(double waitingTime) {
        return waitingTime > EMERGENCY_OVERRIDE_THRESHOLD;
    }

    /**
     * 检查传感器置信度是否足够
     * @param confidence 置信度
     * @return 是否足够
     */
    public static boolean isSufficientConfidence(double confidence) {
        return confidence >= MIN_DETECTION_CONFIDENCE;
    }
}
