package com.traffic.config.websocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 车道整体状态消息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LaneStatusMessage {
    private int segmentId;
    private List<VehiclePassInfo> upstreamVehicles;
    private List<VehiclePassInfo> downstreamVehicles;
    private int upstreamInCount;
    private int upstreamOutCount;
    private int downstreamInCount;
    private int downstreamOutCount;
    private double throughputRate; // 通行效率
    private double congestionLevel; // 拥堵程度

    public LaneStatusMessage(int segmentId) {
        this.segmentId = segmentId;
    }

    // Getters and Setters
    public int getSegmentId() { return segmentId; }
    public void setSegmentId(int segmentId) { this.segmentId = segmentId; }

    public List<VehiclePassInfo> getUpstreamVehicles() { return upstreamVehicles; }
    public void setUpstreamVehicles(List<VehiclePassInfo> upstreamVehicles) { this.upstreamVehicles = upstreamVehicles; }

    public List<VehiclePassInfo> getDownstreamVehicles() { return downstreamVehicles; }
    public void setDownstreamVehicles(List<VehiclePassInfo> downstreamVehicles) { this.downstreamVehicles = downstreamVehicles; }

    public int getUpstreamInCount() { return upstreamInCount; }
    public void setUpstreamInCount(int upstreamInCount) { this.upstreamInCount = upstreamInCount; }

    public int getUpstreamOutCount() { return upstreamOutCount; }
    public void setUpstreamOutCount(int upstreamOutCount) { this.upstreamOutCount = upstreamOutCount; }

    public int getDownstreamInCount() { return downstreamInCount; }
    public void setDownstreamInCount(int downstreamInCount) { this.downstreamInCount = downstreamInCount; }

    public int getDownstreamOutCount() { return downstreamOutCount; }
    public void setDownstreamOutCount(int downstreamOutCount) { this.downstreamOutCount = downstreamOutCount; }

    public double getThroughputRate() { return throughputRate; }
    public void setThroughputRate(double throughputRate) { this.throughputRate = throughputRate; }

    public double getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(double congestionLevel) { this.congestionLevel = congestionLevel; }
}
