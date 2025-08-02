package com.traffic.config.cardetector.model;

import com.traffic.config.common.enums.DataType;

import java.net.InetAddress;
import java.time.LocalDateTime;

public class HeartbeatData {
    private DataType dataType;              // 数据类型
    private InetAddress signalIp;           // 信号机IP
    private LocalDateTime timestamp;        // 时间戳

    public HeartbeatData() {
        this.dataType = DataType.HEARTBEAT;
        this.timestamp = LocalDateTime.now();
    }

    // getter和setter方法
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    public InetAddress getSignalIp() { return signalIp; }
    public void setSignalIp(InetAddress signalIp) { this.signalIp = signalIp; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

