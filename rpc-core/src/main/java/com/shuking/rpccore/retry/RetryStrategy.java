package com.shuking.rpccore.retry;

import com.github.rholder.retry.RetryException;
import com.shuking.rpccore.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 重试机制
 */
public interface RetryStrategy {

    /**
     * 执行请求
     * @param callable  请求方法
     * @return  响应
     */
    RpcResponse doRemoteCall(Callable<RpcResponse> callable) throws ExecutionException, RetryException;
}
