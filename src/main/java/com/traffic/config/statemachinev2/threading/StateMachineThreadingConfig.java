package com.traffic.config.statemachinev2.threading;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@Profile("statemachine-v2")
public class StateMachineThreadingConfig {

    /**
     * 状态机专用线程池
     */
    @Bean(name = "stateMachineExecutor")
    public ThreadPoolTaskExecutor stateMachineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // 核心线程数
        executor.setMaxPoolSize(4);            // 最大线程数
        executor.setQueueCapacity(100);        // 队列容量
        executor.setThreadNamePrefix("StateMachine-");
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：记录日志并丢弃
        executor.setRejectedExecutionHandler((r, executor1) -> {
            LoggerFactory.getLogger(StateMachineThreadingConfig.class)
                    .warn("状态机线程池拒绝任务: {}", r.toString());
        });

        executor.initialize();
        return executor;
    }
}
