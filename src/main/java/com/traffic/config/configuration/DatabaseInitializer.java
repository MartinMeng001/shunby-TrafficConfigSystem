package com.traffic.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 数据库初始化配置
 * 确保数据目录存在
 *
 * @author System
 * @version 1.0.0
 */
@Slf4j
@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Value("${traffic.data.dir:./data}")
    private String dataDir;

    @Value("${traffic.data.backup.dir:./data/backup}")
    private String backupDir;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeDirectories();
        log.info("数据库初始化完成");
    }

    private void initializeDirectories() {
        createDirectoryIfNotExists(dataDir);
        createDirectoryIfNotExists(backupDir);
    }

    private void createDirectoryIfNotExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建数据目录: {}", path.toAbsolutePath());
            } else {
                log.debug("数据目录已存在: {}", path.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("创建数据目录失败: {}", dirPath, e);
            throw new RuntimeException("无法创建数据目录: " + dirPath, e);
        }
    }
}