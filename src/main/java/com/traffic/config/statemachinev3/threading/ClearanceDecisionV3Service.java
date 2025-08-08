package com.traffic.config.statemachinev3.threading;

import com.traffic.config.statemachinev3.clearance.ClearanceDecisionEngine;
import com.traffic.config.statemachinev3.enums.segment.ClearanceDecision;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ClearanceDecisionV3Service {

    private static final Logger logger = LoggerFactory.getLogger(ClearanceDecisionV3Service.class);

    private final ClearanceDecisionEngine clearanceEngine = new ClearanceDecisionEngine();

    /**
     * 异步计算上行清空决策
     */
    @Async("clearanceDecisionV3Executor")
    public CompletableFuture<ClearanceDecision> calculateUpstreamClearanceAsync(SegmentVariables variables) {
        try {
            ClearanceDecision decision = clearanceEngine.calculateUpstreamClearance(variables);
            logger.debug("路段{}上行清空决策: {}", variables.getSegmentId(), decision);
            return CompletableFuture.completedFuture(decision);
        } catch (Exception e) {
            logger.error("上行清空决策计算异常: 路段{}", variables.getSegmentId(), e);
            return CompletableFuture.completedFuture(ClearanceDecision.WAIT);
        }
    }

    /**
     * 异步计算下行清空决策
     */
    @Async("clearanceDecisionV3Executor")
    public CompletableFuture<ClearanceDecision> calculateDownstreamClearanceAsync(SegmentVariables variables) {
        try {
            ClearanceDecision decision = clearanceEngine.calculateDownstreamClearance(variables);
            logger.debug("路段{}下行清空决策: {}", variables.getSegmentId(), decision);
            return CompletableFuture.completedFuture(decision);
        } catch (Exception e) {
            logger.error("下行清空决策计算异常: 路段{}", variables.getSegmentId(), e);
            return CompletableFuture.completedFuture(ClearanceDecision.WAIT);
        }
    }

    /**
     * 异步计算整体清空决策
     */
    @Async("clearanceDecisionV3Executor")
    @Profile("!statemachine-v2")
    public CompletableFuture<ClearanceDecision> calculateOverallClearanceAsync(SegmentVariables variables) {
        try {
            // 并行计算上行和下行决策
            CompletableFuture<ClearanceDecision> upstreamFuture = calculateUpstreamClearanceAsync(variables);
            CompletableFuture<ClearanceDecision> downstreamFuture = calculateDownstreamClearanceAsync(variables);

            // 等待两个决策完成并综合判断
            return CompletableFuture.allOf(upstreamFuture, downstreamFuture)
                    .thenApply(v -> {
                        ClearanceDecision upstream = upstreamFuture.join();
                        ClearanceDecision downstream = downstreamFuture.join();
                        return clearanceEngine.calculateOverallClearance(variables);
                    });
        } catch (Exception e) {
            logger.error("整体清空决策计算异常: 路段{}", variables.getSegmentId(), e);
            return CompletableFuture.completedFuture(ClearanceDecision.WAIT);
        }
    }
}
