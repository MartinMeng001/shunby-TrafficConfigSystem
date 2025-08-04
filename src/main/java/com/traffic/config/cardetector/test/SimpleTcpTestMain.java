package com.traffic.config.cardetector.test;

import com.traffic.config.cardetector.manager.ConnectionManager;
import com.traffic.config.cardetector.manager.DataAccessManager;
import com.traffic.config.cardetector.model.ProtocolMessage;
import com.traffic.config.cardetector.parser.VehicleDataParser;
import com.traffic.config.cardetector.tcp.TcpClientHandler;
import com.traffic.config.cardetector.tcp.TcpServer;
import com.traffic.config.cardetector.model.VehicleData;
import com.traffic.config.exception.DataParseException;

/**
 * 简单的TCP服务测试启动器
 * 启动TCP服务，接收外部设备数据并解析，在控制台输出解析结果
 */
public class SimpleTcpTestMain {

    public static void main(String[] args) {
        System.out.println("=== TCP车辆检测服务测试启动器 ===");
        System.out.println("启动TCP服务，等待外部设备连接...\n");

        try {
            // 创建组件
            ConnectionManager connectionManager = new ConnectionManager();
            VehicleDataParser vehicleDataParser = new VehicleDataParser();

            // 创建数据访问管理器，打印解析结果
            DataAccessManager dataAccessManager = new TestDataAccessManager(vehicleDataParser);

            // 创建客户端处理器
            TcpClientHandler clientHandler = new TcpClientHandler();
            setField(clientHandler, "connectionManager", connectionManager);
            setField(clientHandler, "dataAccessManager", dataAccessManager);

            // 创建并启动TCP服务器
            TcpServer tcpServer = new TcpServer();
            setField(tcpServer, "port", 8130);
            setField(tcpServer, "threadPoolSize", 10);
            setField(tcpServer, "connectionManager", connectionManager);
            setField(tcpServer, "clientHandler", clientHandler);

            // 启动服务器
            tcpServer.startServer();

            System.out.println("✓ TCP服务已启动，监听端口: 8130");
            System.out.println("✓ 数据解析器已就绪");
            System.out.println("✓ 等待外部设备连接和发送数据...\n");
            System.out.println("按 Ctrl+C 停止服务\n");

            // 保持程序运行
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n正在停止TCP服务...");
                tcpServer.stopServer();
                System.out.println("TCP服务已停止");
            }));

            // 等待
            while (tcpServer.isRunning()) {
                Thread.sleep(1000);
                System.out.print(".");
                System.out.flush();
            }

        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试用的数据访问管理器 - 在控制台打印解析结果
     */
    static class TestDataAccessManager extends DataAccessManager {
        private final VehicleDataParser vehicleDataParser;

        public TestDataAccessManager(VehicleDataParser parser) {
            this.vehicleDataParser = parser;
        }

        @Override
        public void processMessage(ProtocolMessage message) {
            System.out.println("\n=== 收到消息 ===");
            System.out.println("时间: " + message.getReceiveTime());
            System.out.println("来源IP: " + message.getClientAddress());
            System.out.println("数据长度: " + message.getDataLength());

            try {
                // 判断消息类型并解析
                if (message.getData().length > 0) {
                    byte dataType = message.getData()[0];

                    if (dataType == 0x00) {
                        // 心跳消息
                        System.out.println("消息类型: 心跳消息");
                        if (message.getData().length >= 5) {
                            byte[] ipBytes = new byte[4];
                            System.arraycopy(message.getData(), 1, ipBytes, 0, 4);
                            String ip = String.format("%d.%d.%d.%d",
                                    ipBytes[0] & 0xFF, ipBytes[1] & 0xFF,
                                    ipBytes[2] & 0xFF, ipBytes[3] & 0xFF);
                            System.out.println("信号机IP: " + ip);
                        }

                    } else if (dataType == 0x01) {
                        // 车辆数据
                        System.out.println("消息类型: 车辆数据");

                        if (vehicleDataParser.canParse(message)) {
                            VehicleData vehicleData = vehicleDataParser.parse(message);

                            System.out.println("解析结果:");
                            System.out.println("  数据类型: " + vehicleData.getDataType());
                            System.out.println("  信号机IP: " + vehicleData.getSignalIp());
                            System.out.println("  车道编号: " + vehicleData.getLaneNumber());
                            System.out.println("  车牌号: " + vehicleData.getLicensePlate());
                            System.out.println("  方向: " + vehicleData.getDirection().getDescription());
                            System.out.println("  排队长度: " + vehicleData.getQueueLength() + "m");
                            System.out.println("  速度: " + vehicleData.getSpeed() + "km/h");
                            System.out.println("  时间戳: " + vehicleData.getTimestamp());

                            System.out.println("✓ 车辆数据解析成功");
                        } else {
                            System.out.println("⚠ 无法解析车辆数据");
                        }

                    } else {
                        System.out.println("消息类型: 未知 (0x" + String.format("%02X", dataType) + ")");
                    }
                }

                // 显示原始数据
                System.out.println("原始数据: " + formatBytes(message.getData()));

            } catch (DataParseException e) {
                System.err.println("✗ 数据解析失败: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("✗ 处理消息时出错: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("================\n");
        }

        private String formatBytes(byte[] bytes) {
            if (bytes.length == 0) return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < Math.min(bytes.length, 32); i++) {
                if (i > 0) sb.append(", ");
                sb.append(String.format("0x%02X", bytes[i] & 0xFF));
            }
            if (bytes.length > 32) {
                sb.append(", ...(" + bytes.length + " bytes)");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * 通过反射设置私有字段（简单实现）
     */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            System.err.println("警告: 无法设置字段 " + fieldName + ": " + e.getMessage());
            // 如果反射失败，尝试其他方式或忽略
        }
    }
}
