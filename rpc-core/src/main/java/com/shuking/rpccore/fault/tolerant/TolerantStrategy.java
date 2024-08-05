package com.shuking.rpccore.fault.tolerant;

import com.github.rholder.retry.RetryException;
import com.shuking.rpccore.model.RpcResponse;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 重试机制
 */
public interface TolerantStrategy {

    /**
     * 执行请求
     *
     * @param context 上下文
     * @return 响应
     */
    RpcResponse doTolerant(Map<String, Object> context, Exception e);
}
