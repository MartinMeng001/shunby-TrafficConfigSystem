package com.traffic.config.service;

import com.traffic.config.entity.GlobalConfig;
import com.traffic.config.entity.Segment;
import com.traffic.config.entity.SingleLane;

import java.util.List;
import java.util.Optional;

public interface ConfigService {

    /**
     * 加载配置文件并缓存
     *
     * @return 单车道配置对象
     * @throws ConfigException 配置加载失败时抛出
     */
    SingleLane loadConfig();

    /**
     * 保存配置到文件
     *
     * @param config 配置对象
     * @throws ConfigException 配置保存失败时抛出
     */
    void saveConfig(SingleLane config);

    /**
     * 获取全局配置
     *
     * @return 全局配置对象
     */
    GlobalConfig getGlobalConfig();

    /**
     * 更新全局配置
     *
     * @param allRed 全红时间
     * @param maxAllRed 最大全红时间
     */
    void updateGlobalConfig(int allRed, int maxAllRed);

    /**
     * 获取所有路段配置
     *
     * @return 路段配置列表
     */
    List<Segment> getAllSegments();

    /**
     * 根据信号灯ID获取路段配置
     *
     * @param sigid 信号灯ID
     * @return 路段配置，如果不存在则返回Optional.empty()
     */
    Optional<Segment> getSegmentBySigid(String sigid);

    /**
     * 根据名称获取路段配置
     *
     * @param name 路段名称
     * @return 路段配置，如果不存在则返回Optional.empty()
     */
    Optional<Segment> getSegmentByName(String name);

    /**
     * 更新路段配置
     *
     * @param sigid 信号灯ID
     * @param updatedSegment 更新后的路段配置
     * @return 是否更新成功
     */
    boolean updateSegment(String sigid, Segment updatedSegment);

    /**
     * 添加路段配置
     *
     * @param segment 新的路段配置
     * @throws ConfigException 路段已存在或添加失败时抛出
     */
    void addSegment(Segment segment);

    /**
     * 删除路段配置
     *
     * @param sigid 信号灯ID
     * @return 是否删除成功
     */
    boolean deleteSegment(String sigid);

    /**
     * 验证路段配置
     *
     * @param segment 路段配置
     * @throws ConfigException 验证失败时抛出
     */
    void validateSegment(Segment segment);

    /**
     * 验证全局配置
     *
     * @param globalConfig 全局配置
     * @throws ConfigException 验证失败时抛出
     */
    void validateGlobalConfig(GlobalConfig globalConfig);

    /**
     * 强制刷新配置缓存
     */
    void refreshCache();

    /**
     * 检查配置文件是否存在
     *
     * @return 配置文件是否存在
     */
    boolean isConfigFileExists();

    /**
     * 获取配置文件最后修改时间
     *
     * @return 最后修改时间戳
     */
    long getConfigLastModified();

    /**
     * 备份当前配置
     *
     * @param backupPath 备份文件路径
     * @throws ConfigException 备份失败时抛出
     */
    void backupConfig(String backupPath);

    /**
     * 从备份恢复配置
     *
     * @param backupPath 备份文件路径
     * @throws ConfigException 恢复失败时抛出
     */
    void restoreConfig(String backupPath);
}