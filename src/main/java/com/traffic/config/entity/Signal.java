package com.traffic.config.entity;

import javax.xml.bind.annotation.XmlValue;

public class Signal {
    private String name;

    public Signal() {
        // 无参构造函数
    }

    public Signal(String name) {
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
