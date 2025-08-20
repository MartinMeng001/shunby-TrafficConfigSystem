package com.traffic.config.controller;

import com.traffic.config.websocket.handler.TrafficWebSocketHandler;
import com.traffic.config.websocket.service.TrafficMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket管理REST接口
 * 提供WebSocket连接状态查询和管理功能
 */
@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private TrafficWebSocketHandler webSocketHandler;

    @Autowired
    private TrafficMessagePublisher messagePublisher;

    /**
     * 获取WebSocket连接状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("activeConnections", webSocketHandler.getActiveSessionCount());
            status.put("sessionIds", webSocketHandler.getActiveSessionIds());
            status.put("isHealthy", webSocketHandler.isHealthy());
            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取WebSocket状态时出错", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取活跃会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        try {
            Set<String> sessionIds = webSocketHandler.getActiveSessionIds();
            Map<String, Object> response = new HashMap<>();
            response.put("sessionCount", sessionIds.size());
            response.put("sessionIds", sessionIds);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取活跃会话列表时出错", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取会话列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 手动触发所有状态信息推送
     */
    @PostMapping("/publish/all")
    public ResponseEntity<Map<String, Object>> publishAllStatus() {
        try {
            messagePublisher.publishAllStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "所有状态信息推送完成");
            response.put("activeConnections", webSocketHandler.getActiveSessionCount());
            response.put("timestamp", System.currentTimeMillis());

            logger.info("手动触发所有状态信息推送完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("手动推送所有状态时出错", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "推送失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 清除状态缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearStateCache() {
        try {
            messagePublisher.clearStateCache();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "状态缓存已清除");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("状态缓存清除完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("清除状态缓存时出错", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "清除缓存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean isHealthy = webSocketHandler.isHealthy();
            int activeConnections = webSocketHandler.getActiveSessionCount();

            Map<String, Object> health = new HashMap<>();
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("activeConnections", activeConnections);
            health.put("details", Map.of(
                    "websocketHandler", isHealthy ? "UP" : "DOWN",
                    "messagePublisher", "UP"
            ));
            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("WebSocket健康检查时出错", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取消息统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMessageStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("activeConnections", webSocketHandler.getActiveSessionCount());
            stats.put("totalSessions", webSocketHandler.getActiveSessionIds().size());

            // 这里可以添加更多统计信息，比如发送消息数量等
            // 需要在TrafficMessagePublisher中添加相应的计数器

            stats.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取消息统计信息时出错", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
