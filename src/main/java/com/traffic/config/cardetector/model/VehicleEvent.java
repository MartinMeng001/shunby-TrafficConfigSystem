package com.traffic.config.cardetector.model;

import com.traffic.config.common.enums.Direction;
import com.traffic.config.common.enums.EventType;

import java.time.LocalDateTime;

/**
 * 车辆事件
 */
public class VehicleEvent {
    private String eventId;             // 事件ID
    private String vehicleId;           // 车辆ID（车牌）
    private String segmentId;           // 路段ID
    private Direction direction;        // 行驶方向
    private EventType eventType;        // 事件类型
    private LocalDateTime eventTime;    // 事件发生时间
    private String sensorId;            // 传感器ID
    private String rawData;             // 原始数据
    private String sourceIp;            // 数据来源IP

    public VehicleEvent() {
        this.eventId = generateEventId();
        this.eventTime = LocalDateTime.now();
    }

    public VehicleEvent(String vehicleId, String segmentId, Direction direction, EventType eventType) {
        this();
        this.vehicleId = vehicleId;
        this.segmentId = segmentId;
        this.direction = direction;
        this.eventType = eventType;
    }

    private String generateEventId() {
        return "EVT_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    @Override
    public String toString() {
        return String.format("VehicleEvent{id='%s', vehicle='%s', segment='%s', direction=%s, type=%s, time=%s}",
                eventId, vehicleId, segmentId, direction, eventType, eventTime);
    }
}

