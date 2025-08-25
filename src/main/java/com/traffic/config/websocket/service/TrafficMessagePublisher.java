package com.traffic.config.websocket.service;

import com.traffic.config.signalplatform.platformbase.CrossInfoManager;
import com.traffic.config.signalplatform.platformbase.entity.CrossInfo;
import com.traffic.config.signalplatform.platformbase.enums.ControlPhase;
import com.traffic.config.statemachinev3.core.SegmentStateMachine;
import com.traffic.config.statemachinev3.core.TopLevelStateMachine;
import com.traffic.config.statemachinev3.events.*;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import com.traffic.config.statemachinev3.variables.objects.CrossMeetingArea;
import com.traffic.config.statemachinev3.variables.objects.CrossMettingZoneManager;
import com.traffic.config.statemachinev3.variables.objects.MeetingArea;
import com.traffic.config.websocket.events.SegmentStateChangeEvent;
import com.traffic.config.websocket.events.VehicleEnterEvent;
import com.traffic.config.websocket.events.VehicleLeaveEvent;
import com.traffic.config.websocket.handler.TrafficWebSocketHandler;
import com.traffic.config.websocket.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交通消息发布服务
 * 负责监听系统事件并通过WebSocket发布交通信息
 */
@Service
public class TrafficMessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(TrafficMessagePublisher.class);

    @Autowired
    private TrafficWebSocketHandler webSocketHandler;

    @Autowired
    private CrossInfoManager crossInfoManager;

    @Autowired
    private TopLevelStateMachine topLevelStateMachine;

    /**
     * 缓存上一次的状态，用于检测变化
     */
    private final Map<String, Object> lastStateCache = new ConcurrentHashMap<>();

    /**
     * 监听车辆进入事件
     */
    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleVehicleEnter(VehicleEnterEvent event) {
        try {
            int segmentId = event.getSegmentId();
            String vehicleId = event.getVehicleId();
            String direction = event.getDirection().toString();

            SegmentMessage segmentMessage = new SegmentMessage(segmentId, "VEHICLE_ENTER", vehicleId, direction);

            // 获取当前车辆数量
            SegmentStateMachine segmentMachine = topLevelStateMachine.getSegmentStateMachine(segmentId);
            if (segmentMachine != null) {
                SegmentVariables variables = segmentMachine.getVariables();
                if ("UPSTREAM".equals(direction)) {
                    segmentMessage.setVehicleCount(variables.getUpstreamVehicleIds().size());
                } else {
                    segmentMessage.setVehicleCount(variables.getDownstreamVehicleIds().size());
                }
            }

            TrafficMessage message = new TrafficMessage("SEGMENT", segmentMessage);
            webSocketHandler.broadcastMessage(message);

//            logger.debug("发布车辆进入消息: segmentId={}, vehicleId={}, direction={}",
//                    segmentId, vehicleId, direction);

        } catch (Exception e) {
            logger.error("处理车辆进入事件时出错", e);
        }
    }

    /**
     * 监听车辆离开事件
     */
    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleVehicleLeave(VehicleLeaveEvent event) {
        try {
            int segmentId = event.getSegmentId();
            String vehicleId = event.getVehicleId();
            String direction = event.getDirection().toString();

            SegmentMessage segmentMessage = new SegmentMessage(segmentId, "VEHICLE_LEAVE", vehicleId, direction);

            // 获取当前车辆数量
            SegmentStateMachine segmentMachine = topLevelStateMachine.getSegmentStateMachine(segmentId);
            if (segmentMachine != null) {
                SegmentVariables variables = segmentMachine.getVariables();
                if ("UPSTREAM".equals(direction)) {
                    segmentMessage.setVehicleCount(variables.getUpstreamVehicleIds().size());
                } else {
                    segmentMessage.setVehicleCount(variables.getDownstreamVehicleIds().size());
                }
            }

            TrafficMessage message = new TrafficMessage("SEGMENT", segmentMessage);
            webSocketHandler.broadcastMessage(message);

            logger.debug("发布车辆离开消息: segmentId={}, vehicleId={}, direction={}",
                    segmentId, vehicleId, direction);

        } catch (Exception e) {
            logger.error("处理车辆离开事件时出错", e);
        }
    }

    /**
     * 监听路段状态变化事件
     */
    @EventListener
    @Async("platformEventHandlerThreadPool")
    public void handleSegmentStateChange(SegmentStateChangeEvent event) {
        try {
            int segmentId = event.getSegmentId();
            String newState = event.getNewState().toString();

            // 检查是否为清空状态
            if ("ALL_RED_CLEAR".equals(newState)) {
                SegmentMessage segmentMessage = new SegmentMessage(segmentId, "CLEAR");
                TrafficMessage message = new TrafficMessage("SEGMENT", segmentMessage);
                webSocketHandler.broadcastMessage(message);

                logger.debug("发布路段清空消息: segmentId={}", segmentId);
            }

        } catch (Exception e) {
            logger.error("处理路段状态变化事件时出错", e);
        }
    }

    /**
     * 定期推送等待区状态
     */
    @Scheduled(fixedRate = 1000) // 每1秒推送一次
    public void publishWaitingAreaStatus() {
        try {
            if (webSocketHandler.getActiveSessionCount() == 0) {
                return; // 没有连接的客户端，跳过推送
            }
            CrossMettingZoneManager.getInstance().getAllCrossMeetingZone().forEach((zoneId, meetingArea) -> {
                publishWaitingAreaStatus(meetingArea);
            });

//            List<SegmentStateMachine> segmentMachines = topLevelStateMachine.getSegmentStateMachines();
//            for (SegmentStateMachine segmentMachine : segmentMachines) {
//                publishWaitingAreaStatusForSegment(segmentMachine);
//            }

        } catch (Exception e) {
            logger.error("定期推送等待区状态时出错", e);
        }
    }

    /**
     * 定期推送车道整体状态
     */
    @Scheduled(fixedRate = 3000) // 每3秒推送一次
    public void publishLaneStatus() {
        try {
            if (webSocketHandler.getActiveSessionCount() == 0) {
                return; // 没有连接的客户端，跳过推送
            }

            List<SegmentStateMachine> segmentMachines = topLevelStateMachine.getSegmentStateMachines();
            for (SegmentStateMachine segmentMachine : segmentMachines) {
                publishLaneStatusForSegment(segmentMachine);
            }

        } catch (Exception e) {
            logger.error("定期推送车道状态时出错", e);
        }
    }

    /**
     * 发布所有红绿灯状态
     */
    @Scheduled(fixedRate = 1000)
    public void publishAllTrafficLightStatus() {
        Map<String, CrossInfo> crossInfoMap = crossInfoManager.getAllCrossInfo();

        for (Map.Entry<String, CrossInfo> entry : crossInfoMap.entrySet()) {
            String sigid = entry.getKey();
            CrossInfo crossInfo = entry.getValue();

            publishTrafficLightStatus(sigid, crossInfo);
        }
    }

    /**
     * 发布特定路段的红绿灯状态
     */
//    private void publishTrafficLightStatusForSegment(int segmentId) {
//        // 这里需要根据segmentId找到对应的sigid
//        // 暂时遍历所有crossInfo来找到匹配的
//        Map<String, CrossInfo> crossInfoMap = crossInfoManager.getAllCrossInfo();
//
//        for (Map.Entry<String, CrossInfo> entry : crossInfoMap.entrySet()) {
//            String sigid = entry.getKey();
//            CrossInfo crossInfo = entry.getValue();
//
//            // 这里可以添加segmentId与sigid的映射逻辑
//            publishTrafficLightStatus(sigid, crossInfo);
//        }
//    }

    /**
     * 发布红绿灯状态
     */
    private void publishTrafficLightStatus(String sigid, CrossInfo crossInfo) {
        try {
            int ctrlPhase = crossInfo.getCtrlPhase();
            String status = getTrafficLightStatus(ctrlPhase);
            String statusDescription = getTrafficLightStatusDescription(ctrlPhase);

            // 检查状态是否发生变化
            String cacheKey = "traffic_light_" + sigid;
            Object lastPhase = lastStateCache.get(cacheKey);
//            if (lastPhase != null && lastPhase.equals(ctrlPhase)) {
//                return; // 状态未变化，不发送消息
//            }
            lastStateCache.put(cacheKey, ctrlPhase);

            TrafficLightMessage trafficLightMessage = new TrafficLightMessage(
                    sigid, status, statusDescription, ctrlPhase);
            TrafficMessage message = new TrafficMessage("TRAFFIC_LIGHT", trafficLightMessage);
            webSocketHandler.broadcastMessage(message);

//            logger.debug("发布红绿灯状态: sigid={}, status={}, phase={}",
//                    sigid, status, ctrlPhase);

        } catch (Exception e) {
            logger.error("发布红绿灯状态时出错: sigid={}", sigid, e);
        }
    }

    /**
     * 发布等待区状态
     */
    private void publishWaitingAreaStatus(CrossMeetingArea meetingArea){
        try{
            WaitingAreaMessage waitingAreaMessage = new WaitingAreaMessage(meetingArea.getCrossMeetingAreaId());
            waitingAreaMessage.setUpstreamHasVehicle(!meetingArea.getUpMeetingArea().isEmpty());
            waitingAreaMessage.setUpstreamRequest(meetingArea.getUpMeetingArea().isEmpty()?false:true);
            waitingAreaMessage.setDownstreamHasVehicle(!meetingArea.getDownMeetingArea().isEmpty());
            waitingAreaMessage.setDownstreamRequest(meetingArea.getDownMeetingArea().isEmpty()?false:true);
            waitingAreaMessage.setUpstreamVehicleCount(meetingArea.getUpMeetingArea().getCount());
            waitingAreaMessage.setDownstreamVehicleCount(meetingArea.getDownMeetingArea().getCount());

            // 检查状态是否发生变化
            String cacheKey = "waiting_area_" + meetingArea.getCrossMeetingAreaId();
            String currentStateKey = generateWaitingAreaStateKey(waitingAreaMessage);
            Object lastStateKey = lastStateCache.get(cacheKey);
            if (lastStateKey != null && lastStateKey.equals(currentStateKey)) {
                return; // 状态未变化，不发送消息
            }
            lastStateCache.put(cacheKey, currentStateKey);

            TrafficMessage message = new TrafficMessage("WAITING_AREA", waitingAreaMessage);
            webSocketHandler.broadcastMessage(message);

            logger.debug("发布等待区状态: meetingAreaId={}, upVehicles={}, downVehicles={}, upRequest={}, downRequest={}",
                    meetingArea.getCrossMeetingAreaId(), waitingAreaMessage.getUpstreamVehicleCount(),
                    waitingAreaMessage.getDownstreamVehicleCount(),
                    waitingAreaMessage.isUpstreamRequest(), waitingAreaMessage.isDownstreamRequest());
        } catch (Exception e) {
            logger.error("发布等待区状态时出错: meetingAreaId={}",
                    meetingArea.getCrossMeetingAreaId(), e);
        }
    }
    private void publishWaitingAreaStatusForSegment(SegmentStateMachine segmentMachine) {
        try {
            SegmentVariables variables = segmentMachine.getVariables();
            int segmentId = variables.getSegmentId();

            WaitingAreaMessage waitingAreaMessage = new WaitingAreaMessage(segmentId);
            waitingAreaMessage.setUpstreamHasVehicle(!variables.isEmptyUpstreamMeetingzone());
            waitingAreaMessage.setUpstreamRequest(variables.isUpstreamRequest());
            waitingAreaMessage.setDownstreamHasVehicle(!variables.isEmptyDownstreamMeetingzone());
            waitingAreaMessage.setDownstreamRequest(variables.isDownstreamRequest());
            waitingAreaMessage.setUpstreamVehicleCount(variables.getUpMeetingZoneCount());
            waitingAreaMessage.setDownstreamVehicleCount(variables.getDownMeetingZoneCount());

            // 检查状态是否发生变化
            String cacheKey = "waiting_area_" + segmentId;
            String currentStateKey = generateWaitingAreaStateKey(waitingAreaMessage);
            Object lastStateKey = lastStateCache.get(cacheKey);
            if (lastStateKey != null && lastStateKey.equals(currentStateKey)) {
                return; // 状态未变化，不发送消息
            }
            lastStateCache.put(cacheKey, currentStateKey);

            TrafficMessage message = new TrafficMessage("WAITING_AREA", waitingAreaMessage);
            webSocketHandler.broadcastMessage(message);

            logger.debug("发布等待区状态: segmentId={}, upVehicles={}, downVehicles={}, upRequest={}, downRequest={}",
                    segmentId, waitingAreaMessage.getUpstreamVehicleCount(),
                    waitingAreaMessage.getDownstreamVehicleCount(),
                    waitingAreaMessage.isUpstreamRequest(), waitingAreaMessage.isDownstreamRequest());

        } catch (Exception e) {
            logger.error("发布等待区状态时出错: segmentId={}",
                    segmentMachine.getVariables().getSegmentId(), e);
        }
    }

    /**
     * 发布车道整体状态
     */
    private void publishLaneStatusForSegment(SegmentStateMachine segmentMachine) {
        try {
            SegmentVariables variables = segmentMachine.getVariables();
            int segmentId = variables.getSegmentId();

            LaneStatusMessage laneStatusMessage = new LaneStatusMessage(segmentId);

            // 设置车辆计数信息
            laneStatusMessage.setUpstreamInCount(variables.getUpstreamInCounter());
            laneStatusMessage.setUpstreamOutCount(variables.getUpstreamOutCounter());
            laneStatusMessage.setDownstreamInCount(variables.getDownstreamInCounter());
            laneStatusMessage.setDownstreamOutCount(variables.getDownstreamOutCounter());

            // 设置性能指标
            laneStatusMessage.setThroughputRate(variables.getThroughputRate());
            laneStatusMessage.setCongestionLevel(variables.getCongestionLevel());

            // 构建车辆通过信息列表
            List<VehiclePassInfo> upstreamVehicles = new ArrayList<>();
            List<VehiclePassInfo> downstreamVehicles = new ArrayList<>();

            // 获取当前在路段内的车辆信息
            Set<String> upstreamIds = variables.getUpstreamVehicleIds();
            Set<String> downstreamIds = variables.getDownstreamVehicleIds();

            for (String vehicleId : upstreamIds) {
                LocalDateTime enterTime = variables.getVehicleEntryTimes().get(vehicleId);
                if (enterTime == null) {
                    enterTime = LocalDateTime.now(); // 默认当前时间
                }
                upstreamVehicles.add(new VehiclePassInfo(vehicleId, "UPSTREAM", enterTime));
            }

            for (String vehicleId : downstreamIds) {
                LocalDateTime enterTime = variables.getVehicleEntryTimes().get(vehicleId);
                if (enterTime == null) {
                    enterTime = LocalDateTime.now(); // 默认当前时间
                }
                downstreamVehicles.add(new VehiclePassInfo(vehicleId, "DOWNSTREAM", enterTime));
            }

            laneStatusMessage.setUpstreamVehicles(upstreamVehicles);
            laneStatusMessage.setDownstreamVehicles(downstreamVehicles);

            TrafficMessage message = new TrafficMessage("LANE_STATUS", laneStatusMessage);
            webSocketHandler.broadcastMessage(message);

//            logger.debug("发布车道状态: segmentId={}, upIn={}, upOut={}, downIn={}, downOut={}, throughput={}, congestion={}",
//                    segmentId, laneStatusMessage.getUpstreamInCount(), laneStatusMessage.getUpstreamOutCount(),
//                    laneStatusMessage.getDownstreamInCount(), laneStatusMessage.getDownstreamOutCount(),
//                    laneStatusMessage.getThroughputRate(), laneStatusMessage.getCongestionLevel());

        } catch (Exception e) {
            logger.error("发布车道状态时出错: segmentId={}",
                    segmentMachine.getVariables().getSegmentId(), e);
        }
    }

    /**
     * 根据控制相位获取红绿灯状态
     */
    private String getTrafficLightStatus(int ctrlPhase) {
        if (ctrlPhase == ControlPhase.NORTH_FULL_GREEN.getValue()) {
            return "UPSTREAM";
        } else if (ctrlPhase == ControlPhase.SOUTH_FULL_GREEN.getValue()) {
            return "DOWNSTREAM";
        } else if (ctrlPhase == ControlPhase.ALL_RED.getValue()) {
            return "ALL_RED";
        } else if (ctrlPhase == ControlPhase.YELLOW_FLASH.getValue()) {
            return "YELLOW_FLASH";
        } else if(ctrlPhase == ControlPhase.SOUTH_NORTH_ALL_GREEN.getValue()){
            return "UPDOWN";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 根据控制相位获取红绿灯状态描述
     */
    private String getTrafficLightStatusDescription(int ctrlPhase) {
        try {
            ControlPhase phase = ControlPhase.fromValue(ctrlPhase);
            return phase.getLabel();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }

    /**
     * 生成等待区状态的缓存键
     */
    private String generateWaitingAreaStateKey(WaitingAreaMessage message) {
        return String.format("%s_%s_%s_%s_%d_%d",
                message.isUpstreamHasVehicle(),
                message.isUpstreamRequest(),
                message.isDownstreamHasVehicle(),
                message.isDownstreamRequest(),
                message.getUpstreamVehicleCount(),
                message.getDownstreamVehicleCount());
    }

    /**
     * 清除状态缓存
     */
    public void clearStateCache() {
        lastStateCache.clear();
        logger.info("状态缓存已清除");
    }

    /**
     * 获取当前活跃WebSocket连接数
     */
    public int getActiveConnectionCount() {
        return webSocketHandler.getActiveSessionCount();
    }

    /**
     * 手动发布所有状态信息
     */
    public void publishAllStatus() {
        try {
            publishAllTrafficLightStatus();

            List<SegmentStateMachine> segmentMachines = topLevelStateMachine.getSegmentStateMachines();
            for (SegmentStateMachine segmentMachine : segmentMachines) {
                //publishWaitingAreaStatusForSegment(segmentMachine);
                publishLaneStatusForSegment(segmentMachine);
            }

            logger.info("手动发布所有状态信息完成");
        } catch (Exception e) {
            logger.error("手动发布所有状态信息时出错", e);
        }
    }
}