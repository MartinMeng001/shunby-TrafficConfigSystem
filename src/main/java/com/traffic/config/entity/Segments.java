package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class Segments {
    private int size;
    private List<Segment> segmentList;

    @XmlAttribute(name = "size")
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @XmlElement(name = "segment")
    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<Segment> segmentList) {
        this.segmentList = segmentList;
        this.size = segmentList != null ? segmentList.size() : 0;
    }
}