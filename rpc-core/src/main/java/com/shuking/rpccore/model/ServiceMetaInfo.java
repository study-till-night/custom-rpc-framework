package com.shuking.rpccore.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模块服务信息
 */
@Data
@Builder
// @Component
// @EnableConfigurationProperties
// @ConfigurationProperties("rpc.service")
public class ServiceMetaInfo {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务版本
     */
    private String serviceVersion = "1.0.0";

    /**
     * 服务主机
     */
    private String serviceHost;

    /**
     * 服务端口
     */
    private String servicePort;

    /**
     * 获取服务键名
     * @return
     */
    public String getServiceKey() {
        return String.format("%s-%s", serviceName, serviceVersion);
    }

    /**
     * 获取redis中存储的服务键值  ex. service-v1.0:localhost-6379
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s:%s-%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 获取可调用的服务地址
     * @return
     */
    public String getServiceAddress() {
        if (!serviceHost.contains("http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
