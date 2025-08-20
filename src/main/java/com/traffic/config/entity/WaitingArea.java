package com.traffic.config.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class WaitingArea {
    @XmlElement(name = "index")
    private int index;

    @XmlElement(name = "upCapacity")
    private int upCapacity;

    @XmlElement(name = "downCapacity")
    private int downCapacity;
}
