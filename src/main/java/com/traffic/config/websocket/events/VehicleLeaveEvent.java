package com.traffic.config.websocket.events;

import com.traffic.config.cardetector.model.VehicleEvent;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import org.springframework.context.ApplicationEvent;

/**
 * 车辆离开事件
 */
public class VehicleLeaveEvent extends ApplicationEvent {
    private final int segmentId;
    private final String vehicleId;
    private final SegmentVariables.Direction direction;

    public VehicleLeaveEvent(Object source, int segmentId, String vehicleId, SegmentVariables.Direction direction) {
        super(source);
        this.segmentId = segmentId;
        this.vehicleId = vehicleId;
        this.direction = direction;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public SegmentVariables.Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return String.format("VehicleLeaveEvent{segmentId=%d, vehicleId='%s', direction=%s}",
                segmentId, vehicleId, direction);
    }
}
