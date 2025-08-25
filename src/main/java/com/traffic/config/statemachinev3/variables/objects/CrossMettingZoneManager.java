package com.traffic.config.statemachinev3.variables.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrossMettingZoneManager {
    private static final CrossMettingZoneManager instance = new CrossMettingZoneManager();
    private final Map<Integer, CrossMeetingArea> allCrossMeetingZone = new ConcurrentHashMap<>();

    private CrossMettingZoneManager() {
        // 私有构造函数，防止外部实例化
    }
    public static CrossMettingZoneManager getInstance() {
        return instance;
    }
    /*
     * 注册一个新的会车区
     */
    public void registerCrossing(int crossMeetingZoneId, int maxCapacity){
        allCrossMeetingZone.put(crossMeetingZoneId, new CrossMeetingArea(crossMeetingZoneId, maxCapacity));
        System.out.println("会车区 " + crossMeetingZoneId + " 已注册，最大容量: " + maxCapacity);
    }
    public void updateCrossMeetingCapacity(int crossZoneId, int maxUpCapacity, int maxDownCapacity){
        allCrossMeetingZone.get(crossZoneId).getDownMeetingArea().setMaxCapacity(maxUpCapacity);
        allCrossMeetingZone.get(crossZoneId).getUpMeetingArea().setMaxCapacity(maxDownCapacity);
    }

    public boolean hasUpstreamRequest(int segmentId){
        if(segmentId == 1){ // 路段1没有上行请求，只有下行请求
            return false;
        }
//        if(segmentId == allCrossMeetingZone.size()+1){  // 最后一个路段没有下行请求，只有上行请求
//            if(!allCrossMeetingZone.get(segmentId-1).getUpMeetingArea().isEmpty()) return true;
//        }
        if(segmentId > 1){
            if(!allCrossMeetingZone.get(segmentId-1).getUpMeetingArea().isEmpty()) return true;
        }
        return false;
    }
    public boolean hasDownstreamRequest(int segmentId){
        if(segmentId == allCrossMeetingZone.size()+1){  // 最后一个路段没有下行请求，只有上行请求
            return false;
        }
        if(segmentId <= allCrossMeetingZone.size() && segmentId>0){
            if(!allCrossMeetingZone.get(segmentId).getDownMeetingArea().isEmpty()) return true;
        }
        return false;
    }
    /**
     * 检查指定会车区是否有容量
     */
    public boolean hasUpCapacity(int crossMeetingZoneId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            return meetingArea.getUpMeetingArea().canAcceptVehicle();
        }
        return false;
    }
    public boolean hasDownCapacity(int crossMeetingZoneId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            return meetingArea.getDownMeetingArea().canAcceptVehicle();
        }
        return false;
    }
    /**
     * 通知会车区有一辆车进入
     * 上行，只有路段1到3会产生upVehicleEnter, 同一车牌不应当出现在其它会车区
     * 下行，只有路段2到路段4会产生downVehicleEnter
     */
    public void upVehicleEnterV2(int segmentId, String vehicleId){
        allCrossMeetingZone.forEach((zoneId, meetingArea) -> {
            if(zoneId == segmentId){
                //System.out.println("[upVehicleEnter] segmentId="+segmentId+", vehicleId="+vehicleId);
                meetingArea.getUpMeetingArea().vehicleEntered(vehicleId);
            }else{
                meetingArea.removeVehicleId(vehicleId);
            }
        });
    }
    public void downVehicleEnterV2(int segmentId, String vehicleId){
        allCrossMeetingZone.forEach((zoneId, meetingArea) -> {
            if(zoneId == segmentId-1){
                meetingArea.getDownMeetingArea().vehicleEntered(vehicleId);
            }else{
                meetingArea.removeVehicleId(vehicleId);
            }
        });
    }

    /**
     * 通知会车区有一辆车离开
     * 上行，只有路段1到3会产生upVehicleExit
     * 下行，只有路段2到4会产生downVehicleExit
     */
    public void upVehicleExitV2(int segmentId, String vehicleId){
        if(segmentId < 2 || segmentId > allCrossMeetingZone.size()+1){ return; }
        allCrossMeetingZone.get(segmentId-1).getUpMeetingArea().vehicleExited(vehicleId);
    }
    public void downVehicleExitV2(int segmentId, String vehicleId){
        if(segmentId < 1 || segmentId> allCrossMeetingZone.size())return;
        allCrossMeetingZone.get(segmentId).getDownMeetingArea().vehicleExited(vehicleId);
    }
    /**
     * 通知会车区清空
     */
    public void upVehicleClear(int crossMeetingZoneId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getUpMeetingArea().clear();
        }
    }
    public void downVehicleClear(int crossMeetingZoneId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getDownMeetingArea().clear();
        }
    }
    public MeetingArea getUpMeetingArea(int crossMeetingZoneId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            return meetingArea.getUpMeetingArea();
        }
        return null;
    }
    public MeetingArea getDownMeetingArea(int crossMeetingZoneId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            return meetingArea.getDownMeetingArea();
        }
        return null;
    }

    public Map<Integer, CrossMeetingArea> getAllCrossMeetingZone() {
        return allCrossMeetingZone;
    }

    public void printAllCrossMeetingZones() {
        allCrossMeetingZone.forEach((zoneId, meetingArea) -> {
            System.out.println("会车区ID: " + zoneId + ", 信息: " + meetingArea.toString());
        });
    }
}
