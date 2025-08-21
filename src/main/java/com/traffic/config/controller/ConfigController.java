package com.traffic.config.controller;

import com.traffic.config.entity.DetectPoint;
import com.traffic.config.entity.GlobalConfig;
import com.traffic.config.entity.Segment;
import com.traffic.config.entity.SingleLane;
import com.traffic.config.entity.WaitingArea;
import com.traffic.config.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 配置管理控制器
 * 实现配置约束：
 * 1. 所有参数只能修改，不能删除，也不能增加
 * 2. 检测点参数只读，不允许增加，也不允许修改
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigService configService;

    // ==================== 查询接口 ====================

    /**
     * 获取完整配置
     */
    @GetMapping("/full")
    public ResponseEntity<SingleLane> getFullConfig() {
        try {
            SingleLane config = configService.loadConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("获取完整配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新等待区配置（仅允许修改现有等待区的容量参数）
     */
    @PutMapping("/waitingareas/{index}")
    public ResponseEntity<Map<String, Object>> updateWaitingArea(@PathVariable int index,
                                                                 @RequestBody WaitingArea waitingArea) {
        try {
            // 检查等待区是否存在
            SingleLane config = configService.loadConfig();
            Optional<WaitingArea> existingWaitingArea = config.getWaitingAreas().getWaitingAreas().stream()
                    .filter(wa -> wa.getIndex() == index)
                    .findFirst();

            if (!existingWaitingArea.isPresent()) {
                return createErrorResponse("等待区不存在: " + index, HttpStatus.NOT_FOUND);
            }

            // 验证传入的配置是否只修改允许的参数
            ResponseEntity<Map<String, Object>> validationResult = validateWaitingAreaUpdate(index, waitingArea, existingWaitingArea.get());
            if (validationResult != null) {
                return validationResult;
            }

            // 更新等待区配置
            for (WaitingArea wa : config.getWaitingAreas().getWaitingAreas()) {
                if (wa.getIndex() == index) {
                    wa.setUpCapacity(waitingArea.getUpCapacity());
                    wa.setDownCapacity(waitingArea.getDownCapacity());
                    break;
                }
            }

            configService.saveConfig(config);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "等待区配置更新成功");
            response.put("data", waitingArea);

            logger.info("等待区配置更新成功: index={}", index);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新等待区配置失败: index={}", index, e);
            return createErrorResponse("更新失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取全局配置
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalConfig> getGlobalConfig() {
        try {
            GlobalConfig global = configService.getGlobalConfig();
            return ResponseEntity.ok(global);
        } catch (Exception e) {
            logger.error("获取全局配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有路段配置
     */
    @GetMapping("/segments")
    public ResponseEntity<List<Segment>> getAllSegments() {
        try {
            List<Segment> segments = configService.getAllSegments();
            return ResponseEntity.ok(segments);
        } catch (Exception e) {
            logger.error("获取路段配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据信号灯ID获取路段配置
     */
    @GetMapping("/segments/{sigid}")
    public ResponseEntity<Segment> getSegmentBySigid(@PathVariable String sigid) {
        try {
            Optional<Segment> segment = configService.getSegmentBySigid(sigid);
            return segment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("根据信号灯ID获取路段配置失败: {}", sigid, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据名称获取路段配置
     */
    @GetMapping("/segments/byname/{name}")
    public ResponseEntity<Segment> getSegmentByName(@PathVariable String name) {
        try {
            Optional<Segment> segment = configService.getSegmentByName(name);
            return segment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("根据名称获取路段配置失败: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有检测点配置（只读）
     */
    @GetMapping("/detectpoints")
    public ResponseEntity<List<DetectPoint>> getAllDetectPoints() {
        try {
            List<DetectPoint> detectPoints = configService.getAllDetectPoints();
            return ResponseEntity.ok(detectPoints);
        } catch (Exception e) {
            logger.error("获取检测点配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有等待区配置
     */
    @GetMapping("/waitingareas")
    public ResponseEntity<List<WaitingArea>> getAllWaitingAreas() {
        try {
            SingleLane config = configService.loadConfig();
            List<WaitingArea> waitingAreas = config.getWaitingAreas().getWaitingAreas();
            return ResponseEntity.ok(waitingAreas);
        } catch (Exception e) {
            logger.error("获取等待区配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据索引获取等待区配置
     */
    @GetMapping("/waitingareas/{index}")
    public ResponseEntity<WaitingArea> getWaitingAreaByIndex(@PathVariable int index) {
        try {
            SingleLane config = configService.loadConfig();
            Optional<WaitingArea> waitingArea = config.getWaitingAreas().getWaitingAreas().stream()
                    .filter(wa -> wa.getIndex() == index)
                    .findFirst();
            return waitingArea.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("根据索引获取等待区配置失败: {}", index, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== 修改接口（仅允许更新现有参数）====================

    /**
     * 更新全局配置（仅允许修改现有参数）
     */
    @PutMapping("/global")
    public ResponseEntity<Map<String, Object>> updateGlobalConfig(@RequestBody GlobalConfig globalConfig) {
        try {
            // 验证传入的配置是否只修改允许的参数
            ResponseEntity<Map<String, Object>> validationResult = validateGlobalConfigUpdate(globalConfig);
            if (validationResult != null) {
                return validationResult;
            }

            configService.updateGlobalConfig(globalConfig.getAllRed(), globalConfig.getMaxAllRed());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "全局配置更新成功");
            response.put("data", globalConfig);

            logger.info("全局配置更新成功: AllRed={}, MaxAllRed={}",
                    globalConfig.getAllRed(), globalConfig.getMaxAllRed());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新全局配置失败", e);
            return createErrorResponse("更新失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新路段配置（仅允许修改现有路段的参数）
     */
    @PutMapping("/segments/{sigid}")
    public ResponseEntity<Map<String, Object>> updateSegment(@PathVariable String sigid, @RequestBody Segment
            segment) {
        try {
            // 检查路段是否存在
            Optional<Segment> existingSegment = configService.getSegmentBySigid(sigid);
            if (!existingSegment.isPresent()) {
                return createErrorResponse("路段不存在: " + sigid, HttpStatus.NOT_FOUND);
            }

            // 验证传入的配置是否只修改允许的参数
            ResponseEntity<Map<String, Object>> validationResult = validateSegmentUpdate(sigid, segment, existingSegment.get());
            if (validationResult != null) {
                return validationResult;
            }

            // 确保请求体中的sigid与路径参数一致
            segment.setUpsigid(sigid);

            boolean updated = configService.updateSegment(sigid, segment);
            if (updated) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "路段配置更新成功");
                response.put("data", segment);

                logger.info("路段配置更新成功: sigid={}", sigid);
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("路段更新失败", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("更新路段配置失败: sigid={}", sigid, e);
            return createErrorResponse("更新失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 禁用的操作接口 ====================

    /**
     * 禁止添加路段配置
     */
    @PostMapping("/segments")
    public ResponseEntity<Map<String, Object>> addSegment(@RequestBody Segment segment) {
        return createErrorResponse("不允许添加新的路段配置，只能修改现有路段参数", HttpStatus.FORBIDDEN);
    }

    /**
     * 禁止删除路段配置
     */
    @DeleteMapping("/segments/{sigid}")
    public ResponseEntity<Map<String, Object>> deleteSegment(@PathVariable String sigid) {
        return createErrorResponse("不允许删除路段配置，只能修改现有路段参数", HttpStatus.FORBIDDEN);
    }

    /**
     * 禁止添加检测点配置
     */
    @PostMapping("/detectpoints")
    public ResponseEntity<Map<String, Object>> addDetectPoint(@RequestBody DetectPoint detectPoint) {
        return createErrorResponse("检测点配置为只读，不允许添加", HttpStatus.FORBIDDEN);
    }

    /**
     * 禁止修改检测点配置
     */
    @PutMapping("/detectpoints/{index}")
    public ResponseEntity<Map<String, Object>> updateDetectPoint(@PathVariable int index,
                                                                 @RequestBody DetectPoint detectPoint) {
        return createErrorResponse("检测点配置为只读，不允许修改", HttpStatus.FORBIDDEN);
    }

    /**
     * 禁止添加等待区配置
     */
    @PostMapping("/waitingareas")
    public ResponseEntity<Map<String, Object>> addWaitingArea(@RequestBody WaitingArea waitingArea) {
        return createErrorResponse("不允许添加新的等待区配置，只能修改现有等待区参数", HttpStatus.FORBIDDEN);
    }

    /**
     * 禁止删除等待区配置
     */
    @DeleteMapping("/waitingareas/{index}")
    public ResponseEntity<Map<String, Object>> deleteWaitingArea(@PathVariable int index) {
        return createErrorResponse("不允许删除等待区配置，只能修改现有等待区参数", HttpStatus.FORBIDDEN);
    }

    // ==================== 工具接口 ====================

    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        try {
            configService.refreshCache();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "配置缓存刷新成功");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("配置缓存刷新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("配置缓存刷新失败", e);
            return createErrorResponse("刷新失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            SingleLane config = configService.loadConfig();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "配置服务正常");
            response.put("configExists", config != null);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("配置服务健康检查失败", e);
            return createErrorResponse("配置服务异常: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取配置约束说明
     */
    @GetMapping("/constraints")
    public ResponseEntity<Map<String, Object>> getConfigConstraints() {
        Map<String, Object> constraints = new HashMap<>();

        Map<String, String> rules = new HashMap<>();
        rules.put("全局配置", "只允许修改AllRed和MaxAllRed参数，不允许添加或删除参数");
        rules.put("路段配置", "只允许修改现有路段的参数，不允许添加或删除路段");
        rules.put("检测点配置", "完全只读，不允许任何修改、添加或删除操作");
        rules.put("信号控制器列表", "只读，不允许修改");
        rules.put("等待区配置", "只允许修改现有等待区的容量参数，不允许添加或删除等待区");

        Map<String, List<String>> allowedOperations = new HashMap<>();
        allowedOperations.put("GET", Arrays.asList("/full", "/global", "/segments", "/segments/{sigid}", "/detectpoints", "/detectpoints/{index}", "/waitingareas", "/waitingareas/{index}"));
        allowedOperations.put("PUT", Arrays.asList("/global", "/segments/{sigid}", "/waitingareas/{index}"));
        allowedOperations.put("POST", Arrays.asList("/refresh"));
        allowedOperations.put("禁止操作", Arrays.asList("POST /segments", "DELETE /segments/{sigid}", "POST /detectpoints", "PUT /detectpoints/{index}", "DELETE /detectpoints/{index}", "POST /waitingareas", "DELETE /waitingareas/{index}"));

        constraints.put("rules", rules);
        constraints.put("allowedOperations", allowedOperations);
        constraints.put("version", "1.0");
        constraints.put("lastUpdated", System.currentTimeMillis());

        return ResponseEntity.ok(constraints);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证全局配置更新
     */
    private ResponseEntity<Map<String, Object>> validateGlobalConfigUpdate(GlobalConfig globalConfig) {
        // 1. 基本空值检查
        if (globalConfig == null) {
            return createErrorResponse("全局配置不能为空", HttpStatus.BAD_REQUEST);
        }

        // 2. AllRed参数验证
        if (globalConfig.getAllRed() <= 0) {
            return createErrorResponse("AllRed参数必须大于0", HttpStatus.BAD_REQUEST);
        }

        // AllRed参数合理范围检查（1-600秒）
        if (globalConfig.getAllRed() > 600) {
            return createErrorResponse("AllRed参数不能超过600秒", HttpStatus.BAD_REQUEST);
        }

        // 3. MaxAllRed参数验证
        if (globalConfig.getMaxAllRed() <= 0) {
            return createErrorResponse("MaxAllRed参数必须大于0", HttpStatus.BAD_REQUEST);
        }

        // MaxAllRed参数合理范围检查（1-1200秒）
        if (globalConfig.getMaxAllRed() > 1200) {
            return createErrorResponse("MaxAllRed参数不能超过1200秒", HttpStatus.BAD_REQUEST);
        }

        // 4. 参数逻辑关系验证
        if (globalConfig.getMaxAllRed() < globalConfig.getAllRed()) {
            return createErrorResponse("MaxAllRed不能小于AllRed", HttpStatus.BAD_REQUEST);
        }

        // 5. 可选：检查是否有不允许修改的字段被传入
        // 由于GlobalConfig可能包含其他只读字段，这里可以加入检查
        try {
            // 获取当前的全局配置，比较是否有不应该修改的字段
            GlobalConfig currentConfig = configService.getGlobalConfig();

            // 检查PlatformUrl是否被尝试修改（如果GlobalConfig包含此字段）
            // 这里假设GlobalConfig可能包含PlatformUrl字段，但我们不允许通过此接口修改
            // 实际实现时需要根据GlobalConfig的具体字段来调整

            // 如果有其他只读字段，可以在这里添加检查
            // 例如：
            // if (!Objects.equals(currentConfig.getPlatformUrl(), globalConfig.getPlatformUrl())) {
            //     return createErrorResponse("不允许修改PlatformUrl字段", HttpStatus.BAD_REQUEST);
            // }

        } catch (Exception e) {
            logger.warn("获取当前配置进行比较时出错: {}", e.getMessage());
            // 如果获取当前配置失败，继续处理，不阻塞更新操作
        }

        // 6. 参数组合有效性检查
        // 检查参数组合是否在系统可接受范围内
        int timeDifference = globalConfig.getMaxAllRed() - globalConfig.getAllRed();
        if (timeDifference > 300) {
            return createErrorResponse("MaxAllRed与AllRed的差值不应超过300秒", HttpStatus.BAD_REQUEST);
        }

        // 验证通过
        return null;
    }

    /**
     * 验证等待区配置更新
     */
    private ResponseEntity<Map<String, Object>> validateWaitingAreaUpdate(int index, WaitingArea
            newWaitingArea, WaitingArea existingWaitingArea) {
        if (newWaitingArea == null) {
            return createErrorResponse("等待区配置不能为空", HttpStatus.BAD_REQUEST);
        }

        // 确保不能修改索引
        if (newWaitingArea.getIndex() != existingWaitingArea.getIndex()) {
            return createErrorResponse("不允许修改等待区索引", HttpStatus.BAD_REQUEST);
        }

        if (newWaitingArea.getIndex() != index) {
            return createErrorResponse("路径参数索引与配置中的索引不匹配", HttpStatus.BAD_REQUEST);
        }

        // 验证容量参数
        if (newWaitingArea.getUpCapacity() <= 0) {
            return createErrorResponse("上行容量必须大于0", HttpStatus.BAD_REQUEST);
        }

        if (newWaitingArea.getDownCapacity() <= 0) {
            return createErrorResponse("下行容量必须大于0", HttpStatus.BAD_REQUEST);
        }

        // 设置合理的容量上限
        if (newWaitingArea.getUpCapacity() > 100) {
            return createErrorResponse("上行容量不能超过100", HttpStatus.BAD_REQUEST);
        }

        if (newWaitingArea.getDownCapacity() > 100) {
            return createErrorResponse("下行容量不能超过100", HttpStatus.BAD_REQUEST);
        }

        return null; // 验证通过
    }

    /**
     * 验证路段配置更新
     */
    private ResponseEntity<Map<String, Object>> validateSegmentUpdate(String sigid, Segment newSegment, Segment
            existingSegment) {
        if (newSegment == null) {
            return createErrorResponse("路段配置不能为空", HttpStatus.BAD_REQUEST);
        }

        // 确保不能修改关键标识字段
        if (newSegment.getSegmentId() != existingSegment.getSegmentId()) {
            return createErrorResponse("不允许修改路段ID", HttpStatus.BAD_REQUEST);
        }

        if (!sigid.equals(newSegment.getUpsigid())) {
            return createErrorResponse("不允许修改信号灯ID", HttpStatus.BAD_REQUEST);
        }

        // 验证参数范围
        if (newSegment.getLength() <= 0) {
            return createErrorResponse("路段长度必须大于0", HttpStatus.BAD_REQUEST);
        }

        if (newSegment.getMinRed() <= 0 || newSegment.getMaxRed() <= 0) {
            return createErrorResponse("红灯时间参数必须大于0", HttpStatus.BAD_REQUEST);
        }

        if (newSegment.getMinGreen() <= 0 || newSegment.getMaxGreen() <= 0) {
            return createErrorResponse("绿灯时间参数必须大于0", HttpStatus.BAD_REQUEST);
        }

        if (newSegment.getMaxRed() < newSegment.getMinRed()) {
            return createErrorResponse("最大红灯时间不能小于最小红灯时间", HttpStatus.BAD_REQUEST);
        }

        if (newSegment.getMaxGreen() < newSegment.getMinGreen()) {
            return createErrorResponse("最大绿灯时间不能小于最小绿灯时间", HttpStatus.BAD_REQUEST);
        }

        return null; // 验证通过
    }

    /**
     * 创建错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", status.value());

        logger.warn("请求被拒绝: {}", message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 调试配置加载情况
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugConfig() {
        try {
            SingleLane config = configService.loadConfig();
            Map<String, Object> result = new HashMap<>();

            if (config != null && config.getSegments() != null) {
                result.put("configLoaded", true);
                result.put("segmentsCount", config.getSegments().getSegmentList().size());
                result.put("detectPointsCount", config.getDetectPoints().getDetectPointList().size());
                result.put("waitingAreasCount", config.getWaitingAreas().getWaitingAreas().size());
                result.put("globalConfigExists", config.getGlobal() != null);

                // 检查检测点是否为只读
                result.put("detectPointsReadonly", true); // config.xml中已标记为readonly="true"

                // 添加配置约束信息
                result.put("constraints", Map.of(
                        "allowModifyGlobal", true,
                        "allowModifySegments", true,
                        "allowModifyDetectPoints", false,
                        "allowModifyWaitingAreas", true,
                        "allowAddDelete", false
                ));
            } else {
                result.put("configLoaded", false);
                result.put("error", "config or segments is null");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("调试配置加载失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exception", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}