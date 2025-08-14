package com.traffic.config.statemachinev3.enums.segment;

/**
 * 路段状态枚举
 * 对应数学模型中的状态集合 Q
 *
 * Q = {UPSTREAM_GREEN, DOWNSTREAM_GREEN, ALL_RED_CLEAR}
 *
 * @author System
 * @version 3.0.0
 */
public enum SegmentState {

    /**
     * 上行绿灯状态
     * - 允许上行方向车辆通行，禁止下行方向车辆进入
     * - 不变量：upstream_signal = GREEN ∧ downstream_signal = RED
     * - 持续条件：绿灯时间在 [min_green_time, max_green_time] 范围内
     * - 容量约束：|upstream_vehicle_ids| ≤ upstream_capacity
     */
    UPSTREAM_GREEN("上行绿灯", "UG", "允许上行方向车辆通行", SignalState.UPSTREAM_ACTIVE),

    /**
     * 下行绿灯状态
     * - 允许下行方向车辆通行，禁止上行方向车辆进入
     * - 不变量：downstream_signal = GREEN ∧ upstream_signal = RED
     * - 持续条件：绿灯时间在 [min_green_time, max_green_time] 范围内
     * - 容量约束：|downstream_vehicle_ids| ≤ downstream_capacity
     */
    DOWNSTREAM_GREEN("下行绿灯", "DG", "允许下行方向车辆通行", SignalState.DOWNSTREAM_ACTIVE),

    /**
     * 全红清空状态
     * - 上行下行均为红灯，用于方向切换前的安全清空缓冲
     * - 不变量：upstream_signal = RED ∧ downstream_signal = RED
     * - 持续条件：清空时间不超过 max_red_time
     * - 清空目标：所有车辆安全离开路段
     */
    ALL_RED_CLEAR("全红清空", "RC", "所有方向红灯，等待路段清空", SignalState.ALL_RED),

    /**
     * 黄闪故障状态
     * - 上行下行均为黄闪，不再进行感应控制，但仍对上行和下行的车辆进行统计
     * - 不变量：upstream_signal = YELLOWFLASH ∧ downstream_signal = YELLOWFLASH
     * - 持续条件：等待人工解除，
     * - 目标：等待人工解除
     */
    ALL_YELLOWFLASH_MANUAL("黄闪", "YF", "所有方向黄灯，等待人工处理", SignalState.ALL_YELLOWFLASH),
    /**
     * 空转状态，非感应状态
     * - 上行下行均为不受平台控制，不再进行感应控制，但仍对上行和下行的车辆进行统计
     * - 不变量：upstream_signal = NOCTRL ∧ downstream_signal = NOCTRL
     * - 持续条件：等待上位状态机取消
     * - 目标：等待上位状态机取消
     */
    ALL_NOCTRL("空转", "NC", "不控制信号灯灯色，由信号机控制", SignalState.NOCTRL);

    // ==================== 信号状态枚举 ====================

    /**
     * 信号灯状态配置
     */
    public enum SignalState {
        UPSTREAM_ACTIVE("上行通行", true, false),
        DOWNSTREAM_ACTIVE("下行通行", false, true),
        ALL_RED("全红", false, false),
        ALL_YELLOWFLASH("黄闪", false, false),
        NOCTRL("空转", false, false);

        private final String description;
        private final boolean upstreamGreen;
        private final boolean downstreamGreen;

        SignalState(String description, boolean upstreamGreen, boolean downstreamGreen) {
            this.description = description;
            this.upstreamGreen = upstreamGreen;
            this.downstreamGreen = downstreamGreen;
        }

        public String getDescription() {
            return description;
        }

        public boolean isUpstreamGreen() {
            return upstreamGreen;
        }

        public boolean isDownstreamGreen() {
            return downstreamGreen;
        }

        public boolean isAllRed() {
            return !upstreamGreen && !downstreamGreen;
        }
    }

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

    /**
     * 对应的信号状态
     */
    private final SignalState signalState;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param chineseName 中文名称
     * @param code 英文代码
     * @param description 状态描述
     * @param signalState 信号状态
     */
    SegmentState(String chineseName, String code, String description, SignalState signalState) {
        this.chineseName = chineseName;
        this.code = code;
        this.description = description;
        this.signalState = signalState;
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

    /**
     * 获取信号状态
     * @return 信号状态
     */
    public SignalState getSignalState() {
        return signalState;
    }

    // ==================== 状态属性判断方法 ====================

    /**
     * 判断是否为绿灯状态
     * @return 是否为绿灯状态
     */
    public boolean isGreenState() {
        return this == UPSTREAM_GREEN || this == DOWNSTREAM_GREEN;
    }

    /**
     * 判断是否为全红状态
     * @return 是否为全红状态
     */
    public boolean isAllRedState() {
        return this == ALL_RED_CLEAR;
    }

    public boolean isYellowFlashState(){
        return this == ALL_YELLOWFLASH_MANUAL;
    }

    public boolean isNoCtrlState(){
        return this == ALL_NOCTRL;
    }

    /**
     * 判断是否为上行状态
     * @return 是否为上行状态
     */
    public boolean isUpstreamState() {
        return this == UPSTREAM_GREEN;
    }

    /**
     * 判断是否为下行状态
     * @return 是否为下行状态
     */
    public boolean isDownstreamState() {
        return this == DOWNSTREAM_GREEN;
    }

    /**
     * 判断上行信号是否为绿灯
     * @return 上行信号是否为绿灯
     */
    public boolean isUpstreamGreen() {
        return signalState.isUpstreamGreen();
    }

    /**
     * 判断下行信号是否为绿灯
     * @return 下行信号是否为绿灯
     */
    public boolean isDownstreamGreen() {
        return signalState.isDownstreamGreen();
    }

    /**
     * 判断是否允许上行车辆进入
     * @return 是否允许上行车辆进入
     */
    public boolean allowsUpstreamEntry() {
        return this == UPSTREAM_GREEN;
    }

    /**
     * 判断是否允许下行车辆进入
     * @return 是否允许下行车辆进入
     */
    public boolean allowsDownstreamEntry() {
        return this == DOWNSTREAM_GREEN;
    }

    /**
     * 判断是否为过渡状态
     * @return 是否为过渡状态
     */
    public boolean isTransitionState() {
        return this == ALL_RED_CLEAR;
    }

    // ==================== 状态转换相关方法 ====================

    /**
     * 获取可以直接转换到的状态集合
     * @return 可转换状态数组
     */
    public SegmentState[] getPossibleTransitions() {
        return switch (this) {
            case UPSTREAM_GREEN -> new SegmentState[]{ALL_RED_CLEAR};
            case DOWNSTREAM_GREEN -> new SegmentState[]{ALL_RED_CLEAR};
            case ALL_RED_CLEAR -> new SegmentState[]{UPSTREAM_GREEN, DOWNSTREAM_GREEN};
            case ALL_YELLOWFLASH_MANUAL -> new SegmentState[]{ALL_YELLOWFLASH_MANUAL};
            case ALL_NOCTRL -> new SegmentState[]{ALL_NOCTRL};
        };
    }

    /**
     * 检查是否可以转换到指定状态
     * @param targetState 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(SegmentState targetState) {
        SegmentState[] possibleTransitions = getPossibleTransitions();
        for (SegmentState state : possibleTransitions) {
            if (state == targetState) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取相反方向的绿灯状态
     * @return 相反方向的绿灯状态，如果当前是全红状态则返回null
     */
    public SegmentState getOppositeDirection() {
        return switch (this) {
            case UPSTREAM_GREEN -> DOWNSTREAM_GREEN;
            case DOWNSTREAM_GREEN -> UPSTREAM_GREEN;
            case ALL_RED_CLEAR, ALL_YELLOWFLASH_MANUAL, ALL_NOCTRL -> null;
        };
    }

    /**
     * 检查状态转换是否需要经过全红清空
     * @param fromState 起始状态
     * @param toState 目标状态
     * @return 是否需要经过全红清空
     */
    public static boolean requiresClearanceTransition(SegmentState fromState, SegmentState toState) {
        // 从绿灯状态到相反方向的绿灯状态需要经过全红清空
        return (fromState == UPSTREAM_GREEN && toState == DOWNSTREAM_GREEN) ||
                (fromState == DOWNSTREAM_GREEN && toState == UPSTREAM_GREEN);
    }

    // ==================== 工具方法 ====================

    /**
     * 根据代码获取状态
     * @param code 状态代码
     * @return 对应的状态，如果未找到则返回null
     */
    public static SegmentState fromCode(String code) {
        for (SegmentState state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }

    /**
     * 获取所有绿灯状态
     * @return 绿灯状态数组
     */
    public static SegmentState[] getGreenStates() {
        return new SegmentState[]{UPSTREAM_GREEN, DOWNSTREAM_GREEN};
    }

    /**
     * 获取所有全红状态
     * @return 全红状态数组
     */
    public static SegmentState[] getAllRedStates() {
        return new SegmentState[]{ALL_RED_CLEAR};
    }

    /**
     * 根据方向获取对应的绿灯状态
     * @param isUpstream 是否为上行方向
     * @return 对应的绿灯状态
     */
    public static SegmentState getGreenStateByDirection(boolean isUpstream) {
        return isUpstream ? UPSTREAM_GREEN : DOWNSTREAM_GREEN;
    }

    /**
     * 检查两个状态是否为相反方向
     * @param state1 状态1
     * @param state2 状态2
     * @return 是否为相反方向
     */
    public static boolean areOppositeDirections(SegmentState state1, SegmentState state2) {
        return (state1 == UPSTREAM_GREEN && state2 == DOWNSTREAM_GREEN) ||
                (state1 == DOWNSTREAM_GREEN && state2 == UPSTREAM_GREEN);
    }

    /**
     * 检查状态是否满足互斥性（不能同时为绿灯）
     * @param upstreamState 上行状态
     * @param downstreamState 下行状态
     * @return 是否满足互斥性
     */
    public static boolean checkMutualExclusion(SegmentState upstreamState, SegmentState downstreamState) {
        // 不能同时有两个方向为绿灯
        return !(upstreamState.isUpstreamGreen() && downstreamState.isDownstreamGreen());
    }

    @Override
    public String toString() {
        return String.format("%s(%s): %s [%s]",
                chineseName, code, description, signalState.getDescription());
    }
}
