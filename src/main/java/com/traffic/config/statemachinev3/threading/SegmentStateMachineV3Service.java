package com.traffic.config.statemachinev3.threading;

import com.traffic.config.statemachinev3.core.SegmentStateMachine;
import com.traffic.config.statemachinev3.core.TopLevelStateMachine;
import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class SegmentStateMachineV3Service {

    private static final Logger logger = LoggerFactory.getLogger(SegmentStateMachineV3Service.class);

    @Autowired
    private TopLevelStateMachine topLevelStateMachine;

    /**
     * 注册路段状态机
     */
//    public void registerSegmentStateMachine(int segmentId, SegmentStateMachine segmentMachine) {
//        segmentMachines.put(segmentId, segmentMachine);
//        logger.info("注册路段状态机: {}", segmentId);
//    }

    /**
     * 异步处理路段事件
     */
    public CompletableFuture<Boolean> processSegmentEventAsync(int segmentId, SegmentEvent event, Map<String, Object> eventData) {
        try {
            SegmentStateMachine segmentMachine = topLevelStateMachine.getSegmentStateMachine(segmentId);
            if (segmentMachine == null) {
                logger.warn("路段状态机不存在: {}", segmentId);
                return CompletableFuture.completedFuture(false);
            }
            segmentMachine.postEvent(event, eventData);

            logger.debug("异步发送路段事件: 路段{} - {}",
                    segmentId, event.getChineseName());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("异步路段事件发送异常: 路段{} - {}", segmentId, event.getChineseName(), e);
            return CompletableFuture.completedFuture(false);
        }
    }
    /*
     * 同步处理关键系统事件
     */
    public boolean processSegmentEventSync(int segmentId, SegmentEvent event, Map<String, Object> eventData) {
//        if(event.isCritical()){
            logger.info("同步处理关键路段事件: {}-{}", segmentId, event.getChineseName());
            SegmentStateMachine segmentMachine = topLevelStateMachine.getSegmentStateMachine(segmentId);
            if (segmentMachine == null) {
                logger.warn("路段状态机不存在: {}", segmentId);
                return false;
            }
            return segmentMachine.processSegmentEvent(event, eventData);
//        }else{
//            processSegmentEventAsync(segmentId, event, eventData);
//            return true;
//        }
    }

    /**
     * 广播事件到所有路段
     */
    public CompletableFuture<Integer> broadcastEventToAllSegments(SegmentEvent event, Map<String, Object> eventData) {
        int successCount = 0;

        // 通过TopLevelStateMachine获取所有路段状态机
        for (SegmentStateMachine segment : topLevelStateMachine.getSegmentStateMachines()) {
            try {
                if (segment.processSegmentEvent(event, eventData)) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("广播事件失败: 路段{} - {}", segment.getSegmentId(), event.getChineseName(), e);
            }
        }

        logger.info("广播事件完成: {} - 成功路段数: {}/{}",
                event.getChineseName(), successCount, topLevelStateMachine.getSegmentStateMachines().size());
        return CompletableFuture.completedFuture(successCount);
    }
}
