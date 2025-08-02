package com.traffic.config.cardetector.model;

import java.time.LocalDateTime;
import java.net.InetAddress;

public class ProtocolMessage {
    private byte startByte = (byte) 0x7E;           // 开始字节
    private int dataLength;                          // 报文长度
    private byte[] data;                            // 数据部分
    private byte checksum;                          // 异或校验
    private byte endByte = (byte) 0x7D;             // 结束字节
    private LocalDateTime receiveTime;              // 接收时间
    private InetAddress clientAddress;              // 客户端地址

    // 构造器、getter、setter
    public ProtocolMessage() {
        this.receiveTime = LocalDateTime.now();
    }

    // getter和setter方法
    public byte getStartByte() { return startByte; }
    public void setStartByte(byte startByte) { this.startByte = startByte; }

    public int getDataLength() { return dataLength; }
    public void setDataLength(int dataLength) { this.dataLength = dataLength; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public byte getChecksum() { return checksum; }
    public void setChecksum(byte checksum) { this.checksum = checksum; }

    public byte getEndByte() { return endByte; }
    public void setEndByte(byte endByte) { this.endByte = endByte; }

    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }

    public InetAddress getClientAddress() { return clientAddress; }
    public void setClientAddress(InetAddress clientAddress) { this.clientAddress = clientAddress; }
}

