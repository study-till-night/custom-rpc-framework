package com.shuking.rpccore.registry;

import cn.hutool.core.util.StrUtil;
import com.shuking.rpccore.registry.registryImpl.RedisRegistry;
import com.shuking.rpccore.utils.SpiUtil;

public class RegistryFactory {

    static {
        SpiUtil.loadSingle(RemoteRegistry.class);
    }

    private static final RemoteRegistry DEFAULT_REGISTRY = new RedisRegistry();

    public static RemoteRegistry getInstance(String key) {
        if (StrUtil.isBlank(key)) {
            return DEFAULT_REGISTRY;
        }
        return SpiUtil.getInstance(RemoteRegistry.class, key);
    }
}
