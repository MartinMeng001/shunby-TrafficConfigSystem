// ==================== 路段数据传输对象 ====================

/**
 * 路段配置数据传输对象
 * 用于前后端路段数据传输的标准化数据结构
 *
 * @author System
 * @version 1.0.0
 */
package com.traffic.config.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    /**
     * 路段ID（系统内部使用）
     */
    private String segmentId;

    /**
     * 路段名称
     */
    @NotBlank(message = "路段名称不能为空")
    @Size(min = 2, max = 50, message = "路段名称长度必须在2-50个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s\\-_]+$",
            message = "路段名称只能包含中文、英文、数字、空格、横线和下划线")
    private String name;

    /**
     * 信号灯ID
     */
    @NotBlank(message = "信号灯ID不能为空")
    @Pattern(regexp = "^[0-9]{5}$", message = "信号灯ID必须是5位数字")
    private String sigid;

    /**
     * 路段显示名称（用于前端显示）
     */
    @Size(max = 100, message = "显示名称不能超过100个字符")
    private String displayName;

    /**
     * 路段描述
     */
    @Size(max = 500, message = "路段描述不能超过500个字符")
    private String description;

    /**
     * 路段代码（用于系统集成）
     */
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "路段代码必须是2-10位大写字母和数字")
    private String segmentCode;

    // ==================== 时间配置 ====================

    /**
     * 全红时间（秒）
     */
    @NotNull(message = "全红时间不能为空")
    @Min(value = 10, message = "全红时间不能小于10秒")
    @Max(value = 300, message = "全红时间不能大于300秒")
    private Integer allred;

    /**
     * 绿灯最小时间（秒）
     */
    @Min(value = 5, message = "绿灯最小时间不能小于5秒")
    @Max(value = 120, message = "绿灯最小时间不能大于120秒")
    private Integer minGreenTime;

    /**
     * 绿灯最大时间（秒）
     */
    @Min(value = 10, message = "绿灯最大时间不能小于10秒")
    @Max(value = 300, message = "绿灯最大时间不能大于300秒")
    private Integer maxGreenTime;

    /**
     * 黄灯时间（秒）
     */
    @Min(value = 2, message = "黄灯时间不能小于2秒")
    @Max(value = 10, message = "黄灯时间不能大于10秒")
    private Integer yellowTime;

    /**
     * 红灯清空时间（秒）
     */
    @Min(value = 1, message = "红灯清空时间不能小于1秒")
    @Max(value = 30, message = "红灯清空时间不能大于30秒")
    private Integer redClearTime;

    // ==================== 相位配置 ====================

    /**
     * 上行控制相位
     */
    @NotNull(message = "上行控制相位不能为空")
    @Min(value = 1, message = "上行控制相位必须大于0")
    @Max(value = 8, message = "上行控制相位不能大于8")
    private Integer upctrl;

    /**
     * 下行控制相位
     */
    @NotNull(message = "下行控制相位不能为空")
    @Min(value = 1, message = "下行控制相位必须大于0")
    @Max(value = 8, message = "下行控制相位不能大于8")
    private Integer downctrl;

    /**
     * 上行相位名称
     */
    @Size(max = 30, message = "上行相位名称不能超过30个字符")
    private String upctrlName;

    /**
     * 下行相位名称
     */
    @Size(max = 30, message = "下行相位名称不能超过30个字符")
    private String downctrlName;

    /**
     * 相位冲突检测
     */
    private Boolean phaseConflictCheck;

    /**
     * 相位切换延迟（秒）
     */
    @Min(value = 0, message = "相位切换延迟不能小于0秒")
    @Max(value = 30, message = "相位切换延迟不能大于30秒")
    private Integer phaseSwitchDelay;

    // ==================== 会车区配置 ====================

    /**
     * 进入会车区编号
     */
    @NotNull(message = "进入会车区编号不能为空")
    @Min(value = 1, message = "进入会车区编号必须大于0")
    @Max(value = 10, message = "进入会车区编号不能大于10")
    private Integer inzone;

    /**
     * 离开会车区编号
     */
    @NotNull(message = "离开会车区编号不能为空")
    @Min(value = 1, message = "离开会车区编号必须大于0")
    @Max(value = 10, message = "离开会车区编号不能大于10")
    private Integer outzone;

    /**
     * 进入会车区名称
     */
    @Size(max = 30, message = "进入会车区名称不能超过30个字符")
    private String inzoneName;

    /**
     * 离开会车区名称
     */
    @Size(max = 30, message = "离开会车区名称不能超过30个字符")
    private String outzoneName;

    /**
     * 会车区容量检查
     */
    private Boolean zoneCapacityCheck;

    /**
     * 会车区最大等待时间（秒）
     */
    @Min(value = 10, message = "会车区最大等待时间不能小于10秒")
    @Max(value = 600, message = "会车区最大等待时间不能大于600秒")
    private Integer maxZoneWaitTime;

    // ==================== 路段属性 ====================

    /**
     * 路段类型
     */
    @NotBlank(message = "路段类型不能为空")
    @Pattern(regexp = "^(START|MIDDLE|END|JUNCTION)$",
            message = "路段类型必须是START、MIDDLE、END或JUNCTION")
    private String segmentType;

    /**
     * 路段长度（米）
     */
    @Min(value = 10, message = "路段长度不能小于10米")
    @Max(value = 2000, message = "路段长度不能大于2000米")
    private Integer length;

    /**
     * 路段宽度（米）
     */
    @DecimalMin(value = "2.5", message = "路段宽度不能小于2.5米")
    @DecimalMax(value = "10.0", message = "路段宽度不能大于10米")
    private Double width;

    /**
     * 最大通行速度（km/h）
     */
    @Min(value = 5, message = "最大通行速度不能小于5km/h")
    @Max(value = 80, message = "最大通行速度不能大于80km/h")
    private Integer maxSpeed;

    /**
     * 建议通行速度（km/h）
     */
    @Min(value = 5, message = "建议通行速度不能小于5km/h")
    @Max(value = 60, message = "建议通行速度不能大于60km/h")
    private Integer recommendedSpeed;

    /**
     * 路段坡度（百分比）
     */
    @DecimalMin(value = "-15.0", message = "路段坡度不能小于-15%")
    @DecimalMax(value = "15.0", message = "路段坡度不能大于15%")
    private Double gradient;

    /**
     * 路面状况
     */
    @Pattern(regexp = "^(EXCELLENT|GOOD|FAIR|POOR)$",
            message = "路面状况必须是EXCELLENT、GOOD、FAIR或POOR")
    private String roadCondition;

    // ==================== 优先级和权重 ====================

    /**
     * 路段优先级（1-10，数字越大优先级越高）
     */
    @Min(value = 1, message = "路段优先级不能小于1")
    @Max(value = 10, message = "路段优先级不能大于10")
    private Integer priority;

    /**
     * 上行权重
     */
    @DecimalMin(value = "0.1", message = "上行权重不能小于0.1")
    @DecimalMax(value = "5.0", message = "上行权重不能大于5.0")
    private Double upstreamWeight;

    /**
     * 下行权重
     */
    @DecimalMin(value = "0.1", message = "下行权重不能小于0.1")
    @DecimalMax(value = "5.0", message = "下行权重不能大于5.0")
    private Double downstreamWeight;

    /**
     * 动态优先级调整
     */
    private Boolean dynamicPriorityEnabled;

    /**
     * 优先级调整因子
     */
    @DecimalMin(value = "0.5", message = "优先级调整因子不能小于0.5")
    @DecimalMax(value = "2.0", message = "优先级调整因子不能大于2.0")
    private Double priorityAdjustmentFactor;

    // ==================== 检测器配置 ====================

    /**
     * 检测器配置
     */
    @Valid
    private List<DetectorConfigDTO> detectors;

    /**
     * 检测器延迟时间（秒）
     */
    @Min(value = 0, message = "检测器延迟时间不能小于0秒")
    @Max(value = 10, message = "检测器延迟时间不能大于10秒")
    private Integer detectorDelay;

    /**
     * 检测器灵敏度（1-10）
     */
    @Min(value = 1, message = "检测器灵敏度不能小于1")
    @Max(value = 10, message = "检测器灵敏度不能大于10")
    private Integer detectorSensitivity;

    /**
     * 检测器故障处理方式
     */
    @Pattern(regexp = "^(IGNORE|WARNING|FALLBACK|SHUTDOWN)$",
            message = "检测器故障处理方式必须是IGNORE、WARNING、FALLBACK或SHUTDOWN")
    private String detectorFailureAction;

    // ==================== 感应控制 ====================

    /**
     * 是否启用感应控制
     */
    private Boolean inductiveControlEnabled;

    /**
     * 感应控制配置
     */
    @Valid
    private InductiveControlDTO inductiveControl;

    /**
     * 车辆到达检测延迟（秒）
     */
    @Min(value = 0, message = "车辆到达检测延迟不能小于0秒")
    @Max(value = 30, message = "车辆到达检测延迟不能大于30秒")
    private Integer arrivalDetectionDelay;

    /**
     * 车辆离开检测延迟（秒）
     */
    @Min(value = 0, message = "车辆离开检测延迟不能小于0秒")
    @Max(value = 30, message = "车辆离开检测延迟不能大于30秒")
    private Integer departureDetectionDelay;

    // ==================== 时段配置 ====================

    /**
     * 时段配置列表
     */
    @Valid
    private List<TimeSlotConfigDTO> timeSlotConfigs;

    /**
     * 是否启用高峰时段特殊控制
     */
    private Boolean peakHourEnabled;

    /**
     * 夜间模式配置
     */
    @Valid
    private NightModeConfigDTO nightModeConfig;

    /**
     * 节假日配置
     */
    @Valid
    private HolidayConfigDTO holidayConfig;

    // ==================== 状态和控制 ====================

    /**
     * 是否启用
     */
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    /**
     * 当前状态
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE|MAINTENANCE|ERROR)$",
            message = "状态必须是ACTIVE、INACTIVE、MAINTENANCE或ERROR")
    private String status;

    /**
     * 控制模式
     */
    @Pattern(regexp = "^(AUTO|MANUAL|TIMED|INDUCTIVE)$",
            message = "控制模式必须是AUTO、MANUAL、TIMED或INDUCTIVE")
    private String controlMode;

    /**
     * 是否允许手动控制
     */
    private Boolean manualControlAllowed;

    /**
     * 手动控制超时时间（分钟）
     */
    @Min(value = 1, message = "手动控制超时时间不能小于1分钟")
    @Max(value = 60, message = "手动控制超时时间不能大于60分钟")
    private Integer manualControlTimeout;

    // ==================== 位置和关联 ====================

    /**
     * 在配置中的位置索引
     */
    @Min(value = 0, message = "位置索引不能小于0")
    private Integer index;

    /**
     * 前一个路段ID
     */
    private String previousSegmentId;

    /**
     * 后一个路段ID
     */
    private String nextSegmentId;

    /**
     * GPS坐标 - 纬度
     */
    @DecimalMin(value = "-90.0", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度必须在-90到90之间")
    private Double latitude;

    /**
     * GPS坐标 - 经度
     */
    @DecimalMin(value = "-180.0", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度必须在-180到180之间")
    private Double longitude;

    /**
     * 海拔高度（米）
     */
    private Double altitude;

    /**
     * 关联的路口ID
     */
    private String junctionId;

    /**
     * 关联的道路ID
     */
    private String roadId;

    // ==================== 约束和规则 ====================

    /**
     * 约束条件列表
     */
    @Valid
    private List<ConstraintDTO> constraints;

    /**
     * 业务规则列表
     */
    @Valid
    private List<BusinessRuleDTO> businessRules;

    /**
     * 最大等待队列长度
     */
    @Min(value = 1, message = "最大等待队列长度不能小于1")
    @Max(value = 100, message = "最大等待队列长度不能大于100")
    private Integer maxQueueLength;

    /**
     * 队列溢出处理方式
     */
    @Pattern(regexp = "^(BLOCK|REDIRECT|EXTEND|ALERT)$",
            message = "队列溢出处理方式必须是BLOCK、REDIRECT、EXTEND或ALERT")
    private String queueOverflowAction;

    // ==================== 元数据 ====================

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    /**
     * 创建者
     */
    @Size(max = 50, message = "创建者名称不能超过50个字符")
    private String createdBy;

    /**
     * 修改者
     */
    @Size(max = 50, message = "修改者名称不能超过50个字符")
    private String modifiedBy;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注不能超过1000个字符")
    private String remarks;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 自定义属性
     */
    private Map<String, Object> customProperties;

    // ==================== 内部类：检测器配置 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DetectorConfigDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 检测器ID
         */
        @NotBlank(message = "检测器ID不能为空")
        private String detectorId;

        /**
         * 检测器名称
         */
        @NotBlank(message = "检测器名称不能为空")
        @Size(max = 50, message = "检测器名称不能超过50个字符")
        private String name;

        /**
         * 检测器类型
         */
        @NotBlank(message = "检测器类型不能为空")
        @Pattern(regexp = "^(LOOP|RADAR|VIDEO|ULTRASONIC|INFRARED)$",
                message = "检测器类型必须是LOOP、RADAR、VIDEO、ULTRASONIC或INFRARED")
        private String type;

        /**
         * 检测器位置
         */
        @NotBlank(message = "检测器位置不能为空")
        @Pattern(regexp = "^(UPSTREAM|DOWNSTREAM|MIDDLE)$",
                message = "检测器位置必须是UPSTREAM、DOWNSTREAM或MIDDLE")
        private String position;

        /**
         * 距离路段起点的距离（米）
         */
        @Min(value = 0, message = "距离不能小于0米")
        private Integer distanceFromStart;

        /**
         * 检测区域长度（米）
         */
        @Min(value = 1, message = "检测区域长度不能小于1米")
        @Max(value = 50, message = "检测区域长度不能大于50米")
        private Integer detectionLength;

        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled;

        /**
         * 检测灵敏度
         */
        @Min(value = 1, message = "检测灵敏度不能小于1")
        @Max(value = 10, message = "检测灵敏度不能大于10")
        private Integer sensitivity;

        /**
         * 检测延迟（秒）
         */
        @Min(value = 0, message = "检测延迟不能小于0秒")
        @Max(value = 10, message = "检测延迟不能大于10秒")
        private Integer delay;
    }

    // ==================== 内部类：感应控制配置 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InductiveControlDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 延伸绿灯时间（秒）
         */
        @Min(value = 1, message = "延伸绿灯时间不能小于1秒")
        @Max(value = 30, message = "延伸绿灯时间不能大于30秒")
        private Integer extensionTime;

        /**
         * 最大延伸次数
         */
        @Min(value = 1, message = "最大延伸次数不能小于1")
        @Max(value = 10, message = "最大延伸次数不能大于10")
        private Integer maxExtensions;

        /**
         * 车辆到达间隔阈值（秒）
         */
        @Min(value = 1, message = "车辆到达间隔阈值不能小于1秒")
        @Max(value = 60, message = "车辆到达间隔阈值不能大于60秒")
        private Integer arrivalThreshold;

        /**
         * 车辆离开检测时间（秒）
         */
        @Min(value = 1, message = "车辆离开检测时间不能小于1秒")
        @Max(value = 30, message = "车辆离开检测时间不能大于30秒")
        private Integer departureDetectionTime;

        /**
         * 是否启用流量自适应
         */
        private Boolean trafficAdaptiveEnabled;

        /**
         * 流量自适应参数
         */
        private Map<String, Object> adaptiveParameters;
    }

    // ==================== 内部类：时段配置 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimeSlotConfigDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 时段名称
         */
        @NotBlank(message = "时段名称不能为空")
        @Size(max = 30, message = "时段名称不能超过30个字符")
        private String name;

        /**
         * 开始时间（HH:mm格式）
         */
        @NotBlank(message = "开始时间不能为空")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "开始时间格式必须为HH:mm")
        private String startTime;

        /**
         * 结束时间（HH:mm格式）
         */
        @NotBlank(message = "结束时间不能为空")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "结束时间格式必须为HH:mm")
        private String endTime;

        /**
         * 适用星期（1-7，1表示周一）
         */
        @NotEmpty(message = "适用星期不能为空")
        private List<@Min(value = 1) @Max(value = 7) Integer> weekDays;

        /**
         * 绿灯时间调整系数
         */
        @DecimalMin(value = "0.5", message = "绿灯时间调整系数不能小于0.5")
        @DecimalMax(value = "3.0", message = "绿灯时间调整系数不能大于3.0")
        private Double greenTimeMultiplier;

        /**
         * 优先级调整
         */
        @Min(value = -5, message = "优先级调整不能小于-5")
        @Max(value = 5, message = "优先级调整不能大于5")
        private Integer priorityAdjustment;

        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled;
    }

    // ==================== 内部类：夜间模式配置 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NightModeConfigDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 是否启用夜间模式
         */
        private Boolean enabled;

        /**
         * 夜间模式开始时间
         */
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式必须为HH:mm")
        private String startTime;

        /**
         * 夜间模式结束时间
         */
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式必须为HH:mm")
        private String endTime;

        /**
         * 夜间全红时间
         */
        @Min(value = 5, message = "夜间全红时间不能小于5秒")
        @Max(value = 600, message = "夜间全红时间不能大于600秒")
        private Integer nightAllRed;

        /**
         * 夜间绿灯时间
         */
        @Min(value = 10, message = "夜间绿灯时间不能小于10秒")
        @Max(value = 300, message = "夜间绿灯时间不能大于300秒")
        private Integer nightGreenTime;

        /**
         * 是否启用黄闪模式
         */
        private Boolean yellowFlashEnabled;
    }

    // ==================== 内部类：节假日配置 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HolidayConfigDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 是否启用节假日特殊配置
         */
        private Boolean enabled;

        /**
         * 节假日时间配置方案
         */
        @Pattern(regexp = "^(WEEKEND|NORMAL|CUSTOM)$",
                message = "节假日配置方案必须是WEEKEND、NORMAL或CUSTOM")
        private String holidayTimingPlan;

        /**
         * 自定义节假日时间参数
         */
        private Map<String, Object> customTimingParameters;
    }

    // ==================== 内部类：约束条件 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConstraintDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 约束类型
         */
        @NotBlank(message = "约束类型不能为空")
        @Pattern(regexp = "^(TIME|PHASE|ZONE|PRIORITY|FLOW|SAFETY|CUSTOM)$",
                message = "约束类型必须是TIME、PHASE、ZONE、PRIORITY、FLOW、SAFETY或CUSTOM")
        private String type;

        /**
         * 约束名称
         */
        @NotBlank(message = "约束名称不能为空")
        @Size(max = 50, message = "约束名称不能超过50个字符")
        private String name;

        /**
         * 约束描述
         */
        @Size(max = 200, message = "约束描述不能超过200个字符")
        private String description;

        /**
         * 约束条件表达式
         */
        @NotBlank(message = "约束条件表达式不能为空")
        @Size(max = 500, message = "约束条件表达式不能超过500个字符")
        private String expression;

        /**
         * 约束参数
         */
        private Map<String, Object> parameters;

        /**
         * 约束级别（1-10，数字越大级别越高）
         */
        @Min(value = 1, message = "约束级别不能小于1")
        @Max(value = 10, message = "约束级别不能大于10")
        private Integer level;

        /**
         * 是否强制约束
         */
        @NotNull(message = "是否强制约束不能为空")
        private Boolean mandatory;

        /**
         * 违反约束时的处理方式
         */
        @Pattern(regexp = "^(IGNORE|WARN|ERROR|ADJUST|BLOCK)$",
                message = "处理方式必须是IGNORE、WARN、ERROR、ADJUST或BLOCK")
        private String violationAction;

        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled;
    }

    // ==================== 内部类：业务规则 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusinessRuleDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 规则ID
         */
        @NotBlank(message = "规则ID不能为空")
        private String ruleId;

        /**
         * 规则名称
         */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50, message = "规则名称不能超过50个字符")
        private String name;

        /**
         * 规则类型
         */
        @NotBlank(message = "规则类型不能为空")
        @Pattern(regexp = "^(TIMING|PRIORITY|FLOW|SAFETY|EFFICIENCY|CUSTOM)$",
                message = "规则类型必须是TIMING、PRIORITY、FLOW、SAFETY、EFFICIENCY或CUSTOM")
        private String type;

        /**
         * 规则描述
         */
        @Size(max = 200, message = "规则描述不能超过200个字符")
        private String description;

        /**
         * 触发条件
         */
        @NotBlank(message = "触发条件不能为空")
        private String triggerCondition;

        /**
         * 执行动作
         */
        @NotBlank(message = "执行动作不能为空")
        private String action;

        /**
         * 规则参数
         */
        private Map<String, Object> parameters;

        /**
         * 规则优先级
         */
        @Min(value = 1, message = "规则优先级不能小于1")
        @Max(value = 10, message = "规则优先级不能大于10")
        private Integer priority;

        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    // ==================== 验证方法 ====================

    /**
     * 自定义验证：上行和下行控制相位不能相同
     */
    @AssertTrue(message = "上行和下行控制相位不能相同")
    public boolean isPhaseDifferent() {
        if (upctrl == null || downctrl == null) {
            return true; // 让@NotNull注解处理空值验证
        }
        return !upctrl.equals(downctrl);
    }

    /**
     * 自定义验证：绿灯时间范围合理性
     */
    @AssertTrue(message = "最小绿灯时间必须小于等于最大绿灯时间")
    public boolean isGreenTimeRangeValid() {
        if (minGreenTime == null || maxGreenTime == null) {
            return true;
        }
        return minGreenTime <= maxGreenTime;
    }

    /**
     * 自定义验证：全红时间不能超过最大绿灯时间
     */
    @AssertTrue(message = "全红时间不应超过最大绿灯时间")
    public boolean isAllRedTimeReasonable() {
        if (allred == null || maxGreenTime == null) {
            return true;
        }
        return allred <= maxGreenTime;
    }

    /**
     * 自定义验证：路段长度与速度的合理性
     */
    @AssertTrue(message = "路段配置与速度设置不匹配")
    public boolean isLengthSpeedConsistent() {
        if (length == null || maxSpeed == null || recommendedSpeed == null) {
            return true;
        }

        // 推荐速度不应超过最大速度
        if (recommendedSpeed > maxSpeed) {
            return false;
        }

        // 对于短路段，速度不应过高
        if (length < 50 && maxSpeed > 30) {
            return false;
        }

        return true;
    }

    /**
     * 自定义验证：检测器配置的合理性
     */
    @AssertTrue(message = "检测器配置不合理")
    public boolean isDetectorConfigValid() {
        if (detectors == null || detectors.isEmpty()) {
            return true; // 允许没有检测器
        }

        // 检查检测器位置是否超出路段长度
        if (length != null) {
            for (DetectorConfigDTO detector : detectors) {
                if (detector.getDistanceFromStart() != null &&
                        detector.getDistanceFromStart() > length) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 自定义验证：时段配置的一致性
     */
    @AssertTrue(message = "时段配置存在时间重叠")
    public boolean isTimeSlotConfigValid() {
        if (timeSlotConfigs == null || timeSlotConfigs.size() <= 1) {
            return true;
        }

        // 检查同一天内时段是否重叠
        for (int i = 0; i < timeSlotConfigs.size(); i++) {
            for (int j = i + 1; j < timeSlotConfigs.size(); j++) {
                TimeSlotConfigDTO config1 = timeSlotConfigs.get(i);
                TimeSlotConfigDTO config2 = timeSlotConfigs.get(j);

                if (hasCommonWeekDay(config1.getWeekDays(), config2.getWeekDays())) {
                    if (isTimeOverlap(config1.getStartTime(), config1.getEndTime(),
                            config2.getStartTime(), config2.getEndTime())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查两个星期列表是否有重叠
     */
    private boolean hasCommonWeekDay(List<Integer> weekDays1, List<Integer> weekDays2) {
        if (weekDays1 == null || weekDays2 == null) {
            return false;
        }
        return weekDays1.stream().anyMatch(weekDays2::contains);
    }

    /**
     * 检查两个时间段是否重叠
     */
    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        try {
            int start1Min = timeToMinutes(start1);
            int end1Min = timeToMinutes(end1);
            int start2Min = timeToMinutes(start2);
            int end2Min = timeToMinutes(end2);

            // 处理跨日情况
            if (end1Min < start1Min) end1Min += 24 * 60;
            if (end2Min < start2Min) end2Min += 24 * 60;

            return !(end1Min <= start2Min || end2Min <= start1Min);
        } catch (Exception e) {
            return false; // 时间格式错误时认为不重叠
        }
    }

    /**
     * 将时间字符串转换为分钟数
     */
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /**
     * 获取路段完整显示名称
     */
    public String getFullDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        return name + " (" + sigid + ")";
    }

    /**
     * 检查路段是否为起始路段
     */
    public boolean isStartSegment() {
        return "START".equals(segmentType);
    }

    /**
     * 检查路段是否为结束路段
     */
    public boolean isEndSegment() {
        return "END".equals(segmentType);
    }

    /**
     * 检查路段是否为中间路段
     */
    public boolean isMiddleSegment() {
        return "MIDDLE".equals(segmentType);
    }

    /**
     * 检查路段是否为路口
     */
    public boolean isJunction() {
        return "JUNCTION".equals(segmentType);
    }

    /**
     * 检查是否启用了感应控制
     */
    public boolean hasInductiveControl() {
        return Boolean.TRUE.equals(inductiveControlEnabled) && inductiveControl != null;
    }

    /**
     * 检查是否配置了检测器
     */
    public boolean hasDetectors() {
        return detectors != null && !detectors.isEmpty();
    }

    /**
     * 检查是否配置了时段控制
     */
    public boolean hasTimeSlotControl() {
        return timeSlotConfigs != null && !timeSlotConfigs.isEmpty();
    }

    /**
     * 检查是否启用了夜间模式
     */
    public boolean hasNightMode() {
        return nightModeConfig != null && Boolean.TRUE.equals(nightModeConfig.getEnabled());
    }

    /**
     * 检查是否有GPS坐标
     */
    public boolean hasGpsCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * 获取路段状态描述
     */
    public String getStatusDescription() {
        if (!Boolean.TRUE.equals(enabled)) {
            return "已禁用";
        }

        return switch (status != null ? status : "UNKNOWN") {
            case "ACTIVE" -> "运行中";
            case "INACTIVE" -> "未激活";
            case "MAINTENANCE" -> "维护中";
            case "ERROR" -> "故障";
            default -> "未知状态";
        };
    }

    /**
     * 获取控制模式描述
     */
    public String getControlModeDescription() {
        return switch (controlMode != null ? controlMode : "AUTO") {
            case "AUTO" -> "自动控制";
            case "MANUAL" -> "手动控制";
            case "TIMED" -> "定时控制";
            case "INDUCTIVE" -> "感应控制";
            default -> "自动控制";
        };
    }

    /**
     * 检查配置是否完整
     */
    public boolean isConfigComplete() {
        return name != null && !name.trim().isEmpty()
                && sigid != null && !sigid.trim().isEmpty()
                && allred != null
                && upctrl != null
                && downctrl != null
                && inzone != null
                && outzone != null
                && segmentType != null && !segmentType.trim().isEmpty()
                && enabled != null;
    }

    /**
     * 获取配置完整性百分比
     */
    public int getConfigCompletenessPercentage() {
        int totalFields = 20; // 核心必填字段数量
        int filledFields = 0;

        if (name != null && !name.trim().isEmpty()) filledFields++;
        if (sigid != null && !sigid.trim().isEmpty()) filledFields++;
        if (allred != null) filledFields++;
        if (upctrl != null) filledFields++;
        if (downctrl != null) filledFields++;
        if (inzone != null) filledFields++;
        if (outzone != null) filledFields++;
        if (segmentType != null) filledFields++;
        if (enabled != null) filledFields++;
        if (length != null) filledFields++;
        if (maxSpeed != null) filledFields++;
        if (priority != null) filledFields++;
        if (minGreenTime != null) filledFields++;
        if (maxGreenTime != null) filledFields++;
        if (yellowTime != null) filledFields++;
        if (redClearTime != null) filledFields++;
        if (controlMode != null) filledFields++;
        if (status != null) filledFields++;
        if (latitude != null && longitude != null) filledFields++;
        if (description != null && !description.trim().isEmpty()) filledFields++;

        return (filledFields * 100) / totalFields;
    }

    /**
     * 创建路段摘要信息
     */
    public String createSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("路段: ").append(getFullDisplayName());

        if (segmentType != null) {
            summary.append(", 类型: ").append(segmentType);
        }

        if (length != null) {
            summary.append(", 长度: ").append(length).append("米");
        }

        if (allred != null) {
            summary.append(", 全红: ").append(allred).append("秒");
        }

        summary.append(", 相位: ").append(upctrl).append("/").append(downctrl);
        summary.append(", 会车区: ").append(inzone).append("→").append(outzone);

        return summary.toString();
    }
}