package com.traffic.config.websocket.events;

import com.traffic.config.statemachinev3.enums.segment.SegmentState;
import com.traffic.config.statemachinev3.variables.SegmentVariables;
import org.springframework.context.ApplicationEvent;

/**
 * 车辆进入事件
 */
public class VehicleEnterEvent extends ApplicationEvent {
    private final int segmentId;
    private final String vehicleId;
    private final SegmentVariables.Direction direction;

    public VehicleEnterEvent(Object source, int segmentId, String vehicleId, SegmentVariables.Direction direction) {
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
        return String.format("VehicleEnterEvent{segmentId=%d, vehicleId='%s', direction=%s}",
                segmentId, vehicleId, direction);
    }
}
