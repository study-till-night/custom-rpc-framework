package com.shuking.rpccore.registry;

import com.shuking.rpccore.config.RegistryConfig;
import com.shuking.rpccore.model.ServiceMetaInfo;

import java.util.List;

public interface RemoteRegistry {

    /**
     * 注册中心初始化  每个服务都调用一次
     * @param registryConfig    注册中心配置信息
     */
    void init(RegistryConfig registryConfig) throws Exception;

    /**
     * 服务注册
     * @param serviceMetaInfo   服务自身信息
     */
    void registry(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务注销
     * @param serviceMetaInfo
     */
    void unRegistry(ServiceMetaInfo serviceMetaInfo);

    /**
     * 获取服务
     * @param serviceKey    注册中心中存储的key
     */
    List<ServiceMetaInfo> getService(String serviceKey);

    /**
     * 销毁连接
     */
    void centerBoom();
}
