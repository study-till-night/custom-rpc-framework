package com.shuking.rpccore.loadBalancer;

import com.shuking.rpccore.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

public interface LoadBalancer {

    /**
     * 进行结点选择
     * @param requestParams 请求参数
     * @param serviceMetaInfoList   服务结点列表
     * @return  选择的服务
     */
    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
