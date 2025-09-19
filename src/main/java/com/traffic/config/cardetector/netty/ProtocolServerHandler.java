package com.traffic.config.cardetector.netty;
import com.traffic.config.cardetector.manager.ConnectionManager;
import com.traffic.config.cardetector.manager.DataAccessManager;
import com.traffic.config.cardetector.model.ProtocolMessage;
import com.traffic.config.util.DataUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import io.netty.channel.socket.SocketChannel;
import java.net.InetSocketAddress;

@Component
@ChannelHandler.Sharable
public class ProtocolServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ProtocolServerHandler.class);

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private DataAccessManager dataAccessManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("新客户端连接: {}", ctx.channel().remoteAddress());
        // 在此处将 Channel 注册到 ConnectionManager
        connectionManager.addConnection(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端连接已断开: {}", ctx.channel().remoteAddress());
        // 在此处移除 ConnectionManager 中的连接
        connectionManager.removeConnection(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ProtocolMessage message = (ProtocolMessage) msg;

        // 业务逻辑处理
        if (isHeartbeatMessage(message)) {
            sendHeartbeatResponse(ctx, message);
        }
        dataAccessManager.processMessage(message);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲，说明客户端长时间未发送数据，可以认为连接空闲或异常
                log.warn("客户端 {} 读空闲超时，即将断开连接", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("处理客户端 {} 数据时发生错误", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    // 以下为协议处理的辅助方法，与原代码逻辑相似，但使用 Netty ByteBuf
    private ProtocolMessage readProtocolMessage(ByteBuf in, ChannelHandlerContext ctx) {
        // 确保至少有开始字节
        if (in.readableBytes() < 1) {
            return null;
        }

        // 1. 查找开始字节 0x7E
        int startIndex = in.readerIndex();
        if (in.getByte(startIndex) != 0x7E) {
            log.warn("无效的开始字节: 0x{}", Integer.toHexString(in.getByte(startIndex)));
            return null;
        }

        // 2. 逐字节查找结束字节 0x7D 并处理转义
        int endIndex = findEndByte(in, startIndex + 1);
        if (endIndex == -1) {
            // 报文不完整，继续等待更多数据
            return null;
        }

        // 3. 提取转义后的数据
        ByteBuf rawData = in.readBytes(endIndex - startIndex + 1);
        byte[] unescapedData = unescapeData(rawData);

        // 4. 验证数据长度和校验和（逻辑与原代码相同）
        if (unescapedData.length < 3) {
            log.warn("反转义后的数据长度不足");
            return null;
        }

        int dataLength = ((unescapedData[0] & 0xFF) << 8) | (unescapedData[1] & 0xFF);
        if (unescapedData.length != dataLength + 2) {
            log.warn("报文长度验证失败。报文头长度: {}, 实际反转义后数据总长度: {}", dataLength, unescapedData.length - 2);
//            log.info(DataUtil.formatBytes(unescapedData));
            return null;
        }

        byte[] data = new byte[dataLength - 1];
        System.arraycopy(unescapedData, 2, data, 0, data.length);
        byte checksum = unescapedData[unescapedData.length - 1];
        if (!validateChecksum(data, checksum)) {
            log.warn("校验和验证失败");
            return null;
        }

        // 5. 构建协议消息
        ProtocolMessage message = new ProtocolMessage();
        message.setStartByte((byte) 0x7E);
        message.setDataLength(dataLength);
        message.setData(data);
        message.setChecksum(checksum);
        message.setEndByte((byte) 0x7D);
        message.setClientAddress(((InetSocketAddress)ctx.channel().remoteAddress()).getAddress());

        return message;
    }

    private int findEndByte(ByteBuf in, int startIndex) {
        int currentIndex = startIndex;
        boolean isEscaped = false;
        while (currentIndex < in.writerIndex()) {
            byte b = in.getByte(currentIndex);
            if (b == 0x5C && !isEscaped) {
                isEscaped = true;
            } else if (b == 0x7D && !isEscaped) {
                return currentIndex; // 找到结束字节
            } else {
                isEscaped = false;
            }
            currentIndex++;
        }
        return -1; // 未找到结束字节
    }

    private byte[] unescapeData(ByteBuf in) {
        ByteArrayOutputStream unescaped = new ByteArrayOutputStream();
        boolean isEscaped = false;
        while (in.isReadable()) {
            byte b = in.readByte();
            if (b == 0x7E) continue; // 跳过开始字节
            if (b == 0x7D) break; // 遇到结束字节

            if (b == 0x5C && !isEscaped) {
                isEscaped = true;
            } else {
                unescaped.write(b);
                isEscaped = false;
            }
        }
        return unescaped.toByteArray();
    }

    private boolean validateChecksum(byte[] data, byte expectedChecksum) {
        byte calculatedChecksum = 0;
        for (byte b : data) {
            calculatedChecksum ^= b;
        }
        return calculatedChecksum == expectedChecksum;
    }

    private boolean isHeartbeatMessage(ProtocolMessage message) {
        return message.getData().length >= 1 && message.getData()[0] == 0;
    }

    private void sendHeartbeatResponse(ChannelHandlerContext ctx, ProtocolMessage originalMessage) {
        // 1. 获取原始数据和计算校验和
        byte[] responseData = originalMessage.getData();
        byte checksum = 0;
        for (byte b : responseData) {
            checksum ^= b;
        }

        // 2. 将数据和校验和合并，并进行转义
        byte[] dataWithChecksum = new byte[responseData.length + 1];
        System.arraycopy(responseData, 0, dataWithChecksum, 0, responseData.length);
        dataWithChecksum[dataWithChecksum.length - 1] = checksum;

        byte[] escapedData = escapeData(dataWithChecksum);

        // 3. 构建完整的响应报文
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeByte(0x7E); // 开始字节
        buffer.writeShort(escapedData.length); // 写入转义后的数据长度
        buffer.writeBytes(escapedData); // 写入转义后的数据
        buffer.writeByte(0x7D); // 结束字节

        // 4. 发送报文
        ctx.writeAndFlush(buffer);
    }
    private byte[] escapeData(byte[] data) {
        ByteArrayOutputStream escaped = new ByteArrayOutputStream();

        for (byte b : data) {
            if (b == 0x7E || b == 0x7D || b == 0x5C) {
                escaped.write(0x5C); // 添加转义字符
            }
            escaped.write(b);
        }

        return escaped.toByteArray();
    }
}
