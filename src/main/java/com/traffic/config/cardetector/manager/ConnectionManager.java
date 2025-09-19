package com.traffic.config.cardetector.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    // 将 ConcurrentMap 的键改为 ChannelId，值改为 Channel
    // 这样可以更直接地管理和查找连接
    private final ConcurrentMap<ChannelId, Channel> connections = new ConcurrentHashMap<>();

    // 或者可以存储一个自定义的 ClientConnection 类，以保留更多信息
    // private final ConcurrentMap<ChannelId, ClientConnection> connections = new ConcurrentHashMap<>();

    public void addConnection(Channel channel) {
        ChannelId channelId = channel.id();
        connections.put(channelId, channel);
        log.info("添加客户端连接: {} (唯一ID: {}), 当前连接数: {}",
                channel.remoteAddress(), channelId.asShortText(), connections.size());
    }

    public void removeConnection(Channel channel) {
        ChannelId channelId = channel.id();
        connections.remove(channelId);
        log.info("移除客户端连接: {} (唯一ID: {}), 当前连接数: {}",
                channel.remoteAddress(), channelId.asShortText(), connections.size());
    }

    public void closeAllConnections() {
        log.info("关闭所有客户端连接，总数: {}", connections.size());
        connections.values().forEach(channel -> {
            channel.close();
        });
        connections.clear();
    }

    public int getConnectionCount() {
        return connections.size();
    }
}
