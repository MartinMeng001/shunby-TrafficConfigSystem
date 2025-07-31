package com.traffic.config.service.event;

import org.springframework.context.ApplicationEvent;

public class ServerUrlUpdateEvent extends ApplicationEvent { // 继承 ApplicationEvent
    private final String newServerUrl;

    /**
     * @param source 发生事件的对象，通常是 this
     * @param newServerUrl 新的服务器URL
     */
    public ServerUrlUpdateEvent(Object source, String newServerUrl) {
        super(source); // 调用父类构造器，传入事件源
        this.newServerUrl = newServerUrl;
    }

    public String getNewServerUrl() {
        return newServerUrl;
    }

    @Override
    public String toString() {
        return "ServerUrlUpdateEvent{newServerUrl='" + newServerUrl + "'}";
    }
}
