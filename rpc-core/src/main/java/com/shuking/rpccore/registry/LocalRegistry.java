package com.shuking.rpccore.registry;

import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class LocalRegistry {

    // 线程安全Map存储服务键值对
    private static final Map<String, Class<?>> registryCenter = new ConcurrentHashMap<>();

    // 服务注册
    public static void register(String serviceName,Class<?> serviceClass){
        if(StrUtil.isBlank(serviceName)){
            log.error("serviceName is empty or blank");
            return;
        }
        registryCenter.put(serviceName, serviceClass);
    }

    // 获取服务class
    public static Class<?> get(String serviceName) {
        return registryCenter.get(serviceName);
    }

    // 移除服务
    public static void remove(String serviceName) {
        registryCenter.remove(serviceName);
    }
}
