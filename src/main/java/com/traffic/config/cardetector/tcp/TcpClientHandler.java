package com.traffic.config.cardetector.tcp;

import com.traffic.config.cardetector.manager.ConnectionManager;
import com.traffic.config.cardetector.manager.DataAccessManager;
import com.traffic.config.cardetector.model.ProtocolMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

@Component
public class TcpClientHandler {
    private static final Logger log = LoggerFactory.getLogger(TcpClientHandler.class);

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private DataAccessManager dataAccessManager;

    public void handleClient(Socket clientSocket) {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        try {
            // 注册连接
            connectionManager.addConnection(clientSocket);

            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            log.info("开始处理客户端 {} 的数据", clientAddress);

            while (!clientSocket.isClosed() && clientSocket.isConnected()) {
                try {
                    // 读取协议消息
                    ProtocolMessage message = readProtocolMessage(inputStream, clientSocket);
                    if (message != null) {
                        // 如果是心跳消息，需要回复
                        if (isHeartbeatMessage(message)) {
                            sendHeartbeatResponse(outputStream, message);
                            //dataAccessManager.processMessage(message);  // 可选的功能，心跳消息也可以处理一下
                        }else{
                            // 处理消息
                            dataAccessManager.processMessage(message);
                        }
                    }

                } catch (IOException e) {
                    log.warn("客户端 {} 连接中断", clientAddress);
                    break;
                } catch (Exception e) {
                    log.error("处理客户端 {} 数据时发生错误", clientAddress, e);
                }
            }

        } catch (Exception e) {
            log.error("处理客户端 {} 时发生错误", clientAddress, e);
        } finally {
            try {
                connectionManager.removeConnection(clientSocket);
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
                log.info("客户端 {} 连接已关闭", clientAddress);
            } catch (IOException e) {
                log.error("关闭客户端 {} 连接时发生错误", clientAddress, e);
            }
        }
    }

    private ProtocolMessage readProtocolMessage(InputStream inputStream, Socket socket) throws IOException {
        // 1. 读取开始字节 0x7E
        int startByte = inputStream.read();
        if (startByte == -1) {
            return null; // 连接已关闭
        }

        if (startByte != 0x7E) {
            log.warn("无效的开始字节: 0x{}", Integer.toHexString(startByte));
            return null;
        }

        // 2. 逐字节读取，并同步处理转义，直到遇到结束字节 0x7D
        ByteArrayOutputStream unescapedDataBuffer = new ByteArrayOutputStream();
        int b;
        boolean isEscaped = false;

        while ((b = inputStream.read()) != -1) {
            if (b == 0x5C) { // 遇到转义字符
                isEscaped = true;
                continue; // 继续读取下一个字节
            }

            if (isEscaped) {
                // 处理转义后的字节
                unescapedDataBuffer.write(b);
                isEscaped = false;
            } else if (b == 0x7D) { // 遇到结束字节
                break;
            } else {
                // 普通字节
                unescapedDataBuffer.write(b);
            }
        }

        if (b == -1) {
            throw new IOException("连接意外关闭，未找到结束字节");
        }

        byte[] unescapedData = unescapedDataBuffer.toByteArray();

        // 3. 验证数据长度
        // 协议文档规定报文长度是“转义前”的“数据”部分字节数 + “校验”部分字节数
        // 在我们的反转义后的数据流中，前两个字节是报文长度字段
        if (unescapedData.length < 3) { // 至少包含2字节长度 + 1字节校验
            throw new IOException("反转义后的数据长度不足");
        }

        // 分离报文长度
        byte[] lengthBytes = new byte[2];
        System.arraycopy(unescapedData, 0, lengthBytes, 0, 2);
        int dataLength = ((lengthBytes[0] & 0xFF) << 8) | (lengthBytes[1] & 0xFF);

        // 协议文档中，报文长度不包含长度字段本身，但包含数据和校验
        // 所以 unescapedData 的总长度应该是：2 (长度字段) + dataLength
        if (unescapedData.length != dataLength + 2) {
            log.warn("报文长度验证失败。报文头长度: {}, 实际反转义后数据总长度: {}", dataLength, unescapedData.length - 2);
            return null;
        }

        // 4. 分离数据和校验
        byte[] data = new byte[dataLength - 1]; // dataLength 包含数据和校验，所以数据长度 = dataLength - 1
        System.arraycopy(unescapedData, 2, data, 0, data.length);
        byte checksum = unescapedData[unescapedData.length - 1];

        // 5. 验证校验和
        if (!validateChecksum(data, checksum)) {
            log.warn("校验和验证失败");
            return null;
        }

        // 6. 构建协议消息
        ProtocolMessage message = new ProtocolMessage();
        message.setStartByte((byte) startByte);
        message.setDataLength(dataLength);
        message.setData(data);
        message.setChecksum(checksum);
        message.setEndByte((byte) 0x7D);
        message.setReceiveTime(LocalDateTime.now());
        message.setClientAddress(socket.getInetAddress());

        return message;
    }

//    private ProtocolMessage readProtocolMessage(InputStream inputStream, Socket socket) throws IOException {
//        // 读取开始字节
//        int startByte = inputStream.read();
//        if (startByte == -1) return null; // 连接已关闭
//
//        if (startByte != 0x7E) {
//            log.warn("无效的开始字节: 0x{}", Integer.toHexString(startByte));
//            return null;
//        }
//
//        // 读取报文长度 (2字节，高字节在前)
//        byte[] lengthBytes = new byte[2];
//        if (inputStream.read(lengthBytes) != 2) {
//            throw new IOException("无法读取报文长度");
//        }
//        int dataLength = ((lengthBytes[0] & 0xFF) << 8) | (lengthBytes[1] & 0xFF);
//
//        // 读取数据部分和校验 (包含转义字符)
//        byte[] rawData = new byte[dataLength];
//        int totalRead = 0;
//        while (totalRead < dataLength) {
//            int bytesRead = inputStream.read(rawData, totalRead, dataLength - totalRead);
//            if (bytesRead == -1) {
//                throw new IOException("连接意外关闭");
//            }
//            totalRead += bytesRead;
//        }
//
//        // 读取结束字节
//        int endByte = inputStream.read();
//        if (endByte != 0x7D) {
//            log.warn("无效的结束字节: 0x{}", Integer.toHexString(endByte));
//            return null;
//        }
//
//        // 反转义处理
//        byte[] unescapedData = unescapeData(rawData);
//
//        // 分离数据和校验
//        if (unescapedData.length < 1) {
//            throw new IOException("数据长度不足");
//        }
//
//        byte[] data = new byte[unescapedData.length - 1];
//        System.arraycopy(unescapedData, 0, data, 0, data.length);
//        byte checksum = unescapedData[unescapedData.length - 1];
//
//        // 验证校验和
//        if (!validateChecksum(data, checksum)) {
//            log.warn("校验和验证失败");
//            return null;
//        }
//
//        // 构建协议消息
//        ProtocolMessage message = new ProtocolMessage();
//        message.setStartByte((byte) startByte);
//        message.setDataLength(dataLength);
//        message.setData(data);
//        message.setChecksum(checksum);
//        message.setEndByte((byte) endByte);
//        message.setReceiveTime(LocalDateTime.now());
//        message.setClientAddress(socket.getInetAddress());
//
//        return message;
//    }

    private byte[] unescapeData(byte[] escapedData) {
        ByteArrayOutputStream unescaped = new ByteArrayOutputStream();

        for (int i = 0; i < escapedData.length; i++) {
            byte b = escapedData[i];
            if (b == 0x5C && i + 1 < escapedData.length) {
                // 遇到转义字符，取下一个字节
                byte nextByte = escapedData[i + 1];
                unescaped.write(nextByte);
                i++; // 跳过转义字符
            } else {
                unescaped.write(b);
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

    private void sendHeartbeatResponse(OutputStream outputStream, ProtocolMessage originalMessage) throws IOException {
        // 心跳响应：返回相同的数据
        byte[] responseData = originalMessage.getData();

        // 计算校验和
        byte checksum = 0;
        for (byte b : responseData) {
            checksum ^= b;
        }

        // 构建响应数据（加上校验和）
        byte[] dataWithChecksum = new byte[responseData.length + 1];
        System.arraycopy(responseData, 0, dataWithChecksum, 0, responseData.length);
        dataWithChecksum[dataWithChecksum.length - 1] = checksum;

        // 转义处理
        byte[] escapedData = escapeData(dataWithChecksum);

        // 发送响应
        outputStream.write(0x7E); // 开始字节
        outputStream.write((escapedData.length >> 8) & 0xFF); // 长度高字节
        outputStream.write(escapedData.length & 0xFF); // 长度低字节
        outputStream.write(escapedData); // 数据
        outputStream.write(0x7D); // 结束字节
        outputStream.flush();

        log.debug("发送心跳响应给客户端");
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

