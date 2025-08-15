package com.traffic.config.statemachinev3.enums.segment;

/**
 * 路段事件枚举
 * 对应数学模型中的事件集合 Σ
 *
 * @author System
 * @version 3.0.0
 */
public enum SegmentEvent {

    // ==================== 时间驱动事件 (Timer Events) ====================

    /**
     * 定时器滴答事件
     * - 周期性触发的时间事件
     * - 用于检查时间相关的守护条件
     */
    TIMER_TICK("定时器滴答", "TIMER_TICK", EventCategory.TIMER, EventPriority.LOW,
            "周期性时间事件，用于状态检查和超时检测"),

    /**
     * 绿灯超时事件
     * - 绿灯时间超过最大限制
     * - 强制进入清空状态
     */
    GREEN_TIMEOUT("绿灯超时", "GREEN_TIMEOUT", EventCategory.TIMER, EventPriority.HIGH,
            "绿灯时间超时，强制切换"),

    /**
     * 清空超时事件
     * - 清空时间超过最大限制
     * - 可能触发保守清空
     */
    CLEAR_TIMEOUT("清空超时", "CLEAR_TIMEOUT", EventCategory.TIMER, EventPriority.HIGH,
            "清空超时，需要特殊处理"),

    // ==================== 车辆运动事件 (Vehicle Movement Events) ====================

    /**
     * 上行车辆进入事件
     * - 上行方向有车辆进入路段
     * - 更新车辆计数和ID集合
     */
    VEHICLE_ENTER_UPSTREAM("上行车辆进入", "VEHICLE_ENTER_UPSTREAM", EventCategory.VEHICLE, EventPriority.NORMAL, "上行车辆进入路段"),

    /**
     * 上行车辆离开事件
     * - 上行方向有车辆离开路段
     * - 更新车辆计数和ID集合
     */
    VEHICLE_EXIT_UPSTREAM("上行车辆离开", "VEHICLE_EXIT_UPSTREAM", EventCategory.VEHICLE, EventPriority.NORMAL, "上行车辆离开路段"),

    /**
     * 下行车辆进入事件
     * - 下行方向有车辆进入路段
     * - 更新车辆计数和ID集合
     */
    VEHICLE_ENTER_DOWNSTREAM("下行车辆进入", "VEHICLE_ENTER_DOWNSTREAM", EventCategory.VEHICLE, EventPriority.NORMAL, "下行车辆进入路段"),

    /**
     * 下行车辆离开事件
     * - 下行方向有车辆离开路段
     * - 更新车辆计数和ID集合
     */
    VEHICLE_EXIT_DOWNSTREAM("下行车辆离开", "VEHICLE_EXIT_DOWNSTREAM", EventCategory.VEHICLE, EventPriority.NORMAL, "下行车辆离开路段"),

    // ==================== 清空决策事件 (Clearance Decision Events) ====================

    /**
     * 清空状态更新事件
     * - 路段清空状态发生变化
     * - 重新计算清空决策
     */
    CLEARANCE_STATUS_UPDATE("清空状态更新", "CLEARANCE_STATUS_UPDATE", EventCategory.CLEARANCE, EventPriority.LOW,
            "路段清空状态更新"),

    /**
     * 保守清空触发事件
     * - 保守清空计时器到期
     * - 强制清空路段
     */
    CONSERVATIVE_CLEAR_TRIGGERED("保守清空触发", "CONSERVATIVE_CLEAR_TRIGGERED", EventCategory.CLEARANCE, EventPriority.HIGH,
            "保守清空计时器到期"),

    /**
     * 清空完成事件
     * - 路段清空完成
     * - 可以进行状态转换
     */
    CLEARANCE_COMPLETE("清空完成", "CLEARANCE_COMPLETE", EventCategory.CLEARANCE, EventPriority.NORMAL,
            "路段清空完成"),

    // ==================== 故障和异常事件 (Fault and Exception Events) ====================

    /**
     * 传感器故障事件
     * - 传感器检测到故障
     * - 可能影响车辆检测
     */
    SENSOR_FAULT("传感器故障", "SENSOR_FAULT", EventCategory.FAULT, EventPriority.HIGH,
            "传感器故障，影响车辆检测"),

    /**
     * 通信故障事件
     * - 通信链路出现问题
     * - 影响数据传输
     */
    COMMUNICATION_FAULT("通信故障", "COMMUNICATION_FAULT", EventCategory.FAULT, EventPriority.HIGH,
            "通信故障，影响数据传输"),

    /**
     * 计数器不匹配检测事件
     * - 检测到计数器不匹配错误
     * - 数据完整性问题
     */
    COUNTER_MISMATCH_DETECTED("计数器不匹配", "COUNTER_MISMATCH_DETECTED", EventCategory.FAULT, EventPriority.MEDIUM,
            "检测到计数器不匹配错误"),

    /**
     * ID逻辑错误检测事件
     * - 检测到车辆ID逻辑错误
     * - 车辆跟踪异常
     */
    ID_LOGIC_ERROR_DETECTED("ID逻辑错误", "ID_LOGIC_ERROR_DETECTED", EventCategory.FAULT, EventPriority.MEDIUM,
            "检测到车辆ID逻辑错误"),

    /**
     * 数据不一致发现事件
     * - 发现数据不一致问题
     * - 需要进行数据校正
     */
    DATA_INCONSISTENCY_FOUND("数据不一致", "DATA_INCONSISTENCY_FOUND", EventCategory.FAULT, EventPriority.MEDIUM,
            "发现数据不一致问题"),

    // ==================== 控制事件 (Control Events) ====================

    /**
     * 强制切换事件
     * - 强制进行状态切换
     * - 优先级最高
     */
    FORCE_SWITCH("强制切换", "FORCE_SWITCH", EventCategory.CONTROL, EventPriority.CRITICAL,
            "强制进行状态切换"),

    /**
     * 紧急覆盖事件
     * - 紧急情况下的强制控制
     * - 忽略常规守护条件
     */
    EMERGENCY_OVERRIDE("紧急覆盖", "EMERGENCY_OVERRIDE", EventCategory.CONTROL, EventPriority.CRITICAL,
            "紧急情况下的强制控制"),

    /**
     * 维护模式事件
     * - 进入维护模式
     * - 暂停自动控制
     */
    MAINTENANCE_MODE("维护模式", "MAINTENANCE_MODE", EventCategory.CONTROL, EventPriority.HIGH,
            "进入维护模式"),

    /**
     * 系统重置事件
     * - 重置路段状态机
     * - 清除所有状态和错误
     */
    SYSTEM_RESET("系统重置", "SYSTEM_RESET", EventCategory.CONTROL, EventPriority.HIGH,
            "重置路段状态机"),

    // ==================== 外部系统事件 (External System Events) ====================

    /**
     * 系统状态机指令事件
     * - 来自顶层系统状态机的指令
     * - 协调整体系统行为
     */
    SYSTEM_STATE_MACHINE_COMMAND("系统指令", "SYSTEM_STATE_MACHINE_COMMAND", EventCategory.EXTERNAL, EventPriority.HIGH,
            "来自顶层系统状态机的指令"),

    /**
     * 配置更新事件
     * - 路段配置参数更新
     * - 重新加载配置
     */
    CONFIG_UPDATE("配置更新", "CONFIG_UPDATE", EventCategory.EXTERNAL, EventPriority.MEDIUM,
            "路段配置参数更新");

    // ==================== 事件分类枚举 ====================

    /**
     * 事件类别
     */
    public enum EventCategory {
        TIMER("时间事件"),
        VEHICLE("车辆事件"),
        REQUEST("请求事件"),
        CLEARANCE("清空事件"),
        FAULT("故障事件"),
        CONTROL("控制事件"),
        EXTERNAL("外部事件");

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
        MEDIUM(2, "中等优先级"),
        NORMAL(3, "普通优先级"),
        HIGH(4, "高优先级"),
        CRITICAL(5, "关键优先级");

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
    SegmentEvent(String chineseName, String code, EventCategory category,
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
     * 判断是否为车辆相关事件
     * @return 是否为车辆相关事件
     */
    public boolean isVehicleEvent() {
        return category == EventCategory.VEHICLE;
    }

    /**
     * 判断是否为上行车辆事件
     * @return 是否为上行车辆事件
     */
    public boolean isUpstreamVehicleEvent() {
        return this == VEHICLE_ENTER_UPSTREAM || this == VEHICLE_EXIT_UPSTREAM;
    }

    /**
     * 判断是否为下行车辆事件
     * @return 是否为下行车辆事件
     */
    public boolean isDownstreamVehicleEvent() {
        return this == VEHICLE_ENTER_DOWNSTREAM || this == VEHICLE_EXIT_DOWNSTREAM;
    }

    /**
     * 判断是否为车辆进入事件
     * @return 是否为车辆进入事件
     */
    public boolean isVehicleEntryEvent() {
        return this == VEHICLE_ENTER_UPSTREAM || this == VEHICLE_ENTER_DOWNSTREAM;
    }

    /**
     * 判断是否为车辆离开事件
     * @return 是否为车辆离开事件
     */
    public boolean isVehicleExitEvent() {
        return this == VEHICLE_EXIT_UPSTREAM || this == VEHICLE_EXIT_DOWNSTREAM;
    }

    /**
     * 判断是否为通行请求相关事件
     * @return 是否为通行请求相关事件
     */
    public boolean isRequestEvent() {
        return category == EventCategory.REQUEST;
    }

    /**
     * 判断是否为清空相关事件
     * @return 是否为清空相关事件
     */
    public boolean isClearanceEvent() {
        return category == EventCategory.CLEARANCE;
    }

    /**
     * 判断是否为故障相关事件
     * @return 是否为故障相关事件
     */
    public boolean isFaultEvent() {
        return category == EventCategory.FAULT;
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

    /**
     * 判断是否为外部事件
     * @return 是否为外部事件
     */
    public boolean isExternalEvent() {
        return category == EventCategory.EXTERNAL;
    }

    /**
     * 判断是否会强制状态转换
     * @return 是否会强制状态转换
     */
    public boolean isForceTransition() {
        return this == FORCE_SWITCH || this == EMERGENCY_OVERRIDE ||
                this == GREEN_TIMEOUT || this == SYSTEM_RESET;
    }

    /**
     * 获取车辆事件的方向
     * @return 车辆事件的方向，如果不是车辆事件则返回null
     */
    public String getVehicleDirection() {
        if (isUpstreamVehicleEvent()) {
            return "UPSTREAM";
        } else if (isDownstreamVehicleEvent()) {
            return "DOWNSTREAM";
        }
        return null;
    }

    /**
     * 获取车辆事件的动作类型
     * @return 车辆事件的动作类型，如果不是车辆事件则返回null
     */
    public String getVehicleAction() {
        if (isVehicleEntryEvent()) {
            return "ENTER";
        } else if (isVehicleExitEvent()) {
            return "EXIT";
        }
        return null;
    }

    // ==================== 工具方法 ====================

    /**
     * 根据代码获取事件
     * @param code 事件代码
     * @return 对应的事件，如果未找到则返回null
     */
    public static SegmentEvent fromCode(String code) {
        for (SegmentEvent event : values()) {
            if (event.getCode().equals(code)) {
                return event;
            }
        }
        return null;
    }

    /**
     * 根据车辆方向和动作创建事件
     * @param direction 方向（UPSTREAM/DOWNSTREAM）
     * @param action 动作（ENTER/EXIT）
     * @return 对应的车辆事件，如果参数无效则返回null
     */
    public static SegmentEvent createVehicleEvent(String direction, String action) {
        if ("UPSTREAM".equals(direction)) {
            return "ENTER".equals(action) ? VEHICLE_ENTER_UPSTREAM :
                    "EXIT".equals(action) ? VEHICLE_EXIT_UPSTREAM : null;
        } else if ("DOWNSTREAM".equals(direction)) {
            return "ENTER".equals(action) ? VEHICLE_ENTER_DOWNSTREAM :
                    "EXIT".equals(action) ? VEHICLE_EXIT_DOWNSTREAM : null;
        }
        return null;
    }

    /**
     * 获取指定类别的所有事件
     * @param category 事件类别
     * @return 该类别的事件数组
     */
    public static SegmentEvent[] getEventsByCategory(EventCategory category) {
        return java.util.Arrays.stream(values())
                .filter(event -> event.getCategory() == category)
                .toArray(SegmentEvent[]::new);
    }

    /**
     * 获取指定优先级的所有事件
     * @param priority 事件优先级
     * @return 该优先级的事件数组
     */
    public static SegmentEvent[] getEventsByPriority(EventPriority priority) {
        return java.util.Arrays.stream(values())
                .filter(event -> event.getPriority() == priority)
                .toArray(SegmentEvent[]::new);
    }

    /**
     * 获取所有车辆事件
     * @return 车辆事件数组
     */
    public static SegmentEvent[] getVehicleEvents() {
        return getEventsByCategory(EventCategory.VEHICLE);
    }

    /**
     * 获取所有通行请求事件
     * @return 通行请求事件数组
     */
    public static SegmentEvent[] getRequestEvents() {
        return getEventsByCategory(EventCategory.REQUEST);
    }

    /**
     * 获取所有故障事件
     * @return 故障事件数组
     */
    public static SegmentEvent[] getFaultEvents() {
        return getEventsByCategory(EventCategory.FAULT);
    }

    /**
     * 获取所有强制转换事件
     * @return 强制转换事件数组
     */
    public static SegmentEvent[] getForceTransitionEvents() {
        return java.util.Arrays.stream(values())
                .filter(SegmentEvent::isForceTransition)
                .toArray(SegmentEvent[]::new);
    }

    /**
     * 比较事件优先级
     * @param other 另一个事件
     * @return 优先级比较结果，正数表示当前事件优先级更高
     */
    public int comparePriority(SegmentEvent other) {
        return this.priority.getLevel() - other.priority.getLevel();
    }

    @Override
    public String toString() {
        return String.format("%s(%s) [%s-%s]: %s",
                chineseName, code, category.getDescription(),
                priority.getDescription(), description);
    }
}
