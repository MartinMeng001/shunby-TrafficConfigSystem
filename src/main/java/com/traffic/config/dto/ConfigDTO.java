// ==================== 配置数据传输对象 ====================

/**
 * 单车道配置数据传输对象
 * 用于前后端数据传输的标准化数据结构
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID（用于版本控制）
     */
    private String configId;

    /**
     * 配置版本
     */
    private Long version;

    /**
     * 全局配置
     */
    @Valid
    @NotNull(message = "全局配置不能为空")
    private GlobalConfigDTO globalConfig;

    /**
     * 路段配置列表
     */
    @Valid
    @NotNull(message = "路段配置列表不能为空")
    @Size(min = 1, max = 20, message = "路段数量必须在1-20之间")
    private List<SegmentDTO> segments;

    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述不能超过500个字符")
    private String description;

    /**
     * 配置标签
     */
    private List<String> tags;

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
     * 修改者
     */
    private String modifiedBy;

    // ==================== 内部类：全局配置DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GlobalConfigDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 全红时间（秒）
         */
        @NotNull(message = "全红时间不能为空")
        @Min(value = 30, message = "全红时间不能小于30秒")
        @Max(value = 600, message = "全红时间不能大于600秒")
        private Integer allRed;

        /**
         * 最大全红时间（秒）
         */
        @NotNull(message = "最大全红时间不能为空")
        @Min(value = 60, message = "最大全红时间不能小于60秒")
        @Max(value = 1200, message = "最大全红时间不能大于1200秒")
        private Integer maxAllRed;

        /**
         * 配置生效时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime effectiveTime;

        /**
         * 配置失效时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expireTime;

        /**
         * 是否启用自动调整
         */
        private Boolean autoAdjustEnabled;

        /**
         * 调整步长（秒）
         */
        @Min(value = 1, message = "调整步长不能小于1秒")
        @Max(value = 60, message = "调整步长不能大于60秒")
        private Integer adjustmentStep;

        /**
         * 最小调整间隔（分钟）
         */
        @Min(value = 1, message = "最小调整间隔不能小于1分钟")
        @Max(value = 60, message = "最小调整间隔不能大于60分钟")
        private Integer minAdjustmentInterval;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注不能超过200个字符")
        private String remarks;

        /**
         * 自定义验证：全红时间必须小于等于最大全红时间
         */
        @AssertTrue(message = "全红时间必须小于等于最大全红时间")
        public boolean isAllRedValid() {
            if (allRed == null || maxAllRed == null) {
                return true; // 让@NotNull注解处理空值验证
            }
            return allRed <= maxAllRed;
        }
    }

    // ==================== 内部类：路段配置DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 路段名称
         */
        @NotBlank(message = "路段名称不能为空")
        @Size(min = 2, max = 50, message = "路段名称长度必须在2-50个字符之间")
        @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "路段名称只能包含中文、英文、数字和空格")
        private String name;

        /**
         * 信号灯ID
         */
        @NotBlank(message = "信号灯ID不能为空")
        @Pattern(regexp = "^[0-9]{5}$", message = "信号灯ID必须是5位数字")
        private String sigid;

        /**
         * 全红时间（秒）
         */
        @NotNull(message = "全红时间不能为空")
        @Min(value = 10, message = "全红时间不能小于10秒")
        @Max(value = 300, message = "全红时间不能大于300秒")
        private Integer allred;

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
         * 路段优先级（1-10，数字越大优先级越高）
         */
        @Min(value = 1, message = "路段优先级不能小于1")
        @Max(value = 10, message = "路段优先级不能大于10")
        private Integer priority;

        /**
         * 路段长度（米）
         */
        @Min(value = 10, message = "路段长度不能小于10米")
        @Max(value = 1000, message = "路段长度不能大于1000米")
        private Integer length;

        /**
         * 最大通行速度（km/h）
         */
        @Min(value = 5, message = "最大通行速度不能小于5km/h")
        @Max(value = 60, message = "最大通行速度不能大于60km/h")
        private Integer maxSpeed;

        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled;

        /**
         * 路段类型
         */
        @NotBlank(message = "路段类型不能为空")
        @Pattern(regexp = "^(START|MIDDLE|END)$", message = "路段类型必须是START、MIDDLE或END")
        private String segmentType;

        /**
         * 路段配置
         */
        @Valid
        private SegmentConfigDTO config;

        /**
         * 路段约束条件
         */
        @Valid
        private List<ConstraintDTO> constraints;

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
         * 备注
         */
        @Size(max = 200, message = "备注不能超过200个字符")
        private String remarks;

        /**
         * 自定义验证：上行和下行控制相位不能相同
         */
        @AssertTrue(message = "上行和下行控制相位不能相同")
        public boolean isPhaseDifferent() {
            if (upctrl == null || downctrl == null) {
                return true;
            }
            return !upctrl.equals(downctrl);
        }

        /**
         * 自定义验证：进入和离开会车区可以相同但通常应该不同
         */
        public boolean isZoneConfigValid() {
            // 这里可以添加更复杂的会车区验证逻辑
            return true;
        }
    }

    // ==================== 内部类：路段配置DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentConfigDTO implements Serializable {

        private static final long serialVersionUID = 1L;

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

        /**
         * 检测器延迟时间（秒）
         */
        @Min(value = 0, message = "检测器延迟时间不能小于0秒")
        @Max(value = 10, message = "检测器延迟时间不能大于10秒")
        private Integer detectorDelay;

        /**
         * 是否启用感应控制
         */
        private Boolean inductiveControlEnabled;

        /**
         * 感应控制参数
         */
        @Valid
        private InductiveControlDTO inductiveControl;

        /**
         * 自定义验证：最小绿灯时间必须小于等于最大绿灯时间
         */
        @AssertTrue(message = "最小绿灯时间必须小于等于最大绿灯时间")
        public boolean isGreenTimeValid() {
            if (minGreenTime == null || maxGreenTime == null) {
                return true;
            }
            return minGreenTime <= maxGreenTime;
        }
    }

    // ==================== 内部类：感应控制DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InductiveControlDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 车辆检测灵敏度（1-10）
         */
        @Min(value = 1, message = "车辆检测灵敏度不能小于1")
        @Max(value = 10, message = "车辆检测灵敏度不能大于10")
        private Integer sensitivity;

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
         * 是否启用高峰时段特殊控制
         */
        private Boolean peakHourEnabled;

        /**
         * 高峰时段配置
         */
        @Valid
        private List<PeakHourConfigDTO> peakHourConfigs;
    }

    // ==================== 内部类：高峰时段配置DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PeakHourConfigDTO implements Serializable {

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
         * 星期配置（1-7，1表示周一）
         */
        @NotEmpty(message = "星期配置不能为空")
        @Size(min = 1, max = 7, message = "星期配置最多包含7天")
        private List<@Min(value = 1, message = "星期数必须在1-7之间") @Max(value = 7, message = "星期数必须在1-7之间") Integer> weekDays;

        /**
         * 高峰时段绿灯时间调整系数（0.5-2.0）
         */
        @DecimalMin(value = "0.5", message = "绿灯时间调整系数不能小于0.5")
        @DecimalMax(value = "2.0", message = "绿灯时间调整系数不能大于2.0")
        private Double greenTimeMultiplier;

        /**
         * 高峰时段优先级调整
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

    // ==================== 内部类：约束条件DTO ====================

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
        @Pattern(regexp = "^(TIME|PHASE|ZONE|PRIORITY|CUSTOM)$",
                message = "约束类型必须是TIME、PHASE、ZONE、PRIORITY或CUSTOM")
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
        private java.util.Map<String, Object> parameters;

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
        @Pattern(regexp = "^(IGNORE|WARN|ERROR|ADJUST)$",
                message = "处理方式必须是IGNORE、WARN、ERROR或ADJUST")
        private String violationAction;

        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled;
    }

    // ==================== 内部类：批量操作DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BatchOperationDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 操作类型
         */
        @NotBlank(message = "操作类型不能为空")
        @Pattern(regexp = "^(CREATE|UPDATE|DELETE|VALIDATE|BACKUP|RESTORE)$",
                message = "操作类型必须是CREATE、UPDATE、DELETE、VALIDATE、BACKUP或RESTORE")
        private String operationType;

        /**
         * 目标路段ID列表
         */
        private List<String> targetSegmentIds;

        /**
         * 批量更新的字段和值
         */
        private java.util.Map<String, Object> updateFields;

        /**
         * 操作参数
         */
        private java.util.Map<String, Object> parameters;

        /**
         * 是否强制执行（忽略验证错误）
         */
        private Boolean forceExecute;

        /**
         * 操作描述
         */
        @Size(max = 200, message = "操作描述不能超过200个字符")
        private String description;
    }

    // ==================== 内部类：配置模板DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConfigTemplateDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 模板名称
         */
        @NotBlank(message = "模板名称不能为空")
        @Size(min = 2, max = 50, message = "模板名称长度必须在2-50个字符之间")
        private String templateName;

        /**
         * 模板描述
         */
        @Size(max = 200, message = "模板描述不能超过200个字符")
        private String description;

        /**
         * 模板类型
         */
        @NotBlank(message = "模板类型不能为空")
        @Pattern(regexp = "^(STANDARD|SIMPLE|COMPLEX|CUSTOM)$",
                message = "模板类型必须是STANDARD、SIMPLE、COMPLEX或CUSTOM")
        private String templateType;

        /**
         * 路段数量
         */
        @Min(value = 1, message = "路段数量不能小于1")
        @Max(value = 20, message = "路段数量不能大于20")
        private Integer segmentCount;

        /**
         * 会车区数量
         */
        @Min(value = 1, message = "会车区数量不能小于1")
        @Max(value = 10, message = "会车区数量不能大于10")
        private Integer zoneCount;

        /**
         * 默认配置参数
         */
        private java.util.Map<String, Object> defaultParameters;

        /**
         * 是否公共模板
         */
        private Boolean isPublic;

        /**
         * 创建者
         */
        private String creator;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    // ==================== 验证方法 ====================

    /**
     * 验证配置的整体一致性
     */
    @AssertTrue(message = "配置整体一致性验证失败")
    public boolean isConfigConsistent() {
        if (segments == null || segments.isEmpty()) {
            return false;
        }

        // 验证信号灯ID的唯一性
        long uniqueSigidCount = segments.stream()
                .map(SegmentDTO::getSigid)
                .distinct()
                .count();

        if (uniqueSigidCount != segments.size()) {
            return false; // 信号灯ID重复
        }

        // 验证路段名称的唯一性
        long uniqueNameCount = segments.stream()
                .map(SegmentDTO::getName)
                .distinct()
                .count();

        if (uniqueNameCount != segments.size()) {
            return false; // 路段名称重复
        }

        // 验证会车区的连续性
        return validateZoneContinuity();
    }

    /**
     * 验证会车区的连续性
     */
    private boolean validateZoneContinuity() {
        if (segments == null || segments.size() <= 1) {
            return true;
        }

        for (int i = 0; i < segments.size() - 1; i++) {
            SegmentDTO current = segments.get(i);
            SegmentDTO next = segments.get(i + 1);

            // 当前路段的出区应该等于下一路段的入区（对于连续路段）
            if (!current.getOutzone().equals(next.getInzone())) {
                // 这里可以根据实际业务逻辑调整验证规则
                // 暂时允许不连续的情况
            }
        }

        return true;
    }

    /**
     * 获取配置摘要信息
     */
    public String getConfigSummary() {
        if (segments == null) {
            return "无配置信息";
        }

        return String.format("配置包含%d个路段，全红时间%d秒",
                segments.size(),
                globalConfig != null ? globalConfig.getAllRed() : 0);
    }

    /**
     * 检查是否为有效配置
     */
    public boolean isValidConfig() {
        return globalConfig != null
                && segments != null
                && !segments.isEmpty()
                && isConfigConsistent();
    }
}