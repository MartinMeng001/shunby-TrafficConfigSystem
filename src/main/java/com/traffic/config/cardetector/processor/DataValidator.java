package com.traffic.config.cardetector.processor;

import com.traffic.config.cardetector.model.HeartbeatData;
import com.traffic.config.cardetector.model.VehicleData;
import com.traffic.config.common.enums.DataType;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataValidator {
    private static final Logger log = LoggerFactory.getLogger(DataValidator.class);

    public boolean validateHeartbeatData(HeartbeatData data) {
        if (data == null) {
            log.warn("心跳数据为空");
            return false;
        }

        if (data.getDataType() != DataType.HEARTBEAT) {
            log.warn("心跳数据类型错误: {}", data.getDataType());
            return false;
        }

        if (data.getSignalIp() == null) {
            log.warn("心跳数据缺少信号机IP");
            return false;
        }

        return true;
    }

    public boolean validateVehicleData(VehicleData data) {
        if (data == null) {
            log.warn("车辆数据为空");
            return false;
        }

        if (data.getDataType() != DataType.NORMAL_DATA) {
            log.warn("车辆数据类型错误: {}", data.getDataType());
            return false;
        }

        if (data.getSignalIp() == null) {
            log.warn("车辆数据缺少信号机IP");
            return false;
        }

        if (data.getLicensePlate() == null || data.getLicensePlate().trim().isEmpty()) {
            log.warn("车辆数据缺少车牌号");
            return false;
        }

        if (data.getLaneNumber() < 0 || data.getLaneNumber() > 255) {
            log.warn("车道编号超出范围: {}", data.getLaneNumber());
            return false;
        }

        if (data.getQueueLength() < 0 || data.getQueueLength() > 255) {
            log.warn("排队长度超出范围: {}", data.getQueueLength());
            return false;
        }

        if (data.getSpeed() < 0 || data.getSpeed() > 255) {
            log.warn("速度超出范围: {}", data.getSpeed());
            return false;
        }

        return true;
    }
}
