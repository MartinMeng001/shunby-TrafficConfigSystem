package com.traffic.config.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    /**
     * 定义一个用于事件处理的 ThreadPoolTaskExecutor。
     * 配置核心线程数为5，最大线程数为10，队列容量为100。
     * 线程名前缀为 "EventHandlerThread-"，便于调试。
     */
    @Bean(name = "platformEventHandlerThreadPool")
    public ThreadPoolTaskExecutor eventHandlerThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 核心线程数
        executor.setMaxPoolSize(10); // 最大线程数
        executor.setQueueCapacity(100); // 队列容量
        executor.setThreadNamePrefix("PlatformEventHandlerThread-"); // 线程名前缀
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(60); // 等待60秒
        executor.initialize();
        return executor;
    }
}
