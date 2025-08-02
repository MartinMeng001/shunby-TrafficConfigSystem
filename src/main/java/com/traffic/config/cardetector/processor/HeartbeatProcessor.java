package com.traffic.config.cardetector.processor;

import com.traffic.config.cardetector.model.HeartbeatData;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class HeartbeatProcessor implements DataProcessor<HeartbeatData> {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatProcessor.class);

    @Override
    public void process(HeartbeatData data) {
        log.debug("处理心跳数据: 信号机IP={}", data.getSignalIp());
        // 这里可以添加心跳处理逻辑，比如更新连接状态等
    }
}
