package com.shuking.rpccore.registry.registryImpl;

import cn.hutool.json.JSONUtil;
import com.shuking.rpccore.config.RegistryConfig;
import com.shuking.rpccore.constant.RpcConstants;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.registry.RemoteRegistry;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class RedisRegistry implements RemoteRegistry {

    // redis客户端
    private Jedis jedis;

    /**
     * 注册中心初始化  每个服务都调用一次
     *
     * @param registryConfig 注册中心配置信息
     */
    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        log.info("服务连接注册中心初始化--{}",registryConfig.toString());

        String centerAddress = registryConfig.getCenterAddress();
        int centerPort = registryConfig.getCenterPort();
        Long timeout = registryConfig.getTimeout();

        // 实例化Jedis客户端
        try {
            jedis = new Jedis(centerAddress, centerPort, Math.toIntExact(timeout));
            // jedis.auth(password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    /**
     * 服务注册
     *
     * @param serviceMetaInfo 服务自身信息
     */
    @Override
    public void registry(ServiceMetaInfo serviceMetaInfo) {
        log.info("注册中心注册服务--{}", serviceMetaInfo.getServiceName());

        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        String key = RpcConstants.DEFAULT_SERVICE_PREFIX + serviceNodeKey;
        jedis.set(key, JSONUtil.toJsonStr(serviceMetaInfo));
        jedis.expire(key, 60 * 60 * 24 * 10);
    }


    @Override
    public void unRegistry(ServiceMetaInfo serviceMetaInfo) {
        log.info("注册中心注销服务--{}", serviceMetaInfo.getServiceName());

        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        String key = RpcConstants.DEFAULT_SERVICE_PREFIX + serviceNodeKey;

        if (jedis.exists(key)) {
            jedis.del(key);
        }
    }

    @Override
    public List<ServiceMetaInfo> getService(String serviceKey) {
        // 得到服务对应前缀 ex. rpc/service:v1.0/
        String prefix = RpcConstants.DEFAULT_SERVICE_PREFIX + serviceKey + ":";
        Set<String> serviceNodeKeys = jedis.keys(prefix + "*");

        return serviceNodeKeys.stream().map(nodeKey -> {
            String serviceJson = jedis.get(nodeKey);
            return JSONUtil.toBean(serviceJson, ServiceMetaInfo.class);
        }).collect(Collectors.toList());
    }

    @Override
    public void centerBoom() {
        log.info("断开与注册中心连接--");

        if (jedis != null) {
            jedis.close();
        }
    }
}
