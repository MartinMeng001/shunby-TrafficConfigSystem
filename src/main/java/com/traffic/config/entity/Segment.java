package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;

public class Segment {
    private int segmentId;
    private String name;
    private String upsigid;
    private String downsigid;
    private int length;
    private int minRed;
    private int maxRed;
    private int minGreen;
    private int maxGreen;


    @XmlElement(name = "segmentId")
    public int getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(int segmentId) {
        this.segmentId = segmentId;
    }
    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "upsigid")
    public String getUpsigid() {
        return upsigid;
    }

    public void setUpsigid(String upsigid) {
        this.upsigid = upsigid;
    }
    @XmlElement(name="downsigid")
    public String getDownsigid() {
        return downsigid;
    }

    public void setDownsigid(String downsigid) {
        this.downsigid = downsigid;
    }

    @XmlElement(name = "length")
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }

    @XmlElement(name = "minRed")
    public int getMinRed() {
        return minRed;
    }
    public void setMinRed(int minRed) {
        this.minRed = minRed;
    }

    @XmlElement(name = "maxRed")
    public int getMaxRed() {
        return maxRed;
    }
    public void setMaxRed(int maxRed) {
        this.maxRed = maxRed;
    }

    @XmlElement(name = "minGreen")
    public int getMinGreen() {
        return minGreen;
    }
    public void setMinGreen(int minGreen) {
        this.minGreen = minGreen;
    }

    @XmlElement(name = "maxGreen")
    public int getMaxGreen() {
        return maxGreen;
    }
    public void setMaxGreen(int maxGreen) {
        this.maxGreen = maxGreen;
    }
}