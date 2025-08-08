package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;

public class Segment {
    private int segmentId;
    private String name;
    private String upsigid;
    private String downsigid;
    private int allred;
    private int upctrl;
    private int downctrl;
    private int inzone;
    private int outzone;

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
    @XmlElement(name = "allred")
    public int getAllred() {
        return allred;
    }

    public void setAllred(int allred) {
        this.allred = allred;
    }

    @XmlElement(name = "upctrl")
    public int getUpctrl() {
        return upctrl;
    }

    public void setUpctrl(int upctrl) {
        this.upctrl = upctrl;
    }

    @XmlElement(name = "downctrl")
    public int getDownctrl() {
        return downctrl;
    }

    public void setDownctrl(int downctrl) {
        this.downctrl = downctrl;
    }

    @XmlElement(name = "inzone")
    public int getInzone() {
        return inzone;
    }

    public void setInzone(int inzone) {
        this.inzone = inzone;
    }

    @XmlElement(name = "outzone")
    public int getOutzone() {
        return outzone;
    }

    public void setOutzone(int outzone) {
        this.outzone = outzone;
    }
}