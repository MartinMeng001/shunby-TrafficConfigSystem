package com.traffic.config.statemachinev2.enums;

/**
 * 系统状态枚举 V2
 * 对应数学模型中的状态集合 Q_sys
 *
 * Q_sys = {SYSTEM_INIT, ALL_RED_TRANSITION, INDUCTIVE_MODE, DEGRADED_MODE, MAINTENANCE_MODE, EMERGENCY_MODE}
 *
 * @author System
 * @version 2.0.0
 */
public enum SystemStateV2 {

    /**
     * 系统初始化状态
     * - 系统启动时的初始状态
     * - 进行系统自检、配置加载等操作
     * - 验证系统完整性和准备就绪状态
     */
    SYSTEM_INIT("系统初始化", "INIT", "系统正在初始化，进行自检和配置加载"),

    /**
     * 全红过渡状态
     * - 状态切换时的安全过渡状态
     * - 所有信号灯显示红灯，确保交通安全
     * - 等待路段清空和时间延迟
     */
    ALL_RED_TRANSITION("全红过渡", "TRANSITION", "所有方向红灯，等待路段清空"),

    /**
     * 感应控制模式
     * - 正常运行的主要状态
     * - 基于交通流感应进行信号控制
     * - 动态调整信号配时
     */
    INDUCTIVE_MODE("感应控制", "INDUCTIVE", "基于交通流感应的智能信号控制"),

    /**
     * 降级模式
     * - 系统出现故障时的降级运行状态
     * - 采用简化的控制策略
     * - 保证基本的交通通行
     */
    DEGRADED_MODE("降级模式", "DEGRADED", "系统降级运行，采用基础控制策略"),

    /**
     * 维护模式
     * - 系统维护时的特殊状态
     * - 可能需要人工干预
     * - 限制自动控制功能
     */
    MAINTENANCE_MODE("维护模式", "MAINTENANCE", "系统处于维护状态，功能受限"),

    /**
     * 紧急模式
     * - 严重故障或紧急情况下的状态
     * - 优先保证安全
     * - 可能采用全红或黄闪等安全信号
     */
    EMERGENCY_MODE("紧急模式", "EMERGENCY", "系统紧急状态，优先保证安全");

    // ==================== 枚举属性 ====================

    /**
     * 状态中文名称
     */
    private final String chineseName;

    /**
     * 状态英文代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param chineseName 中文名称
     * @param code 英文代码
     * @param description 状态描述
     */
    SystemStateV2(String chineseName, String code, String description) {
        this.chineseName = chineseName;
        this.code = code;
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
     * 获取状态描述
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    // ==================== 状态分类方法 ====================

    /**
     * 判断是否为正常运行状态
     * @return 是否为正常运行状态
     */
    public boolean isNormalOperation() {
        return this == INDUCTIVE_MODE;
    }

    /**
     * 判断是否为过渡状态
     * @return 是否为过渡状态
     */
    public boolean isTransitionState() {
        return this == ALL_RED_TRANSITION;
    }

    /**
     * 判断是否为故障相关状态
     * @return 是否为故障相关状态
     */
    public boolean isFaultRelated() {
        return this == DEGRADED_MODE || this == EMERGENCY_MODE;
    }

    /**
     * 判断是否为维护相关状态
     * @return 是否为维护相关状态
     */
    public boolean isMaintenanceRelated() {
        return this == MAINTENANCE_MODE;
    }

    /**
     * 判断是否为初始化状态
     * @return 是否为初始化状态
     */
    public boolean isInitialization() {
        return this == SYSTEM_INIT;
    }

    /**
     * 判断是否为安全状态（所有车辆必须停止的状态）
     * @return 是否为安全状态
     */
    public boolean isSafetyState() {
        return this == ALL_RED_TRANSITION || this == EMERGENCY_MODE;
    }

    /**
     * 判断是否允许自动恢复
     * @return 是否允许自动恢复
     */
    public boolean allowsAutoRecovery() {
        return this == DEGRADED_MODE;
    }

    /**
     * 判断是否需要人工干预
     * @return 是否需要人工干预
     */
    public boolean requiresManualIntervention() {
        return this == MAINTENANCE_MODE || this == EMERGENCY_MODE;
    }

    // ==================== 状态转换相关方法 ====================

    /**
     * 获取可以直接转换到的状态集合
     * 注意：这只是基本的转换关系，实际转换还需要满足守护条件
     *
     * @return 可转换状态数组
     */
    public SystemStateV2[] getPossibleTransitions() {
        return switch (this) {
            case SYSTEM_INIT -> new SystemStateV2[]{ALL_RED_TRANSITION, EMERGENCY_MODE};
            case ALL_RED_TRANSITION -> new SystemStateV2[]{INDUCTIVE_MODE, DEGRADED_MODE, EMERGENCY_MODE, SYSTEM_INIT};
            case INDUCTIVE_MODE -> new SystemStateV2[]{ALL_RED_TRANSITION, EMERGENCY_MODE, MAINTENANCE_MODE};
            case DEGRADED_MODE -> new SystemStateV2[]{ALL_RED_TRANSITION, MAINTENANCE_MODE, EMERGENCY_MODE};
            case MAINTENANCE_MODE -> new SystemStateV2[]{SYSTEM_INIT, EMERGENCY_MODE};
            case EMERGENCY_MODE -> new SystemStateV2[]{SYSTEM_INIT, ALL_RED_TRANSITION};
        };
    }

    /**
     * 检查是否可以转换到指定状态
     * @param targetState 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(SystemStateV2 targetState) {
        SystemStateV2[] possibleTransitions = getPossibleTransitions();
        for (SystemStateV2 state : possibleTransitions) {
            if (state == targetState) {
                return true;
            }
        }
        return false;
    }

    // ==================== 工具方法 ====================

    /**
     * 根据代码获取状态
     * @param code 状态代码
     * @return 对应的状态，如果未找到则返回null
     */
    public static SystemStateV2 fromCode(String code) {
        for (SystemStateV2 state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }

    /**
     * 获取所有正常运行状态
     * @return 正常运行状态数组
     */
    public static SystemStateV2[] getNormalStates() {
        return new SystemStateV2[]{INDUCTIVE_MODE};
    }

    /**
     * 获取所有故障状态
     * @return 故障状态数组
     */
    public static SystemStateV2[] getFaultStates() {
        return new SystemStateV2[]{DEGRADED_MODE, EMERGENCY_MODE};
    }

    /**
     * 获取所有安全状态
     * @return 安全状态数组
     */
    public static SystemStateV2[] getSafetyStates() {
        return new SystemStateV2[]{ALL_RED_TRANSITION, EMERGENCY_MODE};
    }

    @Override
    public String toString() {
        return String.format("%s(%s): %s", chineseName, code, description);
    }
}
