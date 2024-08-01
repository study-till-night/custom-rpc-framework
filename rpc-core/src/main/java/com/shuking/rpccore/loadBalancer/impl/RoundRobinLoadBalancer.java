package com.shuking.rpccore.loadBalancer.impl;

import com.shuking.rpccore.loadBalancer.LoadBalancer;
import com.shuking.rpccore.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轮询实现
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    // k--服务名称 v--计数器
    private final Map<String, Integer> serviceIndexCountMap = new ConcurrentHashMap<>();

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        if (serviceMetaInfoList.size() == 1) {
            return serviceMetaInfoList.get(0);
        }

        ServiceMetaInfo firstNode = serviceMetaInfoList.get(0);
        String serviceName = firstNode.getServiceName();
        // 第一次调用 初始化
        if (!serviceIndexCountMap.containsKey(serviceName)) {
            serviceIndexCountMap.put(serviceName, 1);
            return firstNode;
        }

        // 进行递增并返回
        Integer index = serviceIndexCountMap.get(serviceName);
        serviceIndexCountMap.replace(serviceName, (index + 1) % serviceMetaInfoList.size());
        return serviceMetaInfoList.get(index);
    }
}
