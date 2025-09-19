package com.traffic.config.cardetector.model;
import com.traffic.config.util.DataUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ProtocolMessageDecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(ProtocolMessageDecoder.class);

    private static final byte START_BYTE = (byte) 0x7E;
    private static final byte END_BYTE = (byte) 0x7D;
    private static final byte ESCAPE_BYTE = (byte) 0x5C;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 1. 寻找开始字节 0x7E
        int startByteIndex = findByte(in, START_BYTE);
        if (startByteIndex == -1) {
            // 没有找到开始字节，丢弃已读数据并返回，等待更多数据
            in.discardReadBytes();
            return;
        }

        // 丢弃开始字节之前的所有数据
        in.readerIndex(startByteIndex + 1);

        // 2. 寻找结束字节 0x7D 并检查报文完整性
        int endIndex = findEndByte(in, in.readerIndex());
        if (endIndex == -1) {
            // 未找到结束字节，报文不完整，继续等待
            return;
        }

        // 报文已完整，从输入流中读取整个报文数据块（不包含开始字节）
//        ByteBuf rawMessage = in.readBytes(endIndex - in.readerIndex() + 1);
        // 报文已完整，从输入流中读取整个报文数据块（不包含开始字节和结束字节）
        ByteBuf rawMessage = in.readBytes(endIndex - in.readerIndex());
        // 读取并丢弃结束字节
        in.readByte();

        try {
            // 3. 对数据块进行反转义
            byte[] unescapedData = unescapeData(rawMessage);

            // 4. 验证报文长度和校验和
            if (unescapedData.length < 3) {
                log.warn("反转义后的数据长度不足");
                return;
            }

            // 分离报文长度字段
            int dataLength = ((unescapedData[0] & 0xFF) << 8) | (unescapedData[1] & 0xFF);
            if (unescapedData.length != dataLength + 2) {
                log.warn("报文长度验证失败。报文头长度: {}, 实际反转义后数据总长度: {}", dataLength, unescapedData.length - 2);
                log.info(DataUtil.formatBytes(unescapedData));
                return;
            }

            // 分离数据和校验和
            byte[] data = new byte[dataLength - 1];
            System.arraycopy(unescapedData, 2, data, 0, data.length);
            byte checksum = unescapedData[unescapedData.length - 1];

            if (!validateChecksum(data, checksum)) {
                log.warn("校验和验证失败");
                return;
            }

            // 5. 构建 ProtocolMessage 并传递给下一个处理器
            ProtocolMessage message = new ProtocolMessage();
            message.setStartByte(START_BYTE);
            message.setDataLength(dataLength);
            message.setData(data);
            message.setChecksum(checksum);
            message.setEndByte(END_BYTE);
            // 接收时间等信息可以在这里或业务处理器中设置
            out.add(message);

        } finally {
            rawMessage.release(); // 释放资源
        }
    }

    /**
     * 在 ByteBuf 中寻找指定字节，跳过转义的字节
     */
    private int findByte(ByteBuf in, byte targetByte) {
        int readerIndex = in.readerIndex();
        boolean isEscaped = false;
        while (readerIndex < in.writerIndex()) {
            byte b = in.getByte(readerIndex);
            if (b == ESCAPE_BYTE && !isEscaped) {
                isEscaped = true;
            } else if (b == targetByte && !isEscaped) {
                return readerIndex;
            } else {
                isEscaped = false;
            }
            readerIndex++;
        }
        return -1;
    }

    /**
     * 从当前读索引开始，在 ByteBuf 中寻找未转义的结束字节
     */
    private int findEndByte(ByteBuf in, int startIndex) {
        int readerIndex = startIndex;
        boolean isEscaped = false;
        while (readerIndex < in.writerIndex()) {
            byte b = in.getByte(readerIndex);
            if (b == ESCAPE_BYTE && !isEscaped) {
                isEscaped = true;
            } else if (b == END_BYTE && !isEscaped) {
                return readerIndex;
            } else {
                isEscaped = false;
            }
            readerIndex++;
        }
        return -1;
    }

    /**
     * 对一个完整的、包含转义的数据块进行反转义
     */
    private byte[] unescapeData(ByteBuf escapedData) {
        ByteArrayOutputStream unescaped = new ByteArrayOutputStream();
        while (escapedData.isReadable()) {
            byte b = escapedData.readByte();
            if (b == ESCAPE_BYTE) {
                if (escapedData.isReadable()) {
                    byte nextByte = escapedData.readByte();
                    unescaped.write(nextByte);
                }
            } else {
                unescaped.write(b);
            }
        }
        return unescaped.toByteArray();
    }

    /**
     * 校验和验证逻辑
     */
    private boolean validateChecksum(byte[] data, byte expectedChecksum) {
        byte calculatedChecksum = 0;
        for (byte b : data) {
            calculatedChecksum ^= b;
        }
        return calculatedChecksum == expectedChecksum;
    }
}
