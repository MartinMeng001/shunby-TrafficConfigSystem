package com.traffic.config.statemachinev2.example;

import com.traffic.config.statemachinev2.core.SystemStateMachine;
import com.traffic.config.statemachinev2.enums.SystemStateV2;
import com.traffic.config.statemachinev2.enums.SystemEventV2;

import com.traffic.config.statemachinev2.enums.ext.ClearanceDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 状态机使用示例
 * 演示完整的系统初始化和全红过渡流程
 *
 * @author System
 * @version 2.0.0
 */
@Component
public class StateMachineUsageExample implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineUsageExample.class);

    @Autowired
    private SystemStateMachine stateMachine;

    private ScheduledExecutorService timerExecutor;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== 状态机使用示例开始 ===");

        // 示例1: 完整的系统启动流程
        demonstrateSystemStartup();

        Thread.sleep(2000);

        // 示例2: 故障检测和恢复流程
        demonstrateFaultDetectionAndRecovery();

        Thread.sleep(2000);

        // 示例3: 紧急模式处理
        demonstrateEmergencyMode();

        logger.info("=== 状态机使用示例结束 ===");
    }

    /**
     * 演示系统启动流程
     */
    private void demonstrateSystemStartup() {
        logger.info("\n=== 演示1: 系统启动流程 ===");

        try {
            // 启动状态机和定时器
            startStateMachineWithTimer();

            // 步骤1: 系统启动，应该在SYSTEM_INIT状态
            logger.info("步骤1: 系统启动");
            logCurrentState();

            // 步骤2: 等待系统初始化完成（由timer_tick触发）
            logger.info("步骤2: 等待系统初始化完成...");
            Thread.sleep(3000); // 等待超过SYSTEM_INIT_DELAY
            logCurrentState();

            // 步骤3: 系统应该自动进入ALL_RED_TRANSITION状态
            logger.info("步骤3: 检查是否进入全红过渡状态");
            if (stateMachine.getCurrentState() == SystemStateV2.ALL_RED_TRANSITION) {
                logger.info("✓ 成功进入全红过渡状态");
            } else {
                logger.warn("✗ 未能进入全红过渡状态，当前状态: {}",
                        stateMachine.getCurrentState().getChineseName());
            }

            // 步骤4: 模拟路段清空过程
            logger.info("步骤4: 开始路段清空过程");
            simulateSegmentClearing();

            // 步骤5: 等待过渡时间完成
            logger.info("步骤5: 等待过渡时间完成...");
            Thread.sleep(6000); // 等待超过TRANSITION_TIME

            // 步骤6: 检查是否进入感应控制模式
            logger.info("步骤6: 检查最终状态");
            logCurrentState();

            if (stateMachine.getCurrentState() == SystemStateV2.INDUCTIVE_MODE) {
                logger.info("✓ 系统启动流程完成，成功进入感应控制模式");
            } else {
                logger.warn("✗ 系统启动流程异常，当前状态: {}",
                        stateMachine.getCurrentState().getChineseName());
            }

        } catch (Exception e) {
            logger.error("系统启动流程演示异常", e);
        }
    }

    /**
     * 演示故障检测和恢复流程
     */
    private void demonstrateFaultDetectionAndRecovery() {
        logger.info("\n=== 演示2: 故障检测和恢复流程 ===");

        try {
            // 确保系统在感应模式
            if (stateMachine.getCurrentState() != SystemStateV2.INDUCTIVE_MODE) {
                logger.warn("系统不在感应模式，跳过故障恢复演示");
                return;
            }

            // 步骤1: 触发故障
            logger.info("步骤1: 触发故障检测事件");
            stateMachine.processEvent(SystemEventV2.FAULT_DETECTED);
            logCurrentState();

            // 步骤2: 等待进入降级模式
            logger.info("步骤2: 等待过渡到降级模式...");
            Thread.sleep(6000); // 等待过渡完成
            logCurrentState();

            if (stateMachine.getCurrentState() == SystemStateV2.DEGRADED_MODE) {
                logger.info("✓ 成功进入降级模式");
            } else {
                logger.warn("✗ 未能进入降级模式");
            }

            // 步骤3: 模拟条件恢复
            logger.info("步骤3: 等待故障条件恢复...");
            Thread.sleep(3000);

            // 手动设置恢复条件
            stateMachine.getVariables().setSystemHealthScore(80);
            stateMachine.getVariables().setCommunicationNormal(true);
            stateMachine.getVariables().setPowerStatusNormal(true);

            stateMachine.processEvent(SystemEventV2.CONDITIONS_RESTORED);
            logCurrentState();

            // 步骤4: 等待恢复到感应模式
            logger.info("步骤4: 等待恢复到感应模式...");
            Thread.sleep(6000);

            // 模拟路段清空
            simulateSegmentClearing();
            Thread.sleep(1000);

            logCurrentState();

            if (stateMachine.getCurrentState() == SystemStateV2.INDUCTIVE_MODE) {
                logger.info("✓ 故障恢复流程完成");
            } else {
                logger.warn("✗ 故障恢复流程未完成，当前状态: {}",
                        stateMachine.getCurrentState().getChineseName());
            }

        } catch (Exception e) {
            logger.error("故障恢复流程演示异常", e);
        }
    }

    /**
     * 演示紧急模式处理
     */
    private void demonstrateEmergencyMode() {
        logger.info("\n=== 演示3: 紧急模式处理 ===");

        try {
            // 步骤1: 触发严重故障
            logger.info("步骤1: 触发严重故障");
            stateMachine.processEvent(SystemEventV2.CRITICAL_FAULT);
            logCurrentState();

            if (stateMachine.getCurrentState() == SystemStateV2.EMERGENCY_MODE) {
                logger.info("✓ 成功进入紧急模式");
            } else {
                logger.warn("✗ 未能进入紧急模式");
            }

            // 步骤2: 等待一段时间
            logger.info("步骤2: 紧急模式运行中...");
            Thread.sleep(3000);

            // 步骤3: 系统重置恢复
            logger.info("步骤3: 执行系统重置");
            stateMachine.processEvent(SystemEventV2.SYSTEM_RESET);
            logCurrentState();

            if (stateMachine.getCurrentState() == SystemStateV2.SYSTEM_INIT) {
                logger.info("✓ 系统重置成功，返回初始化状态");
            } else {
                logger.warn("✗ 系统重置失败");
            }

            // 步骤4: 再次启动系统
            logger.info("步骤4: 再次启动系统...");
            Thread.sleep(4000);
            logCurrentState();

        } catch (Exception e) {
            logger.error("紧急模式演示异常", e);
        }
    }

    /**
     * 启动状态机和定时器
     */
    private void startStateMachineWithTimer() {
        // 启动状态机
        stateMachine.start();

        // 启动定时器
        if (timerExecutor == null || timerExecutor.isShutdown()) {
            timerExecutor = Executors.newSingleThreadScheduledExecutor();
            timerExecutor.scheduleAtFixedRate(() -> {
                try {
                    stateMachine.processEvent(SystemEventV2.TIMER_TICK);
                } catch (Exception e) {
                    logger.error("定时器执行异常", e);
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * 模拟路段清空过程
     */
    private void simulateSegmentClearing() {
        logger.info("开始模拟路段清空...");

        int segmentCount = stateMachine.getVariables().getSegmentCount();

        for (int i = 0; i < segmentCount; i++) {
            String segmentId = "segment_" + i;
            stateMachine.updateSegmentClearanceStatus(segmentId, ClearanceDecision.CLEARED_SAFE);
            logger.debug("路段 {} 已清空", segmentId);

            try {
                Thread.sleep(500); // 每个路段清空间隔0.5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("所有路段清空完成 ({}/{})",
                stateMachine.getVariables().getClearedCount(),
                stateMachine.getVariables().getSegmentCount());
    }

    /**
     * 记录当前状态信息
     */
    private void logCurrentState() {
        SystemStateV2 currentState = stateMachine.getCurrentState();
        String summary = stateMachine.getSystemStatusSummary();

        logger.info("当前状态: {} ({})",
                currentState.getChineseName(),
                currentState.getCode());
        logger.info("系统摘要: {}", summary);

        // 如果在过渡状态，显示额外信息
        if (currentState == SystemStateV2.ALL_RED_TRANSITION) {
            logger.info("过渡信息: 持续时间={}s, 路段清空={}/{}, 完成度={:.1f}%",
                    stateMachine.getVariables().getTransitionDurationSeconds(),
                    stateMachine.getVariables().getClearedCount(),
                    stateMachine.getVariables().getSegmentCount(),
                    stateMachine.getVariables().getClearanceCompletionPercentage() * 100);
        }
    }

    /**
     * 停止定时器
     */
    public void stopTimer() {
        if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdown();
            try {
                if (!timerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    timerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                timerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
