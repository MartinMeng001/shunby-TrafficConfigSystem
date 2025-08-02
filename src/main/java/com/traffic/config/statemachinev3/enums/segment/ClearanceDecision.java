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
     * 获取安全等级
     * @return 安全等级
     */
    public int getSafetyLevel() {
        return safetyLevel;
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

    /**
     * 判断是否为最安全级别
     * @return 是否为最安全级别
     */
    public boolean isMostSafe() {
        return this == SAFE;
    }

    /**
     * 判断是否存在数据问题
     * @return 是否存在数据问题
     */
    public boolean hasDataIssues() {
        return this == WARNING || this == CONSERVATIVE || this == WAIT;
    }

    /**
     * 判断是否允许强制清空
     * @return 是否允许强制清空
     */
    public boolean allowsForceClear() {
        return this != SAFE; // 只有非SAFE状态才需要强制清空
    }

    // ==================== 比较方法 ====================

    /**
     * 比较安全等级
     * @param other 另一个清空决策
     * @return 安全等级比较结果，正数表示当前决策更安全
     */
    public int compareSafety(ClearanceDecision other) {
        return this.safetyLevel - other.safetyLevel;
    }

    /**
     * 判断是否比另一个决策更安全
     * @param other 另一个清空决策
     * @return 是否更安全
     */
    public boolean isSaferThan(ClearanceDecision other) {
        return this.safetyLevel > other.safetyLevel;
    }

    /**
     * 判断是否与另一个决策安全等级相同
     * @param other 另一个清空决策
     * @return 是否安全等级相同
     */
    public boolean hasSameSafetyLevel(ClearanceDecision other) {
        return this.safetyLevel == other.safetyLevel;
    }

    // ==================== 组合决策方法 ====================

    /**
     * 组合两个清空决策，返回更保守的结果
     * @param other 另一个清空决策
     * @return 更保守的清空决策
     */
    public ClearanceDecision combineConservative(ClearanceDecision other) {
        return this.safetyLevel <= other.safetyLevel ? this : other;
    }

    /**
     * 组合两个清空决策，返回更激进的结果
     * @param other 另一个清空决策
     * @return 更激进的清空决策
     */
    public ClearanceDecision combineAggressive(ClearanceDecision other) {
        return this.safetyLevel >= other.safetyLevel ? this : other;
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

    /**
     * 获取所有可以进行转换的决策
     * @return 可以进行转换的决策数组
     */
    public static ClearanceDecision[] getTransitionReadyDecisions() {
        return new ClearanceDecision[]{SAFE, WARNING};
    }

    /**
     * 获取所有需要等待的决策
     * @return 需要等待的决策数组
     */
    public static ClearanceDecision[] getWaitingDecisions() {
        return new ClearanceDecision[]{CONSERVATIVE, WAIT};
    }

    /**
     * 获取最安全的决策
     * @return 最安全的决策
     */
    public static ClearanceDecision getMostSafeDecision() {
        return SAFE;
    }

    /**
     * 获取最不安全的决策
     * @return 最不安全的决策
     */
    public static ClearanceDecision getLeastSafeDecision() {
        return WAIT;
    }

    /**
     * 检查决策组合是否允许系统级转换
     * @param decisions 决策数组
     * @return 是否允许系统级转换
     */
    public static boolean allowsSystemTransition(ClearanceDecision[] decisions) {
        for (ClearanceDecision decision : decisions) {
            if (!decision.isSafeForTransition()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算决策数组中的最低安全等级
     * @param decisions 决策数组
     * @return 最低安全等级的决策
     */
    public static ClearanceDecision getMinimumSafetyLevel(ClearanceDecision[] decisions) {
        if (decisions == null || decisions.length == 0) {
            return WAIT;
        }

        ClearanceDecision minimum = decisions[0];
        for (int i = 1; i < decisions.length; i++) {
            if (decisions[i].safetyLevel < minimum.safetyLevel) {
                minimum = decisions[i];
            }
        }
        return minimum;
    }

    /**
     * 计算决策数组中的最高安全等级
     * @param decisions 决策数组
     * @return 最高安全等级的决策
     */
    public static ClearanceDecision getMaximumSafetyLevel(ClearanceDecision[] decisions) {
        if (decisions == null || decisions.length == 0) {
            return WAIT;
        }

        ClearanceDecision maximum = decisions[0];
        for (int i = 1; i < decisions.length; i++) {
            if (decisions[i].safetyLevel > maximum.safetyLevel) {
                maximum = decisions[i];
            }
        }
        return maximum;
    }

    /**
     * 检查是否所有决策都是安全的
     * @param decisions 决策数组
     * @return 是否所有决策都是安全的
     */
    public static boolean areAllSafe(ClearanceDecision[] decisions) {
        for (ClearanceDecision decision : decisions) {
            if (decision != SAFE) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否存在需要保守处理的决策
     * @param decisions 决策数组
     * @return 是否存在保守决策
     */
    public static boolean hasConservativeDecisions(ClearanceDecision[] decisions) {
        for (ClearanceDecision decision : decisions) {
            if (decision == CONSERVATIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否存在需要等待的决策
     * @param decisions 决策数组
     * @return 是否存在等待决策
     */
    public static boolean hasWaitingDecisions(ClearanceDecision[] decisions) {
        for (ClearanceDecision decision : decisions) {
            if (decision == WAIT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) [安全等级:%d]: %s",
                chineseName, code, safetyLevel, description);
    }
}
