package com.traffic.config.statemachinev3.threading;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@Profile("!statemachine-v2")
public class StateMachineV3ThreadingConfig {

    /**
     * 顶层系统状态机专用线程池
     */
    @Bean(name = "systemStateMachineV3Executor")
    @Primary
    public ThreadPoolTaskExecutor systemStateMachineV3Executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);           // 比V2增加1个核心线程
        executor.setMaxPoolSize(6);            // 比V2增加2个最大线程（支持多路段）
        executor.setQueueCapacity(200);        // 增大队列容量（支持更多事件）
        executor.setThreadNamePrefix("SystemV3-");
        executor.setKeepAliveSeconds(60);

        executor.setRejectedExecutionHandler((r, executor1) -> {
            LoggerFactory.getLogger(StateMachineV3ThreadingConfig.class)
                    .warn("系统状态机V3线程池拒绝任务: {}", r.toString());
        });

        executor.initialize();
        return executor;
    }

    /**
     * 路段状态机专用线程池
     */
    @Bean(name = "segmentStateMachineV3Executor")
    public ThreadPoolTaskExecutor segmentStateMachineV3Executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);           // 支持5个路段并发
        executor.setMaxPoolSize(10);           // 峰值时支持10个路段
        executor.setQueueCapacity(500);        // 大容量队列支持多路段事件
        executor.setThreadNamePrefix("SegmentV3-");
        executor.setKeepAliveSeconds(60);

        executor.setRejectedExecutionHandler((r, executor1) -> {
            LoggerFactory.getLogger(StateMachineV3ThreadingConfig.class)
                    .warn("路段状态机V3线程池拒绝任务: {}", r.toString());
        });

        executor.initialize();
        return executor;
    }

    /**
     * 清空决策引擎线程池（计算密集型）
     */
    @Bean(name = "clearanceDecisionV3Executor")
    public ThreadPoolTaskExecutor clearanceDecisionV3Executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 基于CPU核心数配置
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ClearanceV3-");
        executor.setKeepAliveSeconds(30);

        executor.setRejectedExecutionHandler((r, executor1) -> {
            LoggerFactory.getLogger(StateMachineV3ThreadingConfig.class)
                    .warn("清空决策V3线程池拒绝任务: {}", r.toString());
        });

        executor.initialize();
        return executor;
    }
}
