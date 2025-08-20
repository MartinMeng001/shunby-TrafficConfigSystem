package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SingleLane")
public class SingleLane {
    private GlobalConfig global;
    private Segments segments;
    private DetectPoints detectPoints;
    private WaitingAreas waitingAreas;

    @XmlElement(name = "global")
    public GlobalConfig getGlobal() {
        return global;
    }

    public void setGlobal(GlobalConfig global) {
        this.global = global;
    }

    @XmlElement(name = "segments")
    public Segments getSegments() {
        return segments;
    }

    public void setSegments(Segments segments) {
        this.segments = segments;
    }

    @XmlElement(name = "DetectPoints")
    public DetectPoints getDetectPoints() {
        return detectPoints;
    }
    // 不支持设置
//    public void setDetectPoints(DetectPoints detectPoints) {
//        this.detectPoints = detectPoints;
//    }

    @XmlElement(name = "WaitingAreas")
    public WaitingAreas getWaitingAreas() {
        return waitingAreas;
    }
    public void setWaitingAreas(WaitingAreas waitingAreas) {
        this.waitingAreas = waitingAreas;
    }
}