package com.traffic.config.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WebService适配器配置
 */
@Component
@ConfigurationProperties(prefix = "adapter.webservice")
public class WebServiceAdapterConfig {

    private String defaultPort = "8080";
    private String servicePath = "/SignalListenServer/SignalListenDelegate?wsdl";
    private String nameSpace = "http://webservice/";
    private int connectionTimeout = 30000; // 30秒
    private int readTimeout = 60000; // 60秒
    private boolean enableLogging = true;

    // Getters and Setters

    public String getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(String defaultPort) {
        this.defaultPort = defaultPort;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
