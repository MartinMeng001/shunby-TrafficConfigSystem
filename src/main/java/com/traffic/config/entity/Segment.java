package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;

public class Segment {
    private String name;
    private String sigid;
    private int allred;
    private int upctrl;
    private int downctrl;
    private int inzone;
    private int outzone;

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "sigid")
    public String getSigid() {
        return sigid;
    }

    public void setSigid(String sigid) {
        this.sigid = sigid;
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