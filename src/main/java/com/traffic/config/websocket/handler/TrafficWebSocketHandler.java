package com.traffic.config.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.traffic.config.websocket.model.TrafficMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 交通信息WebSocket处理器
 * 负责WebSocket连接管理和消息推送
 */
@Component
public class TrafficWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrafficWebSocketHandler.class);

    public TrafficWebSocketHandler() {
        objectMapper.registerModule(new JavaTimeModule()); // 注册 JSR310 模块
    }
    /**
     * 存储所有活跃的WebSocket会话
     */
    private final Set<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();

    /**
     * 存储会话ID到会话对象的映射，用于快速查找
     */
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 添加到活跃会话集合
        activeSessions.add(session);
        sessionMap.put(session.getId(), session);

        logger.info("WebSocket连接建立成功: sessionId={}, remoteAddress={}, 当前活跃连接数={}",
                session.getId(),
                session.getRemoteAddress(),
                activeSessions.size());

        // 发送欢迎消息
        sendWelcomeMessage(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 处理客户端发送的消息（如订阅特定类型的消息等）
        String payload = message.getPayload().toString();
        logger.debug("收到客户端消息: sessionId={}, message={}", session.getId(), payload);

        // 这里可以根据客户端消息进行相应处理，比如订阅特定路段的信息
        // 暂时只记录日志
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket传输错误: sessionId={}, error={}",
                session.getId(), exception.getMessage(), exception);

        // 清理连接
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("WebSocket连接关闭: sessionId={}, closeStatus={}, 剩余活跃连接数={}",
                session.getId(),
                closeStatus.toString(),
                activeSessions.size() - 1);

        // 清理连接
        cleanupSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 向所有连接的客户端广播消息
     */
    public void broadcastMessage(TrafficMessage message) {
        if (activeSessions.isEmpty()) {
            return;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(jsonMessage);

            // 并发发送消息到所有活跃会话
            activeSessions.parallelStream().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                    } else {
                        // 会话已关闭，从集合中移除
                        logger.warn("发现已关闭的会话，将其移除: sessionId={}", session.getId());
                        cleanupSession(session);
                    }
                } catch (IOException e) {
                    logger.error("发送消息失败: sessionId={}, error={}", session.getId(), e.getMessage());
                    // 发送失败的会话也应该被清理
                    cleanupSession(session);
                }
            });

//            logger.debug("广播消息完成: messageType={}, 发送到{}个客户端",
//                    message.getMessageType(), activeSessions.size());

        } catch (Exception e) {
            logger.error("序列化消息失败: messageType={}, error={}",
                    message.getMessageType(), e.getMessage(), e);
        }
    }

    /**
     * 向特定会话发送消息
     */
    public void sendMessageToSession(String sessionId, TrafficMessage message) {
        WebSocketSession session = sessionMap.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                synchronized (session) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
                logger.debug("向特定会话发送消息成功: sessionId={}, messageType={}",
                        sessionId, message.getMessageType());
            } catch (Exception e) {
                logger.error("向特定会话发送消息失败: sessionId={}, error={}", sessionId, e.getMessage());
                cleanupSession(session);
            }
        } else {
            logger.warn("会话不存在或已关闭: sessionId={}", sessionId);
        }
    }

    /**
     * 获取当前活跃连接数
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 获取所有活跃会话ID
     */
    public Set<String> getActiveSessionIds() {
        return sessionMap.keySet();
    }

    /**
     * 发送欢迎消息
     */
    private void sendWelcomeMessage(WebSocketSession session) {
        try {
            TrafficMessage welcomeMessage = new TrafficMessage("WELCOME",
                    "欢迎连接到交通信息发布系统！");
            String jsonMessage = objectMapper.writeValueAsString(welcomeMessage);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            logger.error("发送欢迎消息失败: sessionId={}, error={}", session.getId(), e.getMessage());
        }
    }

    /**
     * 清理会话
     */
    private void cleanupSession(WebSocketSession session) {
        activeSessions.remove(session);
        sessionMap.remove(session.getId());

        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            logger.error("关闭会话时出错: sessionId={}, error={}", session.getId(), e.getMessage());
        }
    }

    /**
     * 健康检查方法
     */
    public boolean isHealthy() {
        // 清理已关闭的会话
        activeSessions.removeIf(session -> !session.isOpen());
        sessionMap.entrySet().removeIf(entry -> !entry.getValue().isOpen());

        return true;
    }
}