package com.shuking.rpccore.loadBalancer;

import cn.hutool.core.util.StrUtil;
import com.shuking.rpccore.loadBalancer.impl.RoundRobinLoadBalancer;
import com.shuking.rpccore.utils.SpiUtil;

public class LoadBalancerFactory {

    private static final LoadBalancer DEFAULT_LOADBALANCER = new RoundRobinLoadBalancer();

    static {
        SpiUtil.loadSingle(LoadBalancer.class);
    }

    public static LoadBalancer getInstance(String key) {
        if (StrUtil.isBlank(key)) {
            return DEFAULT_LOADBALANCER;
        }
        return SpiUtil.getInstance(LoadBalancer.class, key);
    }
}
