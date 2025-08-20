package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "global")
public class GlobalConfig {
    private int allRed;
    private int maxAllRed;
    private String platformUrl;
    private SignalControllerList regionList;

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

    @XmlElement(name = "PlatformUrl")
    public String getPlatformUrl() {
        return platformUrl;
    }
    public void setPlatformUrl(String platformUrl) {
        this.platformUrl = platformUrl;
    }

    @XmlElement(name = "RegionList")
    public SignalControllerList getRegionList() {
        return regionList;
    }

    public void setRegionList(SignalControllerList regionList) {
        this.regionList = regionList;
    }
    // 便利方法：获取区域名称列表
    public List<String> getRegionNames() {
        if (regionList != null && regionList.getRegions() != null) {
            return regionList.getRegions().stream()
                    .map(Signal::getName)
                    .collect(java.util.stream.Collectors.toList());
        }
        return new ArrayList<>();
    }
}