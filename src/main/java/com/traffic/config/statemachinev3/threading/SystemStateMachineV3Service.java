package com.traffic.config.statemachinev3.threading;

import com.traffic.config.statemachinev3.core.TopLevelStateMachine;
import com.traffic.config.statemachinev3.enums.system.SystemEventV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;

@Service
@Profile("!statemachine-v2")
public class SystemStateMachineV3Service {

    private static final Logger logger = LoggerFactory.getLogger(SystemStateMachineV3Service.class);

    @Autowired
    private TopLevelStateMachine topLevelStateMachine;

    @PostConstruct
    public void init() {
        // 启动顶层状态机
        topLevelStateMachine.start();
        logger.info("StateMachine V3系统服务已启动");
    }

    /**
     * 定时触发系统心跳事件（每500ms一次，比V2更频繁）
     */
    @Scheduled(fixedRate = 500)
    public void systemHeartbeat() {
        try {
            topLevelStateMachine.processTimerTick();  // 直接调用
        } catch (Exception e) {
            logger.error("系统心跳异常", e);
        }
    }

    /**
     * 定时检查系统健康状态（每5秒一次）
     */
    @Scheduled(fixedRate = 5000)
    public void healthCheck() {
        try {
            processSystemEventAsync(SystemEventV3.HEALTH_SCORE_UPDATE);
        } catch (Exception e) {
            logger.error("健康检查异常", e);
        }
    }

    /**
     * 异步处理系统事件
     */
    public CompletableFuture<Boolean> processSystemEventAsync(SystemEventV3 event) {
        try {
            // 发送事件到状态机的内部队列
            topLevelStateMachine.postEvent(event, null);

            //logger.debug("异步发送系统事件: {}", event.getChineseName());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("异步系统事件发送异常: {}", event.getChineseName(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 同步处理关键系统事件
     */
    public boolean processSystemEventSync(SystemEventV3 event) {
        if (event.isCritical()) {
            logger.info("同步处理关键系统事件: {}", event.getChineseName());
            return topLevelStateMachine.processEvent(event, null);
        } else {
            // 非关键事件异步处理
            processSystemEventAsync(event);
            return true;
        }
    }

    /**
     * 批量处理系统事件
     */
    @Async("systemStateMachineV3Executor")
    public CompletableFuture<Integer> processSystemEventsBatch(SystemEventV3[] events) {
        int successCount = 0;
        for (SystemEventV3 event : events) {
            try {
                if (topLevelStateMachine.processEvent(event, null)) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("批量处理事件失败: {}", event.getChineseName(), e);
            }
        }

        logger.info("批量处理完成 - 总计: {}, 成功: {}", events.length, successCount);
        return CompletableFuture.completedFuture(successCount);
    }

    @PreDestroy
    public void destroy() {
        topLevelStateMachine.stop();
        logger.info("StateMachine V3系统服务已停止");
    }
}
