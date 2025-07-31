package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

public class Region {
    private String name;

    public Region() {
        // 无参构造函数
    }

    public Region(String name) {
        this.name = name;
    }

    @XmlValue
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
