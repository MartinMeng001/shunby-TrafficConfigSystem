package com.traffic.config.cardetector.processor;
import com.traffic.config.cardetector.model.VehicleData;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class VehicleDataProcessor implements DataProcessor<VehicleData> {
    private static final Logger log = LoggerFactory.getLogger(VehicleDataProcessor.class);

    @Override
    public void process(VehicleData data) {
        log.info("处理车辆数据: 车牌={}, 车道={}, 方向={}, 排队长度={}m, 速度={}km/h",
                data.getLicensePlate(),
                data.getLaneNumber(),
                data.getDirection().getDescription(),
                data.getQueueLength(),
                data.getSpeed());

        // 这里可以添加车辆数据处理逻辑
        // 比如：
        // 1. 存储到数据库
        // 2. 发送给路段管理器
        // 3. 触发交通控制逻辑
    }
}
