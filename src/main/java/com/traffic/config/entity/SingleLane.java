package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SingleLane")
public class SingleLane {
    private GlobalConfig global;
    private Segments segments;

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
}