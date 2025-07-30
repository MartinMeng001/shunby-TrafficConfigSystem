package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "global")
public class GlobalConfig {
    private int allRed;
    private int maxAllRed;

    @XmlElement(name = "AllRed")
    public int getAllRed() {
        return allRed;
    }

    public void setAllRed(int allRed) {
        this.allRed = allRed;
    }

    @XmlElement(name = "MaxAllRed")
    public int getMaxAllRed() {
        return maxAllRed;
    }

    public void setMaxAllRed(int maxAllRed) {
        this.maxAllRed = maxAllRed;
    }
}