package com.traffic.config.statemachinev2.enums;

/**
 * 系统事件枚举 V2
 * 对应数学模型中的输入事件集合 Σ_sys
 *
 * @author System
 * @version 2.0.0
 */
public enum SystemEventV2 {

    // ==================== 时间事件 (Timer Events) ====================

    /**
     * 定时器滴答事件
     * - 周期性触发的时间事件
     * - 用于检查时间相关的守护条件
     */
    TIMER_TICK("定时器滴答", "TIMER_TICK", EventCategory.TIMER, EventPriority.LOW,
            "周期性时间事件，用于状态检查和超时检测"),

    /**
     * 过渡超时事件
     * - 全红过渡状态超时
     * - 触发异常处理流程
     */
    TRANSITION_TIMEOUT("过渡超时", "TRANSITION_TIMEOUT", EventCategory.TIMER, EventPriority.HIGH,
            "全红过渡状态超时，需要进入降级模式"),

    // ==================== 完成事件 (Completion Events) ====================

    /**
     * 系统初始化完成事件
     * - 系统自检和配置加载完成
     * - 准备进入正常运行状态
     */
    SYSTEM_INIT_COMPLETE("初始化完成", "SYSTEM_INIT_COMPLETE", EventCategory.COMPLETION, EventPriority.NORMAL,
            "系统初始化完成，可以进入运行状态"),

    /**
     * 全红过渡完成事件
     * - 过渡时间达到且路段已清空
     * - 可以进入目标运行状态
     */
    TRANSITION_COMPLETE("过渡完成", "TRANSITION_COMPLETE", EventCategory.COMPLETION, EventPriority.NORMAL,
            "全红过渡完成，路段已清空"),

    // ==================== 故障和恢复事件 (Fault and Recovery Events) ====================

    /**
     * 检测到故障事件
     * - 一般性故障检测
     * - 触发降级处理
     */
    FAULT_DETECTED("故障检测", "FAULT_DETECTED", EventCategory.FAULT, EventPriority.HIGH,
            "检测到系统故障，需要进行故障处理"),

    /**
     * 检测到严重故障事件
     * - 严重故障或多重故障
     * - 触发紧急模式
     */
    CRITICAL_FAULT("严重故障", "CRITICAL_FAULT", EventCategory.FAULT, EventPriority.CRITICAL,
            "检测到严重故障，需要立即进入紧急模式"),

    /**
     * 运行条件恢复事件
     * - 故障条件消失
     * - 可以尝试恢复正常运行
     */
    CONDITIONS_RESTORED("条件恢复", "CONDITIONS_RESTORED", EventCategory.RECOVERY, EventPriority.NORMAL,
            "运行条件恢复，可以尝试恢复正常运行"),

    /**
     * 恢复条件验证通过事件
     * - 恢复条件验证完成
     * - 确认可以安全恢复
     */
    RECOVERY_VERIFIED("恢复验证", "RECOVERY_VERIFIED", EventCategory.RECOVERY, EventPriority.NORMAL,
            "恢复条件验证通过，可以安全恢复运行"),

    // ==================== 控制事件 (Control Events) ====================

    /**
     * 手动降级事件
     * - 操作员手动触发降级
     * - 主动进入降级模式
     */
    MANUAL_DEGRADE("手动降级", "MANUAL_DEGRADE", EventCategory.CONTROL, EventPriority.HIGH,
            "操作员手动触发系统降级"),

    /**
     * 手动紧急模式事件
     * - 操作员手动触发紧急模式
     * - 立即进入紧急状态
     */
    MANUAL_EMERGENCY("手动紧急", "MANUAL_EMERGENCY", EventCategory.CONTROL, EventPriority.CRITICAL,
            "操作员手动触发紧急模式"),

    /**
     * 维护请求事件
     * - 请求进入维护模式
     * - 准备进行系统维护
     */
    MAINTENANCE_REQUEST("维护请求", "MAINTENANCE_REQUEST", EventCategory.CONTROL, EventPriority.NORMAL,
            "请求进入维护模式进行系统维护"),

    /**
     * 系统重置事件
     * - 重置系统到初始状态
     * - 清除所有状态和错误
     */
    SYSTEM_RESET("系统重置", "SYSTEM_RESET", EventCategory.CONTROL, EventPriority.HIGH,
            "重置系统到初始状态"),

    // ==================== 外部事件 (External Events) ====================

    /**
     * 电源故障事件
     * - 电源系统出现问题
     * - 可能影响系统稳定性
     */
    POWER_FAILURE("电源故障", "POWER_FAILURE", EventCategory.EXTERNAL, EventPriority.CRITICAL,
            "电源系统故障，影响系统运行"),

    /**
     * 通信中断事件
     * - 网络通信中断
     * - 影响系统协调
     */
    COMMUNICATION_LOSS("通信中断", "COMMUNICATION_LOSS", EventCategory.EXTERNAL, EventPriority.HIGH,
            "网络通信中断，影响系统协调"),

    /**
     * 外部强制控制事件
     * - 外部系统强制接管控制
     * - 进入被动模式
     */
    EXTERNAL_OVERRIDE("外部接管", "EXTERNAL_OVERRIDE", EventCategory.EXTERNAL, EventPriority.CRITICAL,
            "外部系统强制接管控制权"),

    // ==================== 路段相关事件 (Segment Related Events) ====================

    /**
     * 路段清空事件
     * - 单个路段清空完成
     * - 更新清空状态
     */
    SEGMENT_CLEARED("路段清空", "SEGMENT_CLEARED", EventCategory.SEGMENT, EventPriority.LOW,
            "单个路段清空完成"),

    /**
     * 所有路段清空事件
     * - 所有路段清空完成
     * - 可以进行状态转换
     */
    ALL_SEGMENTS_CLEARED("全部清空", "ALL_SEGMENTS_CLEARED", EventCategory.SEGMENT, EventPriority.NORMAL,
            "所有路段清空完成"),

    /**
     * 清空超时事件
     * - 路段清空超时
     * - 需要特殊处理
     */
    CLEAR_TIMEOUT("清空超时", "CLEAR_TIMEOUT", EventCategory.SEGMENT, EventPriority.HIGH,
            "路段清空超时，需要特殊处理"),

    /**
     * 清空状态更新事件
     * - 路段清空状态更新
     * - 用于状态同步
     */
    CLEAR_STATUS_UPDATE("状态更新", "CLEAR_STATUS_UPDATE", EventCategory.SEGMENT, EventPriority.LOW,
            "路段清空状态更新");

    // ==================== 事件分类枚举 ====================

    /**
     * 事件类别
     */
    public enum EventCategory {
        TIMER("时间事件"),
        COMPLETION("完成事件"),
        FAULT("故障事件"),
        RECOVERY("恢复事件"),
        CONTROL("控制事件"),
        EXTERNAL("外部事件"),
        SEGMENT("路段事件");

        private final String description;

        EventCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 事件优先级
     */
    public enum EventPriority {
        LOW(1, "低优先级"),
        NORMAL(2, "普通优先级"),
        HIGH(3, "高优先级"),
        CRITICAL(4, "关键优先级");

        private final int level;
        private final String description;

        EventPriority(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 枚举属性 ====================

    private final String chineseName;
    private final String code;
    private final EventCategory category;
    private final EventPriority priority;
    private final String description;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param chineseName 中文名称
     * @param code 英文代码
     * @param category 事件类别
     * @param priority 事件优先级
     * @param description 事件描述
     */
    SystemEventV2(String chineseName, String code, EventCategory category,
                  EventPriority priority, String description) {
        this.chineseName = chineseName;
        this.code = code;
        this.category = category;
        this.priority = priority;
        this.description = description;
    }

    // ==================== Getter 方法 ====================

    /**
     * 获取中文名称
     * @return 中文名称
     */
    public String getChineseName() {
        return chineseName;
    }

    /**
     * 获取英文代码
     * @return 英文代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取事件类别
     * @return 事件类别
     */
    public EventCategory getCategory() {
        return category;
    }

    /**
     * 获取事件优先级
     * @return 事件优先级
     */
    public EventPriority getPriority() {
        return priority;
    }

    /**
     * 获取事件描述
     * @return 事件描述
     */
    public String getDescription() {
        return description;
    }

    // ==================== 事件分类方法 ====================

    /**
     * 判断是否为高优先级事件
     * @return 是否为高优先级事件
     */
    public boolean isHighPriority() {
        return priority == EventPriority.HIGH || priority == EventPriority.CRITICAL;
    }

    /**
     * 判断是否为关键事件
     * @return 是否为关键事件
     */
    public boolean isCritical() {
        return priority == EventPriority.CRITICAL;
    }

    /**
     * 判断是否为故障相关事件
     * @return 是否为故障相关事件
     */
    public boolean isFaultRelated() {
        return category == EventCategory.FAULT;
    }

    /**
     * 判断是否为恢复相关事件
     * @return 是否为恢复相关事件
     */
    public boolean isRecoveryRelated() {
        return category == EventCategory.RECOVERY;
    }

    /**
     * 判断是否为外部事件
     * @return 是否为外部事件
     */
    public boolean isExternal() {
        return category == EventCategory.EXTERNAL;
    }

    /**
     * 判断是否为控制事件
     * @return 是否为控制事件
     */
    public boolean isControlEvent() {
        return category == EventCategory.CONTROL;
    }

    /**
     * 判断是否为时间事件
     * @return 是否为时间事件
     */
    public boolean isTimerEvent() {
        return category == EventCategory.TIMER;
    }

    // ==================== 工具方法 ====================

    /**
     * 根据代码获取事件
     * @param code 事件代码
     * @return 对应的事件，如果未找到则返回null
     */
    public static SystemEventV2 fromCode(String code) {
        for (SystemEventV2 event : values()) {
            if (event.getCode().equals(code)) {
                return event;
            }
        }
        return null;
    }

    /**
     * 获取指定类别的所有事件
     * @param category 事件类别
     * @return 该类别的事件数组
     */
    public static SystemEventV2[] getEventsByCategory(EventCategory category) {
        return java.util.Arrays.stream(values())
                .filter(event -> event.getCategory() == category)
                .toArray(SystemEventV2[]::new);
    }

    /**
     * 获取指定优先级的所有事件
     * @param priority 事件优先级
     * @return 该优先级的事件数组
     */
    public static SystemEventV2[] getEventsByPriority(EventPriority priority) {
        return java.util.Arrays.stream(values())
                .filter(event -> event.getPriority() == priority)
                .toArray(SystemEventV2[]::new);
    }

    /**
     * 获取所有高优先级事件（HIGH和CRITICAL）
     * @return 高优先级事件数组
     */
    public static SystemEventV2[] getHighPriorityEvents() {
        return java.util.Arrays.stream(values())
                .filter(SystemEventV2::isHighPriority)
                .toArray(SystemEventV2[]::new);
    }

    /**
     * 获取所有关键事件
     * @return 关键事件数组
     */
    public static SystemEventV2[] getCriticalEvents() {
        return java.util.Arrays.stream(values())
                .filter(SystemEventV2::isCritical)
                .toArray(SystemEventV2[]::new);
    }

    /**
     * 比较事件优先级
     * @param other 另一个事件
     * @return 优先级比较结果，正数表示当前事件优先级更高
     */
    public int comparePriority(SystemEventV2 other) {
        return this.priority.getLevel() - other.priority.getLevel();
    }

    @Override
    public String toString() {
        return String.format("%s(%s) [%s-%s]: %s",
                chineseName, code, category.getDescription(),
                priority.getDescription(), description);
    }
}
