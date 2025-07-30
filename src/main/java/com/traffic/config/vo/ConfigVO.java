// ==================== 配置视图对象 ====================

/**
 * 单车道配置视图对象
 * 用于前端展示和交互的数据结构
 *
 * @author System
 * @version 1.0.0
 */
package com.traffic.config.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置基本信息
     */
    private BasicInfo basicInfo;

    /**
     * 全局配置
     */
    private GlobalConfigVO globalConfig;

    /**
     * 路段配置列表
     */
    private List<SegmentVO> segments;

    /**
     * 配置统计信息
     */
    private ConfigStatistics statistics;

    /**
     * 配置验证结果
     */
    private ValidationResult validationResult;

    // ==================== 内部类：基本信息 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BasicInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 配置文件路径
         */
        private String filePath;

        /**
         * 配置文件名
         */
        private String fileName;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 文件大小（可读格式）
         */
        private String fileSizeReadable;

        /**
         * 最后修改时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastModified;

        /**
         * 配置版本号
         */
        private Long version;

        /**
         * 文件MD5值
         */
        private String md5;

        /**
         * 是否存在
         */
        private Boolean exists;

        /**
         * 是否可读
         */
        private Boolean readable;

        /**
         * 是否可写
         */
        private Boolean writable;
    }

    // ==================== 内部类：全局配置视图对象 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GlobalConfigVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 全红时间（秒）
         */
        private Integer allRed;

        /**
         * 最大全红时间（秒）
         */
        private Integer maxAllRed;

        /**
         * 全红时间可读格式
         */
        private String allRedReadable;

        /**
         * 最大全红时间可读格式
         */
        private String maxAllRedReadable;

        /**
         * 配置状态
         */
        private String status;

        /**
         * 是否有效
         */
        private Boolean valid;

        /**
         * 验证消息
         */
        private String validationMessage;

        /**
         * 建议的全红时间范围
         */
        private Range recommendedAllRedRange;

        /**
         * 建议的最大全红时间范围
         */
        private Range recommendedMaxAllRedRange;
    }

    // ==================== 内部类：路段配置视图对象 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 路段名称
         */
        private String name;

        /**
         * 信号灯ID
         */
        private String sigid;

        /**
         * 全红时间（秒）
         */
        private Integer allred;

        /**
         * 上行控制相位
         */
        private Integer upctrl;

        /**
         * 下行控制相位
         */
        private Integer downctrl;

        /**
         * 进入会车区编号
         */
        private Integer inzone;

        /**
         * 离开会车区编号
         */
        private Integer outzone;

        /**
         * 路段显示名称（用于前端显示）
         */
        private String displayName;

        /**
         * 路段描述
         */
        private String description;

        /**
         * 路段状态
         */
        private String status;

        /**
         * 是否有效
         */
        private Boolean valid;

        /**
         * 验证消息列表
         */
        private List<String> validationMessages;

        /**
         * 路段类型（起始/中间/结束）
         */
        private String segmentType;

        /**
         * 在配置中的位置索引
         */
        private Integer index;

        /**
         * 相邻路段信息
         */
        private AdjacentSegments adjacentSegments;

        /**
         * 控制相位信息
         */
        private PhaseInfo phaseInfo;

        /**
         * 会车区信息
         */
        private ZoneInfo zoneInfo;

        /**
         * 统计信息
         */
        private SegmentStatistics statistics;
    }

    // ==================== 内部类：相邻路段信息 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdjacentSegments implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 前一个路段
         */
        private SegmentRef previous;

        /**
         * 后一个路段
         */
        private SegmentRef next;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentRef implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 路段名称
         */
        private String name;

        /**
         * 信号灯ID
         */
        private String sigid;

        /**
         * 路段索引
         */
        private Integer index;
    }

    // ==================== 内部类：相位信息 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PhaseInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 上行相位详情
         */
        private PhaseDetail upPhase;

        /**
         * 下行相位详情
         */
        private PhaseDetail downPhase;

        /**
         * 相位冲突检查结果
         */
        private Boolean hasConflict;

        /**
         * 冲突描述
         */
        private String conflictDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PhaseDetail implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 相位编号
         */
        private Integer phaseNumber;

        /**
         * 相位名称
         */
        private String phaseName;

        /**
         * 相位描述
         */
        private String description;

        /**
         * 是否有效
         */
        private Boolean valid;
    }

    // ==================== 内部类：会车区信息 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ZoneInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 进入会车区详情
         */
        private ZoneDetail inZone;

        /**
         * 离开会车区详情
         */
        private ZoneDetail outZone;

        /**
         * 会车区路径描述
         */
        private String pathDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ZoneDetail implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 会车区编号
         */
        private Integer zoneNumber;

        /**
         * 会车区名称
         */
        private String zoneName;

        /**
         * 会车区容量
         */
        private Integer capacity;

        /**
         * 会车区类型
         */
        private String zoneType;

        /**
         * 是否有效
         */
        private Boolean valid;
    }

    // ==================== 内部类：配置统计信息 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConfigStatistics implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 路段总数
         */
        private Integer totalSegments;

        /**
         * 有效路段数
         */
        private Integer validSegments;

        /**
         * 无效路段数
         */
        private Integer invalidSegments;

        /**
         * 会车区总数
         */
        private Integer totalZones;

        /**
         * 使用的相位数
         */
        private Integer usedPhases;

        /**
         * 平均全红时间
         */
        private Double averageAllRed;

        /**
         * 最小全红时间
         */
        private Integer minAllRed;

        /**
         * 最大全红时间
         */
        private Integer maxAllRed;

        /**
         * 配置复杂度评分（1-10）
         */
        private Integer complexityScore;

        /**
         * 配置健康度评分（1-100）
         */
        private Integer healthScore;

        /**
         * 最后更新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastUpdated;

        /**
         * 相位分布统计
         */
        private Map<Integer, Integer> phaseDistribution;

        /**
         * 会车区使用统计
         */
        private Map<Integer, Integer> zoneUsage;
    }

    // ==================== 内部类：路段统计信息 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SegmentStatistics implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 通行效率评分（1-100）
         */
        private Integer efficiencyScore;

        /**
         * 安全性评分（1-100）
         */
        private Integer safetyScore;

        /**
         * 配置合理性评分（1-100）
         */
        private Integer reasonabilityScore;

        /**
         * 建议优化项
         */
        private List<String> optimizationSuggestions;

        /**
         * 关键性级别（高/中/低）
         */
        private String criticalLevel;

        /**
         * 依赖路段数量
         */
        private Integer dependencyCount;
    }

    // ==================== 内部类：验证结果 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 整体验证是否通过
         */
        private Boolean valid;

        /**
         * 验证等级（ERROR/WARNING/INFO）
         */
        private String level;

        /**
         * 验证摘要
         */
        private String summary;

        /**
         * 错误数量
         */
        private Integer errorCount;

        /**
         * 警告数量
         */
        private Integer warningCount;

        /**
         * 信息数量
         */
        private Integer infoCount;

        /**
         * 详细验证结果
         */
        private List<ValidationItem> items;

        /**
         * 验证时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime validatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 验证项类型
         */
        private String type;

        /**
         * 验证等级
         */
        private String level;

        /**
         * 验证消息
         */
        private String message;

        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 字段值
         */
        private Object fieldValue;

        /**
         * 错误代码
         */
        private String errorCode;

        /**
         * 建议修复方案
         */
        private String suggestion;

        /**
         * 关联的路段ID（如果适用）
         */
        private String relatedSegmentId;

        /**
         * 验证规则描述
         */
        private String ruleDescription;
    }

    // ==================== 内部类：数值范围 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Range implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 最小值
         */
        private Integer min;

        /**
         * 最大值
         */
        private Integer max;

        /**
         * 推荐值
         */
        private Integer recommended;

        /**
         * 单位
         */
        private String unit;

        /**
         * 描述
         */
        private String description;
    }

    // ==================== 内部类：操作历史 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationHistory implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 操作ID
         */
        private String operationId;

        /**
         * 操作类型（CREATE/UPDATE/DELETE）
         */
        private String operationType;

        /**
         * 操作描述
         */
        private String description;

        /**
         * 操作者
         */
        private String operator;

        /**
         * 操作时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operatedAt;

        /**
         * 变更前的值
         */
        private Object beforeValue;

        /**
         * 变更后的值
         */
        private Object afterValue;

        /**
         * 操作结果
         */
        private String result;

        /**
         * 备注
         */
        private String remarks;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取配置健康状态
     *
     * @return 健康状态描述
     */
    public String getHealthStatus() {
        if (statistics == null) {
            return "未知";
        }

        Integer healthScore = statistics.getHealthScore();
        if (healthScore == null) {
            return "未评估";
        }

        if (healthScore >= 90) {
            return "优秀";
        } else if (healthScore >= 70) {
            return "良好";
        } else if (healthScore >= 50) {
            return "一般";
        } else {
            return "需要优化";
        }
    }

    /**
     * 获取配置复杂度描述
     *
     * @return 复杂度描述
     */
    public String getComplexityDescription() {
        if (statistics == null || statistics.getComplexityScore() == null) {
            return "未知";
        }

        Integer score = statistics.getComplexityScore();
        if (score <= 3) {
            return "简单";
        } else if (score <= 6) {
            return "中等";
        } else if (score <= 8) {
            return "复杂";
        } else {
            return "非常复杂";
        }
    }

    /**
     * 检查是否有验证错误
     *
     * @return 是否有错误
     */
    public boolean hasValidationErrors() {
        return validationResult != null
                && validationResult.getErrorCount() != null
                && validationResult.getErrorCount() > 0;
    }

    /**
     * 检查是否有验证警告
     *
     * @return 是否有警告
     */
    public boolean hasValidationWarnings() {
        return validationResult != null
                && validationResult.getWarningCount() != null
                && validationResult.getWarningCount() > 0;
    }

    /**
     * 获取总的验证问题数量
     *
     * @return 问题总数
     */
    public int getTotalValidationIssues() {
        if (validationResult == null) {
            return 0;
        }

        int errors = validationResult.getErrorCount() != null ? validationResult.getErrorCount() : 0;
        int warnings = validationResult.getWarningCount() != null ? validationResult.getWarningCount() : 0;

        return errors + warnings;
    }

    /**
     * 检查配置是否可用
     *
     * @return 是否可用
     */
    public boolean isConfigUsable() {
        return validationResult != null
                && Boolean.TRUE.equals(validationResult.getValid())
                && !hasValidationErrors();
    }
}