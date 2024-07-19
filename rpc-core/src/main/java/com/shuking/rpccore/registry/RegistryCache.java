package com.shuking.rpccore.registry;

import com.shuking.rpccore.model.ServiceMetaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务结点缓存
 */
public class RegistryCache {

    private final Map<String, List<ServiceMetaInfo>> serviceMetaInfoMap = new HashMap<>();

    /**
     * @param serviceName 服务名称
     * @return
     */
    public List<ServiceMetaInfo> getCache(String serviceName) {
        return serviceMetaInfoMap.get(serviceName);
    }

    /**
     * 设置缓存
     *
     * @param serviceName
     * @param serviceMetaInfoList
     */
    public void setCache(String serviceName, List<ServiceMetaInfo> serviceMetaInfoList) {
        serviceMetaInfoMap.put(serviceName, serviceMetaInfoList);
    }

    /**
     * 删除某一服务缓存
     */
    public void removeCache(String serviceName) {
        serviceMetaInfoMap.remove(serviceName);
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        serviceMetaInfoMap.clear();
    }
}
