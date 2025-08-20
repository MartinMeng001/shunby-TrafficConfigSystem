package com.traffic.config.websocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 路段消息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentMessage {
    private int segmentId;
    private String status; // CLEAR, VEHICLE_ENTER, VEHICLE_LEAVE
    private String vehicleId;
    private String direction; // UPSTREAM, DOWNSTREAM
    private int vehicleCount; // 当前车辆数量

    public SegmentMessage(int segmentId, String status) {
        this.segmentId = segmentId;
        this.status = status;
    }

    public SegmentMessage(int segmentId, String status, String vehicleId, String direction) {
        this.segmentId = segmentId;
        this.status = status;
        this.vehicleId = vehicleId;
        this.direction = direction;
    }

    // Getters and Setters
    public int getSegmentId() { return segmentId; }
    public void setSegmentId(int segmentId) { this.segmentId = segmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public int getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(int vehicleCount) { this.vehicleCount = vehicleCount; }
}
