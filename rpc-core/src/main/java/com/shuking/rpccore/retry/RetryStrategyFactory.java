package com.shuking.rpccore.retry;


import cn.hutool.core.util.StrUtil;
import com.shuking.rpccore.retry.impl.FixedIntervalRetry;
import com.shuking.rpccore.utils.SpiUtil;

/**
 * 重试策略工厂（用于获取重试器对象）
 */
public class RetryStrategyFactory {

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new FixedIntervalRetry();

    static {
        SpiUtil.loadSingle(RetryStrategy.class);
    }

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        if (StrUtil.isBlank(key)) {
            return DEFAULT_RETRY_STRATEGY;
        }
        return SpiUtil.getInstance(RetryStrategy.class, key);
    }

}
