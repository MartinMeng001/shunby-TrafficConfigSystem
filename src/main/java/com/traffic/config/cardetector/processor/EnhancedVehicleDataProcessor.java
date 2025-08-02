package com.traffic.config.cardetector.processor;

import com.traffic.config.cardetector.cache.VehicleDataCache;
import com.traffic.config.cardetector.model.VehicleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EnhancedVehicleDataProcessor implements DataProcessor<VehicleData> {
    private static final Logger log = LoggerFactory.getLogger(EnhancedVehicleDataProcessor.class);

    @Autowired
    private DataValidator dataValidator;

    @Autowired
    private DataFilter dataFilter;

    @Autowired
    private VehicleDataCache vehicleDataCache;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void process(VehicleData data) {
        try {
            // 数据校验
            if (!dataValidator.validateVehicleData(data)) {
                log.warn("车辆数据校验失败，丢弃数据: {}", data.getLicensePlate());
                return;
            }

            // 数据过滤
            if (!dataFilter.shouldProcessVehicleData(data)) {
                log.debug("车辆数据被过滤器拒绝: {}", data.getLicensePlate());
                return;
            }

            // 缓存数据
            String cacheKey = buildCacheKey(data);
            vehicleDataCache.put(cacheKey, data);

            // 记录日志
            log.info("处理车辆数据: 车牌={}, 信号机={}, 车道={}, 方向={}, 排队长度={}m, 速度={}km/h",
                    data.getLicensePlate(),
                    data.getSignalIp().getHostAddress(),
                    data.getLaneNumber(),
                    data.getDirection().getDescription(),
                    data.getQueueLength(),
                    data.getSpeed());

            // 发布事件给其他模块处理
            //publishVehicleDataEvent(data);

        } catch (Exception e) {
            log.error("处理车辆数据时发生错误", e);
        }
    }

    private String buildCacheKey(VehicleData data) {
        return String.format("%s_%d_%s_%d",
                data.getSignalIp().getHostAddress(),
                data.getLaneNumber(),
                data.getLicensePlate(),
                data.getTimestamp().getNano());
    }

//    private void publishVehicleDataEvent(VehicleData data) {
//        try {
//            VehicleDataEvent event = new VehicleDataEvent(this, data);
//            eventPublisher.publishEvent(event);
//        } catch (Exception e) {
//            log.error("发布车辆数据事件失败", e);
//        }
//    }
}

