package com.shuking.rpccore.loadBalancer.impl;

import com.shuking.rpccore.loadBalancer.LoadBalancer;
import com.shuking.rpccore.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡
 */
public class RandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        int size = serviceMetaInfoList.size();
        return serviceMetaInfoList.get(random.nextInt(size));
    }
}
