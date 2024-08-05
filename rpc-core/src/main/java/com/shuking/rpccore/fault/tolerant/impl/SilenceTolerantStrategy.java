package com.shuking.rpccore.fault.tolerant.impl;

import com.shuking.rpccore.fault.tolerant.TolerantStrategy;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.model.ServiceMetaInfo;
import lombok.extern.log4j.Log4j2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 静默处理    进行日志记录 但正常返回响应
 */
@Log4j2
public class SilenceTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        ServiceMetaInfo serviceMetaInfo = (ServiceMetaInfo) context.get("service");
        log.error("服务调用发生错误--{},被调用的服务--{},时间--{}", e.getMessage(), serviceMetaInfo == null ? "未知" : serviceMetaInfo.getServiceName()
                , new SimpleDateFormat().format(new Date()));
        return new RpcResponse();
    }
}
