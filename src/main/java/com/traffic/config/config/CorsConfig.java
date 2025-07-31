package com.traffic.config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 允许所有路径
                        .allowedOrigins(
                                "http://localhost:3001",
                                "http://127.0.0.1:8080") // 允许来自这个源的请求
                        // .allowedOrigins("*") // 允许所有源 (开发环境可以这样，生产环境慎用)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                        .allowedHeaders("*") // 允许所有请求头
                        .allowCredentials(true) // 允许发送Cookie
                        .maxAge(3600); // 预检请求的缓存时间
            }
        };
    }
}