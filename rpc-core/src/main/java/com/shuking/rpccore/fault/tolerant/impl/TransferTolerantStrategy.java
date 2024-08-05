package com.shuking.rpccore.fault.tolerant.impl;

import cn.hutool.core.util.ObjectUtil;
import com.shuking.rpccore.fault.tolerant.TolerantStrategy;
import com.shuking.rpccore.loadBalancer.LoadBalancer;
import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.server.tcp.VertxTcpClient;

import java.util.*;

/**
 * 故障转移 选择其它结点
 */
public class TransferTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 获取必须的参数
        ServiceMetaInfo serviceMetaInfo = (ServiceMetaInfo) context.get("service");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceList");
        LoadBalancer loadBalancer = (LoadBalancer) context.get("loadBalancer");
        RpcRequest rpcRequest = (RpcRequest) context.get("request");

        // 从剩余结点中选取另外的进行调用
        if (ObjectUtil.isAllNotEmpty(serviceMetaInfo, serviceMetaInfoList, loadBalancer, rpcRequest)) {
            HashSet<String> errorServiceNodes = new HashSet<>(Collections.singletonList(serviceMetaInfo.getServiceAddress()));
            while (true) {
                if (errorServiceNodes.size() >= serviceMetaInfoList.size() / 3) {
                    throw new RuntimeException("超出三分之一的服务不可用", e);
                }
                ServiceMetaInfo newService = loadBalancer.select(new HashMap<>(), serviceMetaInfoList);
                if (!errorServiceNodes.contains(serviceMetaInfo.getServiceAddress())) {
                    try {
                        return VertxTcpClient.doRequest(rpcRequest, newService);
                    } catch (Exception exception) {
                        errorServiceNodes.add(newService.getServiceAddress());
                    }
                }
            }
        }

        // 缺少参数 直接报错
        throw new RuntimeException("服务调用出错", e);
    }
}
