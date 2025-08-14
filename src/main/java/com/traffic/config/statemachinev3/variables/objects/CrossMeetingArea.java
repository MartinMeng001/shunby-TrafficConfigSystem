package com.traffic.config.statemachinev3.variables.objects;

public class CrossMeetingArea {
    private final int crossMeetingAreaId;
    private final MeetingArea upMeetingArea;
    private final MeetingArea downMeetingArea;

    public CrossMeetingArea(int crossMeetingAreaId) {
        this.crossMeetingAreaId = crossMeetingAreaId;
        this.upMeetingArea = new MeetingArea(2);
        this.downMeetingArea = new MeetingArea(2);
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
}
