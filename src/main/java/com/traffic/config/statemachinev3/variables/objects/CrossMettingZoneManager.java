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
        allCrossMeetingZone.put(crossMeetingZoneId, new CrossMeetingArea(maxCapacity));
        System.out.println("会车区 " + crossMeetingZoneId + " 已注册，最大容量: " + maxCapacity);
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
     */
    public void upVehicleEnter(int crossMeetingZoneId, String vehicleId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getUpMeetingArea().vehicleEntered(vehicleId);
        }
    }
    public void upVehicleEnterNext(int crossMeetingZoneId, String vehicleId){
        CrossMeetingArea meetingArea  = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getUpMeetingArea().vehicleEntered(vehicleId);
        }
    }
    public void downVehicleEnter(int crossMeetingZoneId, String vehicleId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getDownMeetingArea().vehicleEntered(vehicleId);
        }
    }
    public void downVehicleEnterNext(int crossMeetingZoneId, String vehicleId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getDownMeetingArea().vehicleEntered(vehicleId);
        }
    }
    /**
     * 通知会车区有一辆车离开
     */
    public void upVehicleExit(int crossMeetingZoneId, String vehicleId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getUpMeetingArea().vehicleExited(vehicleId);
        }
    }
    public void downVehicleExit(int crossMeetingZoneId, String vehicleId){
        CrossMeetingArea meetingArea = allCrossMeetingZone.get(crossMeetingZoneId);
        if(meetingArea != null){
            meetingArea.getDownMeetingArea().vehicleExited(vehicleId);
        }
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
}
