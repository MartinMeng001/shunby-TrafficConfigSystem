package com.traffic.config.cardetector.netty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class NettyServer {

    @Value("${traffic.tcp.port:8130}")
    private int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    private NettyChannelInitializer nettyChannelInitializer;

    @PostConstruct
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup(1); // 1个线程用于接受连接
        workerGroup = new NioEventLoopGroup(); // 默认线程数用于处理I/O事件

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(nettyChannelInitializer);

            ChannelFuture f = b.bind(port).sync();
            // 在这里可以添加日志，表明服务器已启动
            System.out.println("Netty TCP server started on port " + port);

            f.channel().closeFuture().sync();
        } finally {
            // 在 PreDestroy 方法中关闭
        }
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().sync();
        }
        System.out.println("Netty TCP server stopped.");
    }
}