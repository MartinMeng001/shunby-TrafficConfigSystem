package com.traffic.config.statemachinev3.enums.segment;

/**
 * 清空决策枚举
 * 对应数学模型中的清空决策集合
 *
 * @author System
 * @version 3.0.0
 */
public enum ClearanceDecision {

    /**
     * 安全清空
     * - 车辆ID集合为空且计数器平衡
     * - 可以安全进行状态转换
     */
    SAFE("安全清空", "SAFE", 4, "车辆已安全清空，可以进行状态转换"),

    /**
     * 警告清空
     * - 车辆ID集合为空但计数器不平衡
     * - 存在数据不一致但可以转换
     */
    WARNING("警告清空", "WARNING", 3, "数据不一致但可以清空"),

    /**
     * 保守清空
     * - 车辆ID集合不为空但计数器平衡
     * - 需要等待保守清空计时器
     */
    CONSERVATIVE("保守清空", "CONSERVATIVE", 2, "需要保守清空等待"),

    /**
     * 等待清空
     * - 车辆ID集合不为空且计数器不平衡
     * - 不能进行状态转换
     */
    WAIT("等待清空", "WAIT", 1, "路段未清空，需要等待");

    // ==================== 枚举属性 ====================

    /**
     * 中文名称
     */
    private final String chineseName;

    /**
     * 英文代码
     */
    private final String code;

    /**
     * 安全等级（数值越高越安全）
     */
    private final int safetyLevel;

    /**
     * 描述
     */
    private final String description;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param chineseName 中文名称
     * @param code 英文代码
     * @param safetyLevel 安全等级
     * @param description 描述
     */
    ClearanceDecision(String chineseName, String code, int safetyLevel, String description) {
        this.chineseName = chineseName;
        this.code = code;
        this.safetyLevel = safetyLevel;
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
     * 获取描述
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    // ==================== 判断方法 ====================

    /**
     * 判断是否可以安全转换
     * @return 是否可以安全转换
     */
    public boolean isSafeForTransition() {
        return this == SAFE || this == WARNING;
    }

    /**
     * 判断是否需要保守处理
     * @return 是否需要保守处理
     */
    public boolean requiresConservativeHandling() {
        return this == CONSERVATIVE;
    }

    /**
     * 判断是否需要等待
     * @return 是否需要等待
     */
    public boolean requiresWaiting() {
        return this == WAIT;
    }

    // ==================== 工具方法 ====================

    /**
     * 根据代码获取清空决策
     * @param code 决策代码
     * @return 对应的清空决策，如果未找到则返回null
     */
    public static ClearanceDecision fromCode(String code) {
        for (ClearanceDecision decision : values()) {
            if (decision.getCode().equals(code)) {
                return decision;
            }
        }
        return null;
    }

    /**
     * 根据路段状态计算清空决策
     * @param idsEmpty 车辆ID集合是否为空
     * @param countersBalanced 计数器是否平衡
     * @return 对应的清空决策
     */
    public static ClearanceDecision calculateDecision(boolean idsEmpty, boolean countersBalanced) {
        if (idsEmpty && countersBalanced) {
            return SAFE;
        } else if (idsEmpty && !countersBalanced) {
            return WARNING;
        } else if (!idsEmpty && countersBalanced) {
            return CONSERVATIVE;
        } else {
            return WAIT;
        }
    }

    /**
     * 计算综合清空决策
     * @param upstreamDecision 上行清空决策
     * @param downstreamDecision 下行清空决策
     * @return 综合清空决策
     */
    public static ClearanceDecision calculateOverallDecision(ClearanceDecision upstreamDecision,
                                                             ClearanceDecision downstreamDecision) {
        if (upstreamDecision == SAFE && downstreamDecision == SAFE) {
            return SAFE;
        } else if ((upstreamDecision == SAFE || upstreamDecision == WARNING) &&
                (downstreamDecision == SAFE || downstreamDecision == WARNING)) {
            return WARNING;
        } else if (upstreamDecision == CONSERVATIVE || downstreamDecision == CONSERVATIVE) {
            return CONSERVATIVE;
        } else {
            return WAIT;
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s) [安全等级:%d]: %s",
                chineseName, code, safetyLevel, description);
    }
}
