package com.shuking.rpccore.fault.retry.impl;

import com.github.rholder.retry.*;
import com.shuking.rpccore.fault.retry.RetryStrategy;
import com.shuking.rpccore.model.RpcResponse;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Log4j2
public class FixedIntervalRetry implements RetryStrategy {
    public static void main(String[] args) {
        try {
            RpcResponse rpcResponse = new FixedIntervalRetry().doRemoteCall(() -> {
                throw new RuntimeException("模拟请求出错");
            });
            log.info(rpcResponse);
        } catch (RetryException e) {
            log.error("多次重试失败--,失败次数--{}", e.getNumberOfFailedAttempts());
        } catch (ExecutionException e) {
            log.error("重试执行失败");
        }
    }

    @Override
    public RpcResponse doRemoteCall(Callable<RpcResponse> callable) throws ExecutionException, RetryException {

        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfRuntimeException()
                // 使用固定间隔3s 重试3次
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("正在执行第{}次尝试--", attempt.getAttemptNumber());
                    }
                }).build();
        return retryer.call(callable);
    }
}
