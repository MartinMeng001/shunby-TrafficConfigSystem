package com.traffic.config.service;

import com.traffic.config.entity.*;
import com.traffic.config.exception.ConfigException;

import java.util.List;
import java.util.Optional;

public interface ConfigService {

    /**
     * 加载配置文件并缓存
     *
     * @return 单车道配置对象
     * @throws ConfigException 配置加载失败时抛出
     */
    SingleLane loadConfig() throws ConfigException;

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
     * 获取所有检测点配置
     *
     * @return 路段配置列表
     */
    List<DetectPoint> getAllDetectPoints();

    List<WaitingArea> getAllWaitingAreas();

    /**
     * 根据信号灯ID获取路段配置
     *
     * @param sigid 信号灯ID
     * @return 路段配置，如果不存在则返回Optional.empty()
     */
    Optional<Segment> getSegmentBySigid(String sigid);

    /**
     * 根据路段ID获取路段配置
     *
     * @param segmentId 路段ID
     * @return 路段配置，如果不存在则返回Optional.empty()
     */
    Optional<Segment> getSegmentBySegmentId(int segmentId);

    /**
     * 根据名称获取路段配置
     *
     * @param name 路段名称
     * @return 路段配置，如果不存在则返回Optional.empty()
     */
    Optional<Segment> getSegmentByName(String name);

    /**
     * 根据索引获取检测点配置
     *
     * @param index 检测点编号
     * @return 路段配置，如果不存在则返回Optional.empty()
     */
    Optional<DetectPoint> getDetectPointByIndex(int index);

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
     * 添加一个DetectPoint配置
     * @param detectPoint 要添加的DetectPoint对象
     */
    void addDetectPoint(DetectPoint detectPoint);

    /**
     * 更新一个DetectPoint配置
     * @param index 要更新的DetectPoint的索引
     * @param updatedDetectPoint 包含更新信息的DetectPoint对象
     * @return 如果更新成功，返回true；否则返回false
     */
    boolean updateDetectPoint(int index, DetectPoint updatedDetectPoint);

    /**
     * 删除一个DetectPoint配置
     * @param index 要删除的DetectPoint的索引
     * @return 如果删除成功，返回true；否则返回false
     */
    boolean deleteDetectPoint(int index);
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
     * 验证车辆检测配置
     *
     * @param detectPoint 车辆检测配置
     * @throws ConfigException 验证失败时抛出
     */
    void validateDetectPoint(DetectPoint detectPoint);
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
     * 检查配置文件参数是否有效
     *
     * @return 配置文件参数是否有效
     */
    boolean isValidConfig();

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