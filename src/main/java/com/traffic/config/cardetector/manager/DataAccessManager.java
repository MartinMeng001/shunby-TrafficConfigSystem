package com.traffic.config.cardetector.manager;

import com.traffic.config.cardetector.model.ProtocolMessage;
import com.traffic.config.cardetector.model.VehicleData;
import com.traffic.config.cardetector.parser.VehicleDataParser;
import com.traffic.config.entity.DetectPoint;
import com.traffic.config.exception.DataParseException;
import com.traffic.config.service.ConfigService;
import com.traffic.config.statemachinev3.enums.segment.SegmentEvent;
import com.traffic.config.statemachinev3.threading.SegmentStateMachineV3Service;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DataAccessManager {

    @Autowired
    private VehicleDataParser vehicleDataParser;

    @Autowired
    private SegmentStateMachineV3Service segmentStateMachineService;

    @Autowired
    private ConfigService configService;

    /*
    *  1. 要关联检测点与路段
    *  2. 监测点要设置 方向及进入，退出
    * */

    public void processMessage(ProtocolMessage message){
//        System.out.println("\n=== 收到消息 ===");
//        System.out.println("时间: " + message.getReceiveTime());
//        System.out.println("来源IP: " + message.getClientAddress());
//        System.out.println("数据长度: " + message.getDataLength());

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

//                        System.out.println("解析结果:");
//                        System.out.println("  数据类型: " + vehicleData.getDataType());
//                        System.out.println("  信号机IP: " + vehicleData.getSignalIp());
//                        System.out.println("  车道编号: " + vehicleData.getLaneNumber());
//                        System.out.println("  车牌号: " + vehicleData.getLicensePlate());
//                        System.out.println("  方向: " + vehicleData.getDirection().getDescription());
//                        System.out.println("  排队长度: " + vehicleData.getQueueLength() + "m");
//                        System.out.println("  速度: " + vehicleData.getSpeed() + "km/h");
//                        System.out.println("  时间戳: " + vehicleData.getTimestamp());
//
//                        System.out.println("✓ 车辆数据解析成功");
//
//                        DetectPoint point = getDetectPointByIndex(vehicleData.getLaneNumber());
//                        if(point == null){ return;}
//                        SegmentEvent event = makeSegmentEvent(point, vehicleData.getRunDirection());
//                        if(event == null){ return;}
//                        Map<String, Object> eventData = makeEventData(point, vehicleData.getRunDirection(), vehicleData.getLicensePlate());
//                        if(eventData == null){ return;}
                        //segmentStateMachineService.processSegmentEventAsync(point.getSegmentId(), event, eventData);
                    } else {
                        System.out.println("⚠ 无法解析车辆数据");
                    }

                } else {
                    System.out.println("消息类型: 未知 (0x" + String.format("%02X", dataType) + ")");
                }
            }

            // 显示原始数据
            //System.out.println("原始数据: " + formatBytes(message.getData()));

        } catch (DataParseException e) {
            System.err.println("✗ 数据解析失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ 处理消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("================\n");
    }
    //
    private DetectPoint getDetectPointByIndex(int index){
        if(configService==null) return null;
        Optional<DetectPoint> point = configService.getDetectPointByIndex(index);
        if(point.isPresent()) return point.get();
        return null;
    }
    private SegmentEvent makeSegmentEvent(DetectPoint point, int runDirection){
        if(point==null) return null;
        if(point.getDirection().equalsIgnoreCase("up")){    // 上行检测点逻辑
            String inout = point.getInout();
            if("in".equalsIgnoreCase(inout)){
                if(runDirection==0){ return null; }
                if(runDirection==1){ return SegmentEvent.VEHICLE_ENTER_UPSTREAM; }
                if(runDirection==2){ return SegmentEvent.VEHICLE_EXIT_DOWNSTREAM; }
            }else if("out".equalsIgnoreCase(inout)){
                if(runDirection==0){ return null; }
                if(runDirection==1){ return SegmentEvent.VEHICLE_EXIT_UPSTREAM; }
                if(runDirection==2){ return SegmentEvent.VEHICLE_ENTER_DOWNSTREAM; }
            }
            return null;
        }
        if(point.getDirection().equalsIgnoreCase("down")){    // 上行检测点逻辑
            String inout = point.getInout();
            if("in".equalsIgnoreCase(inout)){
                if(runDirection==0){ return null; }
                if(runDirection==1){ return SegmentEvent.VEHICLE_ENTER_DOWNSTREAM; }
                if(runDirection==2){ return SegmentEvent.VEHICLE_EXIT_UPSTREAM; }
            }else if("out".equalsIgnoreCase(inout)){
                if(runDirection==0){ return null; }
                if(runDirection==1){ return SegmentEvent.VEHICLE_EXIT_DOWNSTREAM; }
                if(runDirection==2){ return SegmentEvent.VEHICLE_ENTER_UPSTREAM; }
            }
        }
        return null;
    }

    private Map<String, Object> makeEventData(DetectPoint point, int runDirection, String carId){
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("vehicleId", carId);
        if(point==null) return null;
        if(point.getDirection().equalsIgnoreCase("up")){    // 上行检测点逻辑
            if(runDirection==0){ return null; }
            if(runDirection==1){ eventData.put("direction", SegmentVariables.Direction.UPSTREAM);return eventData; }
            if(runDirection==2){ eventData.put("direction", SegmentVariables.Direction.DOWNSTREAM);return eventData; }

            return null;
        }
        if(point.getDirection().equalsIgnoreCase("down")){    // 上行检测点逻辑
            if(runDirection==0){ return null; }
            if(runDirection==1){ eventData.put("direction", SegmentVariables.Direction.DOWNSTREAM);return eventData; }
            if(runDirection==2){ eventData.put("direction", SegmentVariables.Direction.UPSTREAM);return eventData; }

        }
        return null;
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
