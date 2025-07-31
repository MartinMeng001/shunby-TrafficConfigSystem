package com.traffic.config.service.event;

import com.traffic.config.entity.RegionList;
import org.springframework.context.ApplicationEvent;
/*这个不会也不允许修改，只在init时初始化一次即可*/
public class SignalListEvent extends ApplicationEvent {
    private final RegionList regionList;

    /**
     * @param source 发生事件的对象，通常是 this
     * @param regionList 信号机列表
     */
    public SignalListEvent(Object source, RegionList regionList) {
        super(source);
        this.regionList = regionList;
    }
    public RegionList getRegionList() {
        return regionList;
    }
    @Override
    public String toString(){
        return "SignalListEventUpdateEvent{newSignalList=" + regionList + "}";
    }
}
