package com.traffic.config.controller;

import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.threading.SegmentStateMachineV3Service;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 路段车辆事件手动触发测试特征
 * 基于 SegmentStateMachineV3Service.processSegmentEventAsync 功能实现
 *
 * 功能：
 * 1. 支持手动触发指定路段的车辆进入/离开事件
 * 2. 支持上行和下行方向
 * 3. 提供两个固定车牌号进行测试
 * 4. 记录所有触发的事件历史
 *
 * @author System
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/test/vehicle-events")
@Component
public class SegmentVehicleEventTestFeature {

    private static final Logger logger = LoggerFactory.getLogger(SegmentVehicleEventTestFeature.class);

    @Autowired
    private SegmentStateMachineV3Service segmentStateMachineV3Service;

    // 固定的测试车牌号
    private static final String TEST_LICENSE_PLATE_1 = "TEST001";  // 上行方向测试车牌
    private static final String TEST_LICENSE_PLATE_2 = "TEST002";  // 下行方向测试车牌

    // 事件历史记录
    private static final Map<String, Object> eventHistory = new HashMap<>();
    private static int eventCounter = 0;

    /**
     * 获取测试功能说明
     */
    @GetMapping("/info")
    public Map<String, Object> getTestInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("feature", "路段车辆事件手动触发测试");
        info.put("description", "基于processSegmentEventAsync功能的手动测试工具");
        info.put("test_license_plate_1", TEST_LICENSE_PLATE_1);
        info.put("test_license_plate_2", TEST_LICENSE_PLATE_2);
        info.put("supported_segments", new int[]{1, 2, 3, 4});
        info.put("directions", new String[]{"upstream", "downstream"});
        info.put("actions", new String[]{"enter", "exit"});
        info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return info;
    }

    /**
     * 强控接口：手动触发路段控制阶段强控
     * @param segmentId 路段ID
     * @param ctrlPhase 控制阶段，取值: RED, YF, UP, DOWN
     * @return 操作结果
     */
    @PostMapping("/segment/{segmentId}/control/{ctrlPhase}")
    public Map<String, Object> triggerSegmentControl(@PathVariable int segmentId, @PathVariable String ctrlPhase) {
        Map<String, Object> response = new HashMap<>();
        try {
            SegmentEvent event;
            // 根据传入的ctrlPhase确定要触发的事件
            switch (ctrlPhase.toUpperCase()) {
                case "RED", "UP", "YF", "DOWN":
                    event = SegmentEvent.FORCE_SWITCH;
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "无效的控制阶段: " + ctrlPhase);
                    return response;
            }

            // 准备事件数据，可以根据需要添加更多信息
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("ctrlPhase", ctrlPhase);

            // 调用核心服务触发事件
            segmentStateMachineV3Service.processSegmentEventAsync(segmentId, event, eventData);

            response.put("success", true);
            response.put("message", "强控事件触发成功");
            response.put("segmentId", segmentId);
            response.put("ctrlPhase", ctrlPhase);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logger.info("手动触发路段强控事件 - 路段{} 阶段{}", segmentId, ctrlPhase);

        } catch (Exception e) {
            logger.error("触发强控事件失败 - 路段{} 阶段{}: {}", segmentId, ctrlPhase, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "触发强控事件失败: " + e.getMessage());
        }
        return response;
    }
    // ==================== 上行方向事件触发 (使用车牌TEST001) ====================

    /**
     * 手动触发路段1上行车入事件
     */
    @PostMapping("/segment1/upstream/enter")
    public Map<String, Object> triggerSegment1UpstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(1, SegmentEvent.VEHICLE_ENTER_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段1上行车出事件
     */
    @PostMapping("/segment1/upstream/exit")
    public Map<String, Object> triggerSegment1UpstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(1, SegmentEvent.VEHICLE_EXIT_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段2上行车入事件
     */
    @PostMapping("/segment2/upstream/enter")
    public Map<String, Object> triggerSegment2UpstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(2, SegmentEvent.VEHICLE_ENTER_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段2上行车出事件
     */
    @PostMapping("/segment2/upstream/exit")
    public Map<String, Object> triggerSegment2UpstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(2, SegmentEvent.VEHICLE_EXIT_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段3上行车入事件
     */
    @PostMapping("/segment3/upstream/enter")
    public Map<String, Object> triggerSegment3UpstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(3, SegmentEvent.VEHICLE_ENTER_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段3上行车出事件
     */
    @PostMapping("/segment3/upstream/exit")
    public Map<String, Object> triggerSegment3UpstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(3, SegmentEvent.VEHICLE_EXIT_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段4上行车入事件
     */
    @PostMapping("/segment4/upstream/enter")
    public Map<String, Object> triggerSegment4UpstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(4, SegmentEvent.VEHICLE_ENTER_UPSTREAM, licensePlate);
    }

    /**
     * 手动触发路段4上行车出事件
     */
    @PostMapping("/segment4/upstream/exit")
    public Map<String, Object> triggerSegment4UpstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(4, SegmentEvent.VEHICLE_EXIT_UPSTREAM, licensePlate);
    }

    // ==================== 下行方向事件触发 (使用车牌TEST002) ====================

    /**
     * 手动触发路段1下行车入事件
     */
    @PostMapping("/segment1/downstream/enter")
    public Map<String, Object> triggerSegment1DownstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(1, SegmentEvent.VEHICLE_ENTER_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段1下行车出事件
     */
    @PostMapping("/segment1/downstream/exit")
    public Map<String, Object> triggerSegment1DownstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(1, SegmentEvent.VEHICLE_EXIT_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段2下行车入事件
     */
    @PostMapping("/segment2/downstream/enter")
    public Map<String, Object> triggerSegment2DownstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(2, SegmentEvent.VEHICLE_ENTER_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段2下行车出事件
     */
    @PostMapping("/segment2/downstream/exit")
    public Map<String, Object> triggerSegment2DownstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(2, SegmentEvent.VEHICLE_EXIT_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段3下行车入事件
     */
    @PostMapping("/segment3/downstream/enter")
    public Map<String, Object> triggerSegment3DownstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(3, SegmentEvent.VEHICLE_ENTER_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段3下行车出事件
     */
    @PostMapping("/segment3/downstream/exit")
    public Map<String, Object> triggerSegment3DownstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(3, SegmentEvent.VEHICLE_EXIT_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段4下行车入事件
     */
    @PostMapping("/segment4/downstream/enter")
    public Map<String, Object> triggerSegment4DownstreamEnter(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(4, SegmentEvent.VEHICLE_ENTER_DOWNSTREAM, licensePlate);
    }

    /**
     * 手动触发路段4下行车出事件
     */
    @PostMapping("/segment4/downstream/exit")
    public Map<String, Object> triggerSegment4DownstreamExit(@RequestParam(required = false) String licensePlate) {
        return triggerVehicleEvent(4, SegmentEvent.VEHICLE_EXIT_DOWNSTREAM, licensePlate);
    }

    // ==================== 通用方法和工具方法 ====================

    /**
     * 通用的车辆事件触发方法
     * 传入的licensePlate为空时，根据方向使用固定的测试车牌
     */
    private Map<String, Object> triggerVehicleEvent(int segmentId, SegmentEvent event, String licensePlate) {
        // 如果传入的licensePlate为空，则根据事件方向选择固定的测试车牌
        String finalLicensePlate = licensePlate;
        if (finalLicensePlate == null || finalLicensePlate.isEmpty()) {
            if (event.isUpstreamVehicleEvent()) {
                finalLicensePlate = TEST_LICENSE_PLATE_1;
            } else {
                finalLicensePlate = TEST_LICENSE_PLATE_2;
            }
        }

        try {
            // 准备事件数据
            Map<String, Object> eventData = createEventData(finalLicensePlate);
            switch (event) {
                case VEHICLE_ENTER_UPSTREAM, VEHICLE_EXIT_UPSTREAM->{
                    eventData.put("direction", SegmentVariables.Direction.UPSTREAM);
                }
                case VEHICLE_ENTER_DOWNSTREAM, VEHICLE_EXIT_DOWNSTREAM->{
                    eventData.put("direction", SegmentVariables.Direction.DOWNSTREAM);
                }
            }
//            if(finalLicensePlate.equalsIgnoreCase(TEST_LICENSE_PLATE_1))
//                eventData.put("direction", SegmentVariables.Direction.UPSTREAM);
//            else eventData.put("direction", SegmentVariables.Direction.DOWNSTREAM);
            eventData.put("vehicleId", finalLicensePlate);

            // 调用核心服务的processSegmentEventAsync方法
//            CompletableFuture<Boolean> future = segmentStateMachineV3Service
//                    .processSegmentEventAsync(segmentId, event, eventData);
            segmentStateMachineV3Service.processSegmentEventSync(segmentId, event, eventData);
            // 记录事件历史
            recordEventHistory(segmentId, event, finalLicensePlate);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("message", "事件触发成功");
            response.put("segmentId", segmentId);
            response.put("event", event.getChineseName());
            response.put("eventCode", event.getCode());
            response.put("licensePlate", finalLicensePlate);
            response.put("direction", event.getVehicleDirection());
            response.put("action", event.getVehicleAction());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("eventCounter", eventCounter);

            logger.info("手动触发车辆事件 - 路段{} {} 车牌{}",
                    segmentId, event.getChineseName(), finalLicensePlate);

            return response;

        } catch (Exception e) {
            logger.error("触发车辆事件失败 - 路段{} {} 车牌{}: {}",
                    segmentId, event.getChineseName(), finalLicensePlate, e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "事件触发失败: " + e.getMessage());
            errorResponse.put("segmentId", segmentId);
            errorResponse.put("event", event.getChineseName());
            errorResponse.put("licensePlate", finalLicensePlate);
            errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return errorResponse;
        }
    }

    /**
     * 创建事件数据
     */
    private Map<String, Object> createEventData(String licensePlate) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("vehicle_id", licensePlate);
        eventData.put("timestamp", LocalDateTime.now());
        eventData.put("source", "ManualTestTrigger");
        eventData.put("test_mode", true);
        eventData.put("trigger_id", "TEST_" + System.currentTimeMillis());
        return eventData;
    }

    /**
     * 记录事件历史
     */
    private void recordEventHistory(int segmentId, SegmentEvent event, String licensePlate) {
        eventCounter++;
        String historyKey = "event_" + eventCounter;

        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("eventId", eventCounter);
        historyEntry.put("segmentId", segmentId);
        historyEntry.put("event", event.getChineseName());
        historyEntry.put("eventCode", event.getCode());
        historyEntry.put("licensePlate", licensePlate);
        historyEntry.put("direction", event.getVehicleDirection());
        historyEntry.put("action", event.getVehicleAction());
        historyEntry.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        eventHistory.put(historyKey, historyEntry);
    }

    // ==================== 查询和管理接口 ====================

    /**
     * 获取事件历史记录
     */
    @GetMapping("/history")
    public Map<String, Object> getEventHistory() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalEvents", eventCounter);
        response.put("history", eventHistory);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    /**
     * 清除事件历史记录
     */
    @PostMapping("/history/clear")
    public Map<String, Object> clearEventHistory() {
        int clearedCount = eventCounter;
        eventHistory.clear();
        eventCounter = 0;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "事件历史已清除");
        response.put("clearedCount", clearedCount);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        logger.info("清除了 {} 条事件历史记录", clearedCount);
        return response;
    }

    /**
     * 批量触发测试场景
     */
//    @PostMapping("/scenario/complete-flow")
//    public Map<String, Object> triggerCompleteFlowScenario() {
//        Map<String, Object> response = new HashMap<>();
//        Map<String, Object> results = new HashMap<>();
//
//        try {
//            // 模拟完整的车辆通过流程
//            // 路段1: 上行车入 -> 上行车出
//            results.put("seg1_up_enter", triggerSegment1UpstreamEnter());
//            Thread.sleep(1000); // 模拟停留时间
//            results.put("seg1_up_exit", triggerSegment1UpstreamExit());
//
//            // 路段2: 下行车入 -> 下行车出
//            results.put("seg2_down_enter", triggerSegment2DownstreamEnter());
//            Thread.sleep(1000);
//            results.put("seg2_down_exit", triggerSegment2DownstreamExit());
//
//            response.put("success", true);
//            response.put("message", "完整流程测试场景执行成功");
//            response.put("results", results);
//
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", "完整流程测试场景执行失败: " + e.getMessage());
//            response.put("results", results);
//        }
//
//        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        return response;
//    }

    /**
     * 获取当前状态机状态（如果可用）
     */
    @GetMapping("/status")
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("feature", "SegmentVehicleEventTestFeature");
        status.put("status", "ACTIVE");
        status.put("totalTriggeredEvents", eventCounter);
        status.put("testLicensePlates", new String[]{TEST_LICENSE_PLATE_1, TEST_LICENSE_PLATE_2});
        status.put("supportedSegments", new int[]{1, 2, 3, 4});
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return status;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "SegmentVehicleEventTestFeature");
        health.put("segmentStateMachineV3Service",
                segmentStateMachineV3Service != null ? "AVAILABLE" : "UNAVAILABLE");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return health;
    }
}
