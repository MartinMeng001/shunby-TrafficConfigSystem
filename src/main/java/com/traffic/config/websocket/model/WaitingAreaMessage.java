package com.traffic.config.websocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 等待区消息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WaitingAreaMessage {
    private int segmentId;
    private boolean upstreamHasVehicle;
    private boolean upstreamRequest;
    private boolean downstreamHasVehicle;
    private boolean downstreamRequest;
    private int upstreamVehicleCount;
    private int downstreamVehicleCount;

    public WaitingAreaMessage(int segmentId) {
        this.segmentId = segmentId;
    }

    // Getters and Setters
    public int getSegmentId() { return segmentId; }
    public void setSegmentId(int segmentId) { this.segmentId = segmentId; }

    public boolean isUpstreamHasVehicle() { return upstreamHasVehicle; }
    public void setUpstreamHasVehicle(boolean upstreamHasVehicle) { this.upstreamHasVehicle = upstreamHasVehicle; }

    public boolean isUpstreamRequest() { return upstreamRequest; }
    public void setUpstreamRequest(boolean upstreamRequest) { this.upstreamRequest = upstreamRequest; }

    public boolean isDownstreamHasVehicle() { return downstreamHasVehicle; }
    public void setDownstreamHasVehicle(boolean downstreamHasVehicle) { this.downstreamHasVehicle = downstreamHasVehicle; }

    public boolean isDownstreamRequest() { return downstreamRequest; }
    public void setDownstreamRequest(boolean downstreamRequest) { this.downstreamRequest = downstreamRequest; }

    public int getUpstreamVehicleCount() { return upstreamVehicleCount; }
    public void setUpstreamVehicleCount(int upstreamVehicleCount) { this.upstreamVehicleCount = upstreamVehicleCount; }

    public int getDownstreamVehicleCount() { return downstreamVehicleCount; }
    public void setDownstreamVehicleCount(int downstreamVehicleCount) { this.downstreamVehicleCount = downstreamVehicleCount; }
}
