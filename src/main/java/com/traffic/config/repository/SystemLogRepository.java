package com.traffic.config.repository;

import com.traffic.config.entity.log.SystemLogEntity;
import com.traffic.config.entity.log.SystemLogEntity.LogType;
import com.traffic.config.entity.log.SystemLogEntity.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志数据访问层
 *
 * @author System
 * @version 1.0.0
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLogEntity, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据日志类型查询
     */
    List<SystemLogEntity> findByLogType(LogType logType);

    /**
     * 根据日志级别查询
     */
    List<SystemLogEntity> findByLogLevel(LogLevel logLevel);

    /**
     * 根据日志类型和级别查询
     */
    List<SystemLogEntity> findByLogTypeAndLogLevel(LogType logType, LogLevel logLevel);

    /**
     * 根据时间范围查询
     */
    List<SystemLogEntity> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据实体ID查询
     */
    List<SystemLogEntity> findByEntityId(String entityId);

    /**
     * 根据模块查询
     */
    List<SystemLogEntity> findByModule(String module);

    // ==================== 分页查询方法 ====================

    /**
     * 根据日志类型分页查询
     */
    Page<SystemLogEntity> findByLogType(LogType logType, Pageable pageable);

    /**
     * 根据日志级别分页查询
     */
    Page<SystemLogEntity> findByLogLevel(LogLevel logLevel, Pageable pageable);

    /**
     * 根据时间范围分页查询
     */
    Page<SystemLogEntity> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // ==================== 复合查询方法 ====================

    /**
     * 多条件查询
     */
    @Query("SELECT l FROM SystemLogEntity l WHERE " +
            "(:logType IS NULL OR l.logType = :logType) AND " +
            "(:logLevel IS NULL OR l.logLevel = :logLevel) AND " +
            "(:startTime IS NULL OR l.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR l.createTime <= :endTime) AND " +
            "(:module IS NULL OR l.module = :module) AND " +
            "(:entityId IS NULL OR l.entityId = :entityId) " +
            "ORDER BY l.createTime DESC")
    Page<SystemLogEntity> findByConditions(
            @Param("logType") LogType logType,
            @Param("logLevel") LogLevel logLevel,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("module") String module,
            @Param("entityId") String entityId,
            Pageable pageable);

    /**
     * 内容模糊搜索
     */
    @Query("SELECT l FROM SystemLogEntity l WHERE l.logContent LIKE %:keyword% ORDER BY l.createTime DESC")
    Page<SystemLogEntity> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    // ==================== 统计查询方法 ====================

    /**
     * 统计各类型日志数量
     */
    @Query("SELECT l.logType, COUNT(l) FROM SystemLogEntity l GROUP BY l.logType")
    List<Object[]> countByLogType();

    /**
     * 统计各级别日志数量
     */
    @Query("SELECT l.logLevel, COUNT(l) FROM SystemLogEntity l GROUP BY l.logLevel")
    List<Object[]> countByLogLevel();

    /**
     * 统计指定时间范围内的日志数量
     */
    @Query("SELECT COUNT(l) FROM SystemLogEntity l WHERE l.createTime BETWEEN :startTime AND :endTime")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 统计错误级别日志数量
     */
    @Query("SELECT COUNT(l) FROM SystemLogEntity l WHERE l.logLevel = 'ERROR'")
    long countErrorLogs();

    /**
     * 统计警告级别日志数量
     */
    @Query("SELECT COUNT(l) FROM SystemLogEntity l WHERE l.logLevel = 'WARN'")
    long countWarnLogs();

    /**
     * 获取最近N条日志
     */
    @Query("SELECT l FROM SystemLogEntity l ORDER BY l.createTime DESC")
    List<SystemLogEntity> findTopNByOrderByCreateTimeDesc(Pageable pageable);

    // ==================== 删除方法 ====================

    /**
     * 删除指定时间之前的日志
     */
    @Modifying
    @Query("DELETE FROM SystemLogEntity l WHERE l.createTime < :cutoffTime")
    int deleteByCreateTimeBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 删除指定类型的日志
     */
    @Modifying
    @Query("DELETE FROM SystemLogEntity l WHERE l.logType = :logType")
    int deleteByLogType(@Param("logType") LogType logType);

    /**
     * 删除指定级别的日志
     */
    @Modifying
    @Query("DELETE FROM SystemLogEntity l WHERE l.logLevel = :logLevel")
    int deleteByLogLevel(@Param("logLevel") LogLevel logLevel);

    // ==================== 高级查询方法 ====================

    /**
     * 查询系统健康状态相关日志
     */
    @Query("SELECT l FROM SystemLogEntity l WHERE " +
            "l.logType IN ('SYSTEM_EVENT', 'DETECTOR_ERROR', 'SENSOR_STATUS') AND " +
            "l.logLevel IN ('WARN', 'ERROR') AND " +
            "l.createTime >= :startTime " +
            "ORDER BY l.createTime DESC")
    List<SystemLogEntity> findSystemHealthLogs(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询活跃告警日志
     */
    @Query("SELECT l FROM SystemLogEntity l WHERE " +
            "l.logType = 'CONFLICT_ALERT' AND " +
            "l.logLevel IN ('WARN', 'ERROR') AND " +
            "l.createTime >= :startTime " +
            "ORDER BY l.createTime DESC")
    List<SystemLogEntity> findActiveAlerts(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询特定实体的最新日志
     */
    @Query("SELECT l FROM SystemLogEntity l WHERE " +
            "l.entityId = :entityId " +
            "ORDER BY l.createTime DESC")
    List<SystemLogEntity> findLatestByEntityId(@Param("entityId") String entityId, Pageable pageable);
}