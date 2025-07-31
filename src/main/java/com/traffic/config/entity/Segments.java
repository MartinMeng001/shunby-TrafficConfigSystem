package com.traffic.config.entity;

import javax.xml.bind.annotation.*;
import java.util.List;

//@XmlRootElement(name = "segments")
@XmlAccessorType(XmlAccessType.FIELD)
public class Segments {
    @XmlAttribute(name = "size")
    private int size;
    @XmlElement(name = "segment")
    private List<Segment> segmentList;

    public int getSize() {
        return size;
        //return segmentList != null ? segmentList.size() : 0;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<Segment> segmentList) {
        this.segmentList = segmentList;
        // 确保size属性与实际列表大小一致
        if (segmentList != null) {
            this.size = segmentList.size();
        } else {
            this.size = 0;
        }
    }

    // 添加一个便于调试的toString方法
    @Override
    public String toString() {
        return String.format("Segments{size=%d, actualListSize=%d}",
                size,
                segmentList != null ? segmentList.size() : 0);
    }
}