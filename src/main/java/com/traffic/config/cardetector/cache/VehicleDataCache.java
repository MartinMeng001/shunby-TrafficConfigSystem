package com.traffic.config.cardetector.cache;

import com.traffic.config.cardetector.model.VehicleData;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class VehicleDataCache implements DataCache<VehicleData> {

    private final ConcurrentMap<String, VehicleData> cache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<VehicleData> recentData = new ConcurrentLinkedQueue<>();

    // 最大缓存数量
    private static final int MAX_RECENT_DATA = 1000;

    @Override
    public void put(String key, VehicleData data) {
        cache.put(key, data);

        // 添加到最近数据队列
        recentData.offer(data);

        // 保持队列大小
        while (recentData.size() > MAX_RECENT_DATA) {
            recentData.poll();
        }
    }

    @Override
    public VehicleData get(String key) {
        return cache.get(key);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
        recentData.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    // 获取指定车道的最近车辆数据
    public List<VehicleData> getRecentDataByLane(int laneNumber) {
        return recentData.stream()
                .filter(data -> data.getLaneNumber() == laneNumber)
                .collect(Collectors.toList());
    }

    // 获取指定时间范围内的数据
    public List<VehicleData> getDataSince(LocalDateTime since) {
        return recentData.stream()
                .filter(data -> data.getTimestamp().isAfter(since))
                .collect(Collectors.toList());
    }
}

