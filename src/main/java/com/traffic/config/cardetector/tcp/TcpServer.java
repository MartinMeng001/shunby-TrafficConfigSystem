package com.traffic.config.cardetector.tcp;

import com.traffic.config.cardetector.manager.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TcpServer {
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    @Value("${traffic.tcp.port:8130}")
    private int port;

    @Value("${traffic.tcp.threads:10}")
    private int threadPoolSize;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private TcpClientHandler clientHandler;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    @PostConstruct
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            running = true;

            // 启动接受连接的线程
            executorService.submit(this::acceptConnections);
            log.info("TCP服务器启动成功，监听端口: {}", port);

        } catch (IOException e) {
            log.error("TCP服务器启动失败", e);
            throw new RuntimeException("Failed to start TCP server", e);
        }
    }

    private void acceptConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.info("新客户端连接: {}", clientSocket.getRemoteSocketAddress());

                // 为每个客户端分配处理线程
                executorService.submit(() -> clientHandler.handleClient(clientSocket));

            } catch (IOException e) {
                if (running) {
                    log.error("接受客户端连接时发生错误", e);
                }
            }
        }
    }

    @PreDestroy
    public void stopServer() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            if (executorService != null) {
                executorService.shutdown();
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }

            connectionManager.closeAllConnections();
            log.info("TCP服务器已停止");

        } catch (Exception e) {
            log.error("停止TCP服务器时发生错误", e);
        }
    }

    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }
}


