package com.traffic.config.cardetector.model;

import com.traffic.config.common.enums.DataType;
import com.traffic.config.common.enums.VehicleDirection;

import java.net.InetAddress;
import java.time.LocalDateTime;

public class VehicleData {
    private DataType dataType;              // 数据类型
    private InetAddress signalIp;           // 信号机IP
    private int laneNumber;                 // 车道编号
    private String licensePlate;            // 车牌ID (16字节)
    private VehicleDirection direction;     // 车入/车出
    private int queueLength;                // 排队长度(m)
    private int speed;                      // 速度(km/h)
    private LocalDateTime timestamp;        // 时间戳

    public VehicleData() {
        this.dataType = DataType.NORMAL_DATA;
        this.timestamp = LocalDateTime.now();
    }

    // getter和setter方法
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    public InetAddress getSignalIp() { return signalIp; }
    public void setSignalIp(InetAddress signalIp) { this.signalIp = signalIp; }

    public int getLaneNumber() { return laneNumber; }
    public void setLaneNumber(int laneNumber) { this.laneNumber = laneNumber; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public VehicleDirection getDirection() { return direction; }
    public void setDirection(VehicleDirection direction) { this.direction = direction; }

    public int getQueueLength() { return queueLength; }
    public void setQueueLength(int queueLength) { this.queueLength = queueLength; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

