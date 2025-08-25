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
    private SignalControllerList signalList;

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

    @XmlElement(name = "SignalControllerList")
    public SignalControllerList getSignalList() {
        return signalList;
    }

    public void setSignalList(SignalControllerList signalList) {
        this.signalList = signalList;
    }
    // 便利方法：获取区域名称列表
    public List<String> getSignalNames() {
        if (signalList != null && signalList.getSignals() != null) {
            return signalList.getSignals().stream()
                    .map(Signal::getName)
                    .collect(java.util.stream.Collectors.toList());
        }
        return new ArrayList<>();
    }
}