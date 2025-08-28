package com.traffic.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据库日志系统自动配置类
 * 注意：不再创建SystemLogUtil的Bean，因为它已经通过@Component注解自动注册
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({SystemLogProperties.class, DataDirectoryProperties.class})
@ConditionalOnProperty(name = "traffic.log.database.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseLogAutoConfiguration {

    /**
     * 配置类初始化完成日志
     */
    public DatabaseLogAutoConfiguration() {
        log.info("数据库日志系统配置加载完成");
    }
}