package com.traffic.config.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 数据目录配置属性类
 * 对应application.yml中的traffic.data配置
 *
 * @author System
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "traffic.data")
public class DataDirectoryProperties {

    /**
     * 主数据目录
     */
    private String dir = "./data";

    /**
     * 备份目录配置
     */
    private Backup backup = new Backup();

    /**
     * 日志目录配置
     */
    private Logs logs = new Logs();

    @Data
    public static class Backup {
        /**
         * 备份目录路径
         */
        private String dir = "./data/backup";
    }

    @Data
    public static class Logs {
        /**
         * 日志目录路径
         */
        private String dir = "./logs";
    }
}