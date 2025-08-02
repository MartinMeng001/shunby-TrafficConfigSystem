package com.traffic.config.cardetector.processor;

import com.traffic.config.cardetector.model.HeartbeatData;
import com.traffic.config.common.events.HeartbeatEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EnhancedHeartbeatProcessor implements DataProcessor<HeartbeatData> {
    private static final Logger log = LoggerFactory.getLogger(EnhancedHeartbeatProcessor.class);

    @Autowired
    private DataValidator dataValidator;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void process(HeartbeatData data) {
        try {
            // 数据校验
            if (!dataValidator.validateHeartbeatData(data)) {
                log.warn("心跳数据校验失败");
                return;
            }

            // 记录日志
            log.debug("处理心跳数据: 信号机IP={}", data.getSignalIp().getHostAddress());

            // 发布心跳事件
            publishHeartbeatEvent(data);

        } catch (Exception e) {
            log.error("处理心跳数据时发生错误", e);
        }
    }

    private void publishHeartbeatEvent(HeartbeatData data) {
        try {
            HeartbeatEvent event = new HeartbeatEvent(this, data);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("发布心跳事件失败", e);
        }
    }
}

