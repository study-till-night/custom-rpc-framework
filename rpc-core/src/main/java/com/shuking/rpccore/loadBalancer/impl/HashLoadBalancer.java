package com.shuking.rpccore.loadBalancer.impl;

import com.shuking.rpccore.loadBalancer.LoadBalancer;
import com.shuking.rpccore.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希
 */
public class HashLoadBalancer implements LoadBalancer {

    // hash环结构
    private final TreeMap<Integer, ServiceMetaInfo> hashMap = new TreeMap<>();

    private final Integer VIRTUAL_NODE_NUM = 1000;

    /**
     * 获取hash值
     *
     * @param object obj
     * @return hash
     */
    private static Integer getHash(Object object) {
        return object.hashCode();
    }


    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // 构建虚拟结点
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                hashMap.put(getHash(serviceMetaInfo.getServiceAddress() + "#" + i * 10000), serviceMetaInfo);
            }
        }

        // 顺时针找到第一个结点
        Map.Entry<Integer, ServiceMetaInfo> serviceMetaInfoEntry = hashMap.ceilingEntry(getHash(requestParams));
        // 如果未匹配则直接返回第一个结点
        if (serviceMetaInfoEntry == null) {
            return hashMap.firstEntry().getValue();
        }
        return serviceMetaInfoEntry.getValue();
    }
}
