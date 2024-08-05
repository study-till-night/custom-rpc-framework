package com.shuking.rpccore.fault.tolerant;


import cn.hutool.core.util.StrUtil;
import com.shuking.rpccore.fault.retry.RetryStrategy;
import com.shuking.rpccore.fault.tolerant.impl.FastFailTolerantStrategy;
import com.shuking.rpccore.utils.SpiUtil;

/**
 * 重试策略工厂（用于获取重试器对象）
 */
public class TolerantStrategyFactory {

    /**
     * 默认重试器
     */
    private static final TolerantStrategy DEFAULT_RETRY_STRATEGY = new FastFailTolerantStrategy();

    static {
        SpiUtil.loadSingle(TolerantStrategy.class);
    }

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static TolerantStrategy getInstance(String key) {
        if (StrUtil.isBlank(key)) {
            return DEFAULT_RETRY_STRATEGY;
        }
        return SpiUtil.getInstance(TolerantStrategy.class, key);
    }

}
