package com.shuking.rpccore.fault.tolerant.impl;

import com.shuking.rpccore.fault.tolerant.TolerantStrategy;
import com.shuking.rpccore.model.RpcResponse;

import java.util.Map;

/**
 * 快读失败 --直接告诉调用方发生错误
 */
public class FastFailTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务调用出错", e);
    }
}
