package com.traffic.config.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class DetectPoint {
    @XmlElement(name = "index")
    private int index;
    @XmlElement(name = "segmentId")
    private int segmentId;
    @XmlElement(name = "inout")
    private String inout;
    @XmlElement(name = "direction")
    private String direction;
}
