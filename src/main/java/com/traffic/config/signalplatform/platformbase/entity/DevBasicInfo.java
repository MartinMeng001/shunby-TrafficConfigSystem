package com.traffic.config.signalplatform.platformbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// 表示主 JSON 对象中的 devBasicInfo 对象
@Getter
@Setter
public class DevBasicInfo {
    @JsonProperty("beAutoCheckTime")
    private int beAutoCheckTime;
    @JsonProperty("ip")
    private String ip;
    @JsonProperty("longtitude")
    private double longtitude;
    @JsonProperty("sigid")
    private int sigid;
    @JsonProperty("ip4G")
    private String ip4G;
    @JsonProperty("bdLatitude")
    private double bdLatitude;
    @JsonProperty("bdLongtitude")
    private double bdLongtitude;
    @JsonProperty("lastUpdateTime4timechk")
    private String lastUpdateTime4timechk;
    @JsonProperty("powerStatus")
    private int powerStatus;
    @JsonProperty("isMode4G")
    private String isMode4G;
    @JsonProperty("timeDiff4Check")
    private String timeDiff4Check;
    @JsonProperty("online")
    private int online;
    @JsonProperty("timeMode")
    private String timeMode;
    @JsonProperty("time")
    private String time;
    @JsonProperty("lantitude")
    private double lantitude;
}
