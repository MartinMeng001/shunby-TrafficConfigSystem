package com.traffic.config.cardetector.parser;

import com.traffic.config.cardetector.model.ProtocolMessage;
import com.traffic.config.cardetector.model.VehicleData;
import com.traffic.config.common.enums.DataType;
import com.traffic.config.common.enums.VehicleDirection;
import com.traffic.config.exception.DataParseException;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Component
public class VehicleDataParser implements DataParser<VehicleData> {

    @Override
    public VehicleData parse(ProtocolMessage message) throws DataParseException {
        byte[] data = message.getData();

        if (data.length < 25) { // 1+4+1+16+1+1+1 = 25字节
            throw new DataParseException("车辆数据长度不足");
        }

        try {
            VehicleData vehicleData = new VehicleData();
            int offset = 0;

            // 数据类型 (1字节)
            DataType dataType = DataType.fromCode(data[offset] & 0xFF);
            vehicleData.setDataType(dataType);
            offset++;

            // 信号机IP (4字节)
            byte[] ipBytes = new byte[4];
            System.arraycopy(data, offset, ipBytes, 0, 4);
            InetAddress signalIp = InetAddress.getByAddress(ipBytes);
            vehicleData.setSignalIp(signalIp);
            offset += 4;

            // 车道编号 (1字节)
            int laneNumber = data[offset] & 0xFF;
            vehicleData.setLaneNumber(laneNumber);
            offset++;

            // 车牌ID (16字节)
            byte[] plateBytes = new byte[16];
            System.arraycopy(data, offset, plateBytes, 0, 16);
            String licensePlate = new String(plateBytes, StandardCharsets.UTF_8).trim();
            vehicleData.setLicensePlate(licensePlate);
            offset += 16;

            // 车入/车出 (1字节)
            VehicleDirection direction = VehicleDirection.fromCode(data[offset] & 0xFF);
            vehicleData.setDirection(direction);
            offset++;

            // 排队长度 (1字节)
            int queueLength = data[offset] & 0xFF;
            vehicleData.setQueueLength(queueLength);
            offset++;

            // 速度 (1字节)
            int speed = data[offset] & 0xFF;
            vehicleData.setSpeed(speed);

            return vehicleData;

        } catch (Exception e) {
            throw new DataParseException("解析车辆数据失败", e);
        }
    }

    @Override
    public boolean canParse(ProtocolMessage message) {
        return message.getData().length >= 1 && message.getData()[0] == 1;
    }
}
