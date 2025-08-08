package com.traffic.config.statemachinev2.threading;

import com.traffic.config.statemachinev2.core.SystemStateMachine;
import com.traffic.config.statemachinev2.enums.SystemEventV2;
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
@Profile("statemachine-v2")
public class SpringBasedStateMachineService {

    private static final Logger logger = LoggerFactory.getLogger(SpringBasedStateMachineService.class);

    @Autowired
    private SystemStateMachine stateMachine;

    @PostConstruct
    public void init() {
        // 启动状态机
        stateMachine.start();
        logger.info("Spring状态机服务已启动");
    }

    /**
     * 定时触发timer_tick事件（每秒一次）
     */
    @Scheduled(fixedRate = 1000)
    public void timerTick() {
        try {
            processEventAsync(SystemEventV2.TIMER_TICK);
        } catch (Exception e) {
            logger.error("定时器滴答异常", e);
        }
    }

    /**
     * 异步处理事件
     */
    @Async("stateMachineExecutor")
    public CompletableFuture<Boolean> processEventAsync(SystemEventV2 event) {
        try {
            boolean result = stateMachine.processEvent(event);
            logger.debug("异步处理事件: {} - {}", event.getChineseName(), result ? "成功" : "失败");
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("异步事件处理异常: {}", event.getChineseName(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 同步处理紧急事件
     */
    public boolean processEventSync(SystemEventV2 event) {
        if (event.isCritical()) {
            logger.info("同步处理紧急事件: {}", event.getChineseName());
            return stateMachine.processEvent(event);
        } else {
            // 非紧急事件异步处理
            processEventAsync(event);
            return true;
        }
    }

    @PreDestroy
    public void destroy() {
        stateMachine.stop();
        logger.info("Spring状态机服务已停止");
    }
}
