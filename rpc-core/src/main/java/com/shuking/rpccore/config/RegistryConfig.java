package com.shuking.rpccore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "rpc.registry")
public class RegistryConfig {

    /**
     * 注册中心实现方式
     */
    private String registryType = "redis";

    /**
     * 注册中心服务地址
     */
    private String centerAddress = "127.0.0.1";

    /**
     * 注册中心服务端口
     */
    private int centerPort = 6379;

    private String userName;

    private String password;

    /**
     * 注册中心连接超时时长
     */
    private Long timeout = 10000L;
}
