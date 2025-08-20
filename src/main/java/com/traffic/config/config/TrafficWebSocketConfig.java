package com.traffic.config.config;

import com.traffic.config.websocket.handler.TrafficWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 配置交通系统信息发布的WebSocket端点
 */
@Configuration
@EnableWebSocket
public class TrafficWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TrafficWebSocketHandler trafficWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器
        registry.addHandler(trafficWebSocketHandler, "/ws/traffic")
                .setAllowedOrigins("*"); // 允许跨域访问，生产环境应该配置具体域名
                //.withSockJS(); // 支持SockJS作为WebSocket的备选方案
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("TaskScheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
