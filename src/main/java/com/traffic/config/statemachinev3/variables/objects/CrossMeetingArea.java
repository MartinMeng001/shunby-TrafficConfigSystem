package com.traffic.config.statemachinev3.variables.objects;

public class CrossMeetingArea {
    private final int crossMeetingAreaId;
    private final MeetingArea upMeetingArea;
    private final MeetingArea downMeetingArea;

    public CrossMeetingArea(int crossMeetingAreaId, int maxCapacity) {
        this.crossMeetingAreaId = crossMeetingAreaId;
        this.upMeetingArea = new MeetingArea(maxCapacity);
        this.downMeetingArea = new MeetingArea(maxCapacity);
    }

    public int getCrossMeetingAreaId() {
        return crossMeetingAreaId;
    }
    public MeetingArea getUpMeetingArea() {
        return upMeetingArea;
    }
    public MeetingArea getDownMeetingArea() {
        return downMeetingArea;
    }
    public void removeVehicleId(String vehicleId){
        upMeetingArea.vehicleExited(vehicleId);
        downMeetingArea.vehicleExited(vehicleId);
    }
    public String toString(){
        return String.format("WaitAreaId:%d, Vehicles[UP]:%d, Vehicles[DOWN]:%d\r\n", crossMeetingAreaId, upMeetingArea.getCount(), downMeetingArea.getCount());
    }
}
