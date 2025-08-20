package com.traffic.config.service.impl;

import com.traffic.config.entity.*;
import com.traffic.config.exception.ConfigException;
import com.traffic.config.service.ConfigService;
import com.traffic.config.service.event.ServerUrlUpdateEvent;
import com.traffic.config.service.event.SignalListEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Value("${traffic.config.file.path:classpath:config.xml}")
    private String configFilePath;

    @Value("${traffic.config.backup.enabled:true}")
    private boolean backupEnabled;

    @Value("${traffic.config.backup.dir:./backup}")
    private String backupDir;

    @Value("${traffic.config.validation.enabled:true}")
    private boolean validationEnabled;

    private SingleLane cachedConfig;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long lastModified = 0;
    private JAXBContext jaxbContext;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired // 注入 TaskScheduler
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void init() {
        try {
            // 初始化JAXB上下文
            jaxbContext = JAXBContext.newInstance(SingleLane.class);

            // 创建备份目录
            if (backupEnabled) {
                createBackupDirectory();
            }

            // 预加载配置
            loadConfig();

            log.info("ConfigService初始化完成, 配置文件路径: {}", configFilePath);

            // *** 延迟发布初始事件 ***
            // 使用 TaskScheduler 安排一个任务在指定延迟后执行
            taskScheduler.schedule(() -> {
                log.info("ConfigService: 延迟 {} 秒后发布初始 ServerUrlUpdateEvent。", 5);
                eventPublisher.publishEvent(new ServerUrlUpdateEvent(this, cachedConfig.getGlobal().getPlatformUrl()));
                eventPublisher.publishEvent(new SignalListEvent(this, cachedConfig.getGlobal().getRegionList()));
            }, new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5)));
        } catch (JAXBException e) {
            log.error("JAXB上下文初始化失败", e);
            throw new ConfigException("CONFIG_INIT_ERROR", "配置服务初始化失败", e);
        }
    }

    @Override
    public SingleLane loadConfig() {
        lock.readLock().lock();
        try {
            File configFile = getConfigFile();

            // 检查文件是否更新
            if (cachedConfig == null || configFile.lastModified() > lastModified) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    // 双重检查
                    if (cachedConfig == null || configFile.lastModified() > lastModified) {
                        log.debug("重新加载配置文件: {}", configFile.getAbsolutePath());
                        cachedConfig = parseConfigFile(configFile);
                        lastModified = configFile.lastModified();

                        // 验证配置
                        if (validationEnabled) {
                            validateConfig(cachedConfig);
                        }

                        log.info("配置文件加载成功, 路段数量: {}, 检测点数量: {}",
                                cachedConfig.getSegments().getSize(), cachedConfig.getDetectPoints().getDetectPointList().size());

                        eventPublisher.publishEvent(new ServerUrlUpdateEvent(this, cachedConfig.getGlobal().getPlatformUrl()));
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            log.info("配置文件加载成功, 路段数量: {}",
                    cachedConfig.getSegments().getSize());
            return cachedConfig;
        } catch (Exception e) {
            log.error("加载配置文件失败: {}", e.getMessage(), e);
            if (e instanceof ConfigException) {
                throw (ConfigException)e;
            }
            throw new ConfigException("CONFIG_LOAD_ERROR", "加载配置文件失败", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void saveConfig(SingleLane config) {
        lock.writeLock().lock();
        try {
            File configFile = getConfigFile();

            // 验证配置
            if (validationEnabled) {
                validateConfig(config);
            }

            // 备份当前配置
            if (backupEnabled && configFile.exists()) {
                createAutoBackup();
            }

            // 保存配置
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(config, configFile);

            // 更新缓存
            cachedConfig = config;
            lastModified = configFile.lastModified();

            log.info("配置文件保存成功: {}", configFile.getAbsolutePath());

        } catch (JAXBException e) {
            log.error("配置文件保存失败: {}", e.getMessage(), e);
            throw ConfigException.saveError(configFilePath, e);
        } catch (Exception e) {
            log.error("配置文件保存失败: {}", e.getMessage(), e);
            if (e instanceof ConfigException) {
                throw (ConfigException)e;
            }
            throw ConfigException.saveError(configFilePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public GlobalConfig getGlobalConfig() {
        //SingleLane config = loadConfig();
        return cachedConfig.getGlobal();
    }

    @Override
    public void updateGlobalConfig(int allRed, int maxAllRed) {
        SingleLane config = loadConfig();
        GlobalConfig globalConfig = config.getGlobal();

        // 验证参数
        if (validationEnabled) {
            validateGlobalParameters(allRed, maxAllRed);
        }

        globalConfig.setAllRed(allRed);
        globalConfig.setMaxAllRed(maxAllRed);

        saveConfig(config);
        log.info("全局配置更新成功: allRed={}, maxAllRed={}", allRed, maxAllRed);
    }

    @Override
    public List<Segment> getAllSegments() {
        //SingleLane config = loadConfig();
        return cachedConfig.getSegments().getSegmentList();
    }

    @Override
    public List<DetectPoint> getAllDetectPoints() {
        return cachedConfig.getDetectPoints().getDetectPointList();
    }

    @Override
    public Optional<Segment> getSegmentBySigid(String sigid) {
        if (!StringUtils.hasText(sigid)) {
            return Optional.empty();
        }

        return getAllSegments().stream()
                .filter(segment -> sigid.equals(segment.getUpsigid()))
                .findFirst();
    }

    @Override
    public Optional<Segment> getSegmentBySegmentId(int segmentId) {
        return getAllSegments().stream()
                .filter(segment -> segment.getSegmentId() == segmentId)
                .findFirst();
    }

    @Override
    public Optional<Segment> getSegmentByName(String name) {
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }

        return getAllSegments().stream()
                .filter(segment -> name.equals(segment.getName()))
                .findFirst();
    }

    @Override
    public Optional<DetectPoint> getDetectPointByIndex(int index) {
        //return Optional.empty();
        return getAllDetectPoints().stream()
                .filter(detectPoint -> index==detectPoint.getIndex())
                .findFirst();
    }

    @Override
    public boolean updateSegment(String sigid, Segment updatedSegment) {
        if (!StringUtils.hasText(sigid) || updatedSegment == null) {
            throw new IllegalArgumentException("信号灯ID和路段配置不能为空");
        }

        // 验证路段配置
        if (validationEnabled) {
            validateSegment(updatedSegment);
        }

        SingleLane config = loadConfig();
        List<Segment> segments = config.getSegments().getSegmentList();

        for (int i = 0; i < segments.size(); i++) {
            if (sigid.equals(segments.get(i).getUpsigid())) {
                segments.set(i, updatedSegment);
                saveConfig(config);
                log.info("路段配置更新成功: sigid={}, name={}", sigid, updatedSegment.getName());
                return true;
            }
        }

        log.warn("路段不存在，更新失败: sigid={}", sigid);
        return false;
    }

    @Override
    public void addSegment(Segment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("路段配置不能为空");
        }

        // 验证路段配置
        if (validationEnabled) {
            validateSegment(segment);
        }

        // 检查是否已存在
        if (getSegmentBySigid(segment.getUpsigid()).isPresent()) {
            throw ConfigException.segmentAlreadyExists(segment.getUpsigid());
        }

        SingleLane config = loadConfig();
        List<Segment> segments = config.getSegments().getSegmentList();
        segments.add(segment);
        config.getSegments().setSize(segments.size());

        saveConfig(config);
        log.info("路段配置添加成功: sigid={}, name={}", segment.getUpsigid(), segment.getName());
    }

    @Override
    public boolean deleteSegment(String sigid) {
        if (!StringUtils.hasText(sigid)) {
            throw new IllegalArgumentException("信号灯ID不能为空");
        }

        SingleLane config = loadConfig();
        List<Segment> segments = config.getSegments().getSegmentList();

        boolean removed = segments.removeIf(segment -> sigid.equals(segment.getUpsigid()));
        if (removed) {
            config.getSegments().setSize(segments.size());
            saveConfig(config);
            log.info("路段配置删除成功: sigid={}", sigid);
        } else {
            log.warn("路段不存在，删除失败: sigid={}", sigid);
        }

        return removed;
    }

    @Override
    public void addDetectPoint(DetectPoint detectPoint) {
        if (detectPoint == null) {
            throw new IllegalArgumentException("DetectPoint配置不能为空");
        }

        // 验证DetectPoint配置
        if (validationEnabled) {
            validateDetectPoint(detectPoint);
        }

        // 检查是否已存在
        if (getDetectPointByIndex(detectPoint.getIndex()).isPresent()) {
            throw ConfigException.detectPointAlreadyExists(detectPoint.getIndex());
        }

        SingleLane config = loadConfig();
        List<DetectPoint> detectPoints = config.getDetectPoints().getDetectPointList();
        detectPoints.add(detectPoint);

        saveConfig(config);
        log.info("DetectPoint配置添加成功: index={}", detectPoint.getIndex());
    }

    @Override
    public boolean updateDetectPoint(int index, DetectPoint updatedDetectPoint) {
        if (updatedDetectPoint == null) {
            throw new IllegalArgumentException("DetectPoint配置不能为空");
        }

        // 验证DetectPoint配置
        if (validationEnabled) {
            validateDetectPoint(updatedDetectPoint);
        }

        SingleLane config = loadConfig();
        List<DetectPoint> detectPoints = config.getDetectPoints().getDetectPointList();

        for (int i = 0; i < detectPoints.size(); i++) {
            if (index == detectPoints.get(i).getIndex()) {
                detectPoints.set(i, updatedDetectPoint);
                saveConfig(config);
                log.info("DetectPoint配置更新成功: index={}", index);
                return true;
            }
        }

        log.warn("DetectPoint不存在，更新失败: index={}", index);
        return false;
    }

    @Override
    public boolean deleteDetectPoint(int index) {
        SingleLane config = loadConfig();
        List<DetectPoint> detectPoints = config.getDetectPoints().getDetectPointList();

        boolean removed = detectPoints.removeIf(detectPoint -> index == detectPoint.getIndex());
        if (removed) {
            saveConfig(config);
            log.info("DetectPoint配置删除成功: index={}", index);
        } else {
            log.warn("DetectPoint不存在，删除失败: index={}", index);
        }

        return removed;
    }

    @Override
    public void validateSegment(Segment segment) {
        if (segment == null) {
            throw ConfigException.validationError("segment", null);
        }

        // 验证必填字段
        if (!StringUtils.hasText(segment.getName())) {
            throw ConfigException.validationError("name", segment.getName());
        }

        if (!StringUtils.hasText(segment.getUpsigid())) {
            throw ConfigException.validationError("sigid", segment.getUpsigid());
        }

    }

    @Override
    public void validateGlobalConfig(GlobalConfig globalConfig) {
        if (globalConfig == null) {
            throw ConfigException.validationError("globalConfig", null);
        }

        validateGlobalParameters(globalConfig.getAllRed(), globalConfig.getMaxAllRed());
    }
    @Override
    public void validateDetectPoint(DetectPoint detectPoint) {
        if (detectPoint == null) {
            throw ConfigException.validationError("detectPoint", null);
        }

        // 验证必填字段和数值范围
        if (detectPoint.getIndex() <= 0) {
            throw ConfigException.validationError("detectPoint.index", detectPoint.getIndex());
        }

    }
    @Override
    public void refreshCache() {
        lock.writeLock().lock();
        try {
            cachedConfig = null;
            lastModified = 0;
            log.info("配置缓存已刷新");
        } finally {
            lock.writeLock().unlock();
        }

        // 重新加载配置
        loadConfig();
    }

    @Override
    public boolean isConfigFileExists() {
        try {
            File configFile = getConfigFile();
            return configFile.exists();
        } catch (Exception e) {
            log.warn("检查配置文件存在性失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidConfig(){
        if(isConfigFileExists()==false) return false;
        try{
            validateConfig(cachedConfig);
            return true;
        }catch(Exception e){
            log.warn("检查配置文件参数有效性失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getConfigLastModified() {
        try {
            File configFile = getConfigFile();
            return configFile.exists() ? configFile.lastModified() : 0;
        } catch (Exception e) {
            log.warn("获取配置文件修改时间失败: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public void backupConfig(String backupPath) {
        if (!StringUtils.hasText(backupPath)) {
            throw new IllegalArgumentException("备份路径不能为空");
        }

        try {
            File configFile = getConfigFile();
            Path sourcePath = configFile.toPath();
            Path targetPath = Paths.get(backupPath);

            // 确保备份目录存在
            Files.createDirectories(targetPath.getParent());

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("配置文件备份成功: {} -> {}", sourcePath, targetPath);

        } catch (Exception e) {
            log.error("配置文件备份失败: {}", e.getMessage(), e);
            throw new ConfigException("CONFIG_BACKUP_ERROR", "配置文件备份失败", e);
        }
    }

    @Override
    public void restoreConfig(String backupPath) {
        if (!StringUtils.hasText(backupPath)) {
            throw new IllegalArgumentException("备份路径不能为空");
        }

        try {
            File configFile = getConfigFile();
            Path sourcePath = Paths.get(backupPath);
            Path targetPath = configFile.toPath();

            if (!Files.exists(sourcePath)) {
                throw new ConfigException("BACKUP_FILE_NOT_FOUND", "备份文件不存在: " + backupPath);
            }

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 刷新缓存
            refreshCache();

            log.info("配置文件恢复成功: {} -> {}", sourcePath, targetPath);

        } catch (Exception e) {
            log.error("配置文件恢复失败: {}", e.getMessage(), e);
            if (e instanceof ConfigException) {
                throw (ConfigException)e;
            }
            throw new ConfigException("CONFIG_RESTORE_ERROR", "配置文件恢复失败", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private File getConfigFile() throws FileNotFoundException {
        if (configFilePath.startsWith("classpath:")) {
            return ResourceUtils.getFile(configFilePath);
        } else {
            return new File(configFilePath);
        }
    }

    private SingleLane parseConfigFile(File configFile) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (SingleLane) unmarshaller.unmarshal(configFile);
    }

    private void validateConfig(SingleLane config) {
        if (config == null) {
            throw ConfigException.validationError("config", null);
        }

        // 验证全局配置
        validateGlobalConfig(config.getGlobal());

        // 验证路段配置
        if (config.getSegments() != null && config.getSegments().getSegmentList() != null) {
            for (Segment segment : config.getSegments().getSegmentList()) {
                validateSegment(segment);
            }
        }
        // --- 新增代码，用于验证 DetectPoint ---
        if (config.getDetectPoints() != null && config.getDetectPoints().getDetectPointList() != null) {
            for (DetectPoint detectPoint : config.getDetectPoints().getDetectPointList()) {
                validateDetectPoint(detectPoint);
            }
        }
    }

    private void validateGlobalParameters(int allRed, int maxAllRed) {
        if (allRed < 0) {
            throw ConfigException.validationError("allRed", allRed);
        }

        if (maxAllRed < 0) {
            throw ConfigException.validationError("maxAllRed", maxAllRed);
        }

        if (allRed > maxAllRed) {
            throw ConfigException.validationError("allRed > maxAllRed",
                    "allRed(" + allRed + ") > maxAllRed(" + maxAllRed + ")");
        }
    }

    private void createBackupDirectory() {
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                log.info("备份目录创建成功: {}", backupPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.warn("备份目录创建失败: {}", e.getMessage());
        }
    }

    private void createAutoBackup() {
        if (!backupEnabled) {
            return;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "config_backup_" + timestamp + ".xml";
            String backupFilePath = backupDir + File.separator + backupFileName;

            backupConfig(backupFilePath);

        } catch (Exception e) {
            log.warn("自动备份失败: {}", e.getMessage());
        }
    }
}