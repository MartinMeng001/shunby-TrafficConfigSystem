package com.traffic.config.cardetector.manager;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private final ConcurrentMap<String, ClientConnection> connections = new ConcurrentHashMap<>();
    private final AtomicLong connectionCounter = new AtomicLong(0);

    public void addConnection(Socket socket) {
        String remoteAddress = socket.getRemoteSocketAddress().toString();

        // 关键改进：先清理相同地址的旧连接
        cleanupStaleConnections(remoteAddress);

        // 使用唯一ID避免冲突
        String uniqueClientId = remoteAddress + "#" + connectionCounter.incrementAndGet();
        ClientConnection connection = new ClientConnection(socket, LocalDateTime.now());
        connections.put(uniqueClientId, connection);

        log.info("添加客户端连接: {} (唯一ID: {}), 当前连接数: {}", remoteAddress, uniqueClientId, connections.size());
    }

    private void cleanupStaleConnections(String remoteAddress) {
        // 清理相同远程地址的所有旧连接
        connections.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(remoteAddress + "#")) {
                ClientConnection oldConn = entry.getValue();
                try {
                    if (!oldConn.getSocket().isClosed()) {
                        oldConn.getSocket().close();
                        log.info("清理旧连接: {}", entry.getKey());
                    }
                } catch (IOException e) {
                    log.debug("清理旧连接时出错: {}", e.getMessage());
                }
                return true;
            }
            return false;
        });
    }

    public void removeConnection(Socket socket) {
        String remoteAddress = socket.getRemoteSocketAddress().toString();
        connections.entrySet().removeIf(entry -> {
            if (entry.getValue().getSocket() == socket) {
                log.info("移除客户端连接: {}, 当前连接数: {}", entry.getKey(), connections.size() - 1);
                return true;
            }
            return false;
        });
    }
//    public void addConnection(Socket socket) {
//        String clientId = socket.getRemoteSocketAddress().toString();
//        ClientConnection connection = new ClientConnection(socket, LocalDateTime.now());
//        connections.put(clientId, connection);
//        log.info("添加客户端连接: {}, 当前连接数: {}", clientId, connections.size());
//    }

//    public void removeConnection(Socket socket) {
//        String clientId = socket.getRemoteSocketAddress().toString();
//        connections.remove(clientId);
//        log.info("移除客户端连接: {}, 当前连接数: {}", clientId, connections.size());
//    }

    public void closeAllConnections() {
        log.info("关闭所有客户端连接，总数: {}", connections.size());
        connections.values().forEach(connection -> {
            try {
                if (!connection.getSocket().isClosed()) {
                    connection.getSocket().close();
                }
            } catch (IOException e) {
                log.error("关闭客户端连接时发生错误", e);
            }
        });
        connections.clear();
    }

    public int getConnectionCount() {
        return connections.size();
    }

    public boolean hasConnection(String clientId) {
        return connections.containsKey(clientId);
    }

    private static class ClientConnection {
        private final Socket socket;
        private final LocalDateTime connectTime;
        private volatile LocalDateTime lastHeartbeat;

        public ClientConnection(Socket socket, LocalDateTime connectTime) {
            this.socket = socket;
            this.connectTime = connectTime;
            this.lastHeartbeat = connectTime;
        }

        public Socket getSocket() { return socket; }
        public LocalDateTime getConnectTime() { return connectTime; }
        public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
        public void updateLastHeartbeat() { this.lastHeartbeat = LocalDateTime.now(); }
    }
}

