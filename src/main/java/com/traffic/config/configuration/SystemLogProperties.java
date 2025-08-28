package com.traffic.config.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 系统日志配置属性类
 * 对应application.yml中的traffic.log配置
 *
 * @author System
 * @version 1.0.0
 */
@Data
@Primary
@Component
@ConfigurationProperties(prefix = "traffic.log")
public class SystemLogProperties {

    /**
     * 数据库日志配置
     */
    private Database database = new Database();

    /**
     * 日志级别配置
     */
    private Level level = new Level();

    @Data
    public static class Database {
        /**
         * 是否启用数据库日志
         */
        private boolean enabled = true;

        /**
         * 清理配置
         */
        private Cleanup cleanup = new Cleanup();

        @Data
        public static class Cleanup {
            /**
             * 是否启用自动清理
             */
            private boolean enabled = true;

            /**
             * 普通日志保留天数
             */
            private int normalRetainDays = 30;

            /**
             * 错误日志保留天数
             */
            private int errorRetainDays = 90;

            /**
             * 清理任务执行时间表达式
             */
            private String cron = "0 0 2 * * ?";
        }
    }

    @Data
    public static class Level {
        /**
         * 车辆事件日志级别
         */
        private String vehicle = "INFO";

        /**
         * 路段状态日志级别
         */
        private String segment = "INFO";

        /**
         * 冲突告警日志级别
         */
        private String conflict = "WARN";

        /**
         * 检测器错误日志级别
         */
        private String detector = "ERROR";

        /**
         * 传感器状态日志级别
         */
        private String sensor = "INFO";

        /**
         * 系统事件日志级别
         */
        private String system = "INFO";
    }
}