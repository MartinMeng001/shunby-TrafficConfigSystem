package com.traffic.config.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

/**
 * 事件总线服务
 * 集中管理所有事件发布
 */
@Service
public class EventBusService {

    private static final Logger logger = LoggerFactory.getLogger(EventBusService.class);

    private final ApplicationEventPublisher eventPublisher;

    // 静态实例，供静态方法访问
    private static EventBusService instance;

    @Autowired
    public EventBusService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        instance = this;
        logger.info("事件总线服务初始化完成");
    }

    /**
     * 发布事件（实例方法）
     */
    public void publish(Object event) {
        try {
            eventPublisher.publishEvent(event);
            //logger.debug("发布事件成功: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("发布事件失败: {}, 错误: {}",
                    event.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * 静态发布方法（供静态方法调用）
     */
    public static void publishStatic(Object event) {
        if (instance != null) {
            instance.publish(event);
        } else {
            logger.error("事件总线未初始化，无法发布事件: {}",
                    event.getClass().getSimpleName());
        }
    }

    /**
     * 检查服务是否就绪
     */
    public static boolean isReady() {
        return instance != null;
    }
}
