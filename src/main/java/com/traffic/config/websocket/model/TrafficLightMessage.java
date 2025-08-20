package com.traffic.config.websocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 红绿灯消息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficLightMessage {
    private String sigid;
    private String status; // UPSTREAM, DOWNSTREAM, ALL_RED, YELLOW_FLASH
    private String statusDescription;
    private int phase; // 控制相位值

    public TrafficLightMessage(String sigid, String status, String statusDescription, int phase) {
        this.sigid = sigid;
        this.status = status;
        this.statusDescription = statusDescription;
        this.phase = phase;
    }

    // Getters and Setters
    public String getSigid() { return sigid; }
    public void setSigid(String sigid) { this.sigid = sigid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public int getPhase() { return phase; }
    public void setPhase(int phase) { this.phase = phase; }
}
