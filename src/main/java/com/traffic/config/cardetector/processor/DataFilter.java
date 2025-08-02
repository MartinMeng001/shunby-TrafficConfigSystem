package com.traffic.config.cardetector.processor;

import com.traffic.config.cardetector.model.VehicleData;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DataFilter {
    private static final Logger log = LoggerFactory.getLogger(DataFilter.class);

    // 重复数据过滤缓存 (车牌号 -> 最后接收时间)
    private final ConcurrentMap<String, LocalDateTime> duplicateCache = new ConcurrentHashMap<>();

    // 重复数据过滤时间窗口 (秒)
    private static final long DUPLICATE_WINDOW_SECONDS = 5;

    public boolean shouldProcessVehicleData(VehicleData data) {
        // 构建唯一键
        String key = buildVehicleKey(data);
        LocalDateTime now = LocalDateTime.now();

        // 检查是否为重复数据
        LocalDateTime lastReceived = duplicateCache.get(key);
        if (lastReceived != null) {
            long seconds = ChronoUnit.SECONDS.between(lastReceived, now);
            if (seconds < DUPLICATE_WINDOW_SECONDS) {
                log.debug("过滤重复车辆数据: {}", key);
                return false;
            }
        }

        // 更新缓存
        duplicateCache.put(key, now);

        // 清理过期缓存
        cleanupExpiredCache(now);

        return true;
    }

    private String buildVehicleKey(VehicleData data) {
        return String.format("%s_%d_%s_%s",
                data.getSignalIp().getHostAddress(),
                data.getLaneNumber(),
                data.getLicensePlate(),
                data.getDirection().name());
    }

    private void cleanupExpiredCache(LocalDateTime now) {
        duplicateCache.entrySet().removeIf(entry -> {
            long seconds = ChronoUnit.SECONDS.between(entry.getValue(), now);
            return seconds > DUPLICATE_WINDOW_SECONDS * 2; // 保留2倍时间窗口
        });
    }
}
