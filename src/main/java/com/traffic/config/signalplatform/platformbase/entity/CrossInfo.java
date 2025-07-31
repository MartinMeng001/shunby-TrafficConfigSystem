package com.traffic.config.signalplatform.platformbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrossInfo {
    @JsonProperty("CrossName")
    private String crossName;
    @JsonProperty("CrossIDExt")
    private String crossIDExt;
    @JsonProperty("SignalIDExt")
    private String signalIDExt;
    @JsonProperty("SignalType")
    private String signalType;
    @JsonProperty("crossid")
    private int crossid;
    @JsonProperty("devBasicInfo")
    private DevBasicInfo devBasicInfo;
}
