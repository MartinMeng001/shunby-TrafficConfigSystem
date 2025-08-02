package com.traffic.config.cardetector.parser;

import com.traffic.config.cardetector.model.HeartbeatData;
import com.traffic.config.cardetector.model.ProtocolMessage;
import com.traffic.config.common.enums.DataType;
import com.traffic.config.exception.DataParseException;
import org.springframework.stereotype.Component;
import java.net.InetAddress;

@Component
public class HeartbeatDataParser implements DataParser<HeartbeatData> {

    @Override
    public HeartbeatData parse(ProtocolMessage message) throws DataParseException {
        byte[] data = message.getData();

        if (data.length < 5) { // 1字节数据类型 + 4字节IP
            throw new DataParseException("心跳数据长度不足");
        }

        try {
            HeartbeatData heartbeat = new HeartbeatData();

            // 数据类型
            DataType dataType = DataType.fromCode(data[0] & 0xFF);
            heartbeat.setDataType(dataType);

            // 信号机IP (4字节)
            byte[] ipBytes = new byte[4];
            System.arraycopy(data, 1, ipBytes, 0, 4);
            InetAddress signalIp = InetAddress.getByAddress(ipBytes);
            heartbeat.setSignalIp(signalIp);

            return heartbeat;

        } catch (Exception e) {
            throw new DataParseException("解析心跳数据失败", e);
        }
    }

    @Override
    public boolean canParse(ProtocolMessage message) {
        return message.getData().length >= 1 && message.getData()[0] == 0;
    }
}

