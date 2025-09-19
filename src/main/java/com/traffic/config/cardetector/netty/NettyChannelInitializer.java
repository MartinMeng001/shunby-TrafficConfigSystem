package com.traffic.config.cardetector.netty;
import com.traffic.config.cardetector.model.ProtocolMessageDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final int READER_IDLE_TIME_SECONDS = 30;

    @Autowired
    private ProtocolServerHandler protocolServerHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // 1. 添加空闲状态处理器
        p.addLast("idleStateHandler", new IdleStateHandler(READER_IDLE_TIME_SECONDS, 0, 0));

        // 2. 添加我们新的协议解码器
        p.addLast("protocolMessageDecoder", new ProtocolMessageDecoder());

        // 3. 添加业务逻辑处理器
        p.addLast("protocolServerHandler", protocolServerHandler);
    }
}
