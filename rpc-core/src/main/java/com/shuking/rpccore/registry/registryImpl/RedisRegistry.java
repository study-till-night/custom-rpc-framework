package com.shuking.rpccore.registry.registryImpl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.shuking.rpccore.config.JedisFactory;
import com.shuking.rpccore.config.RegistryConfig;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.constant.RpcConstants;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.registry.RegistryCache;
import com.shuking.rpccore.registry.RemoteRegistry;
import com.shuking.rpccore.registry.ServiceJedisSub;
import com.shuking.rpccore.utils.ConfigUtil;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class RedisRegistry implements RemoteRegistry {

    // 注册中心中保存的服务结点名称集合 即redis键值
    // (提供端)
    private final Set<String> localServiceNodeSet = new HashSet<>();

    // 提供端的服务缓存
    // (消费端)
    private final RegistryCache registryCache = new RegistryCache();

    // 监听的提供端服务的redis key值
    // (消费端)
    private final Set<String> watchedNodeKey = new ConcurrentHashSet<>();

    // redis客户端
    private Jedis jedis;

    /**
     * 注册中心初始化  每个服务都调用一次
     *
     * @param registryConfig 注册中心配置信息
     */
    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        log.info("服务连接注册中心初始化--{}", registryConfig.toString());

        // 实例化Jedis客户端
        try {
            jedis = JedisFactory.getInstance(registryConfig.getCenterAddress(), registryConfig.getCenterPort(), Math.toIntExact(registryConfig.getTimeout()));
            heartBeat();
            // jedis.auth(password);
        } catch (Exception e) {
            throw new Exception(e);
        }

        // 注册JVM的shutdown Hook 在程序退出时执行
        Runtime.getRuntime().addShutdownHook(new Thread(this::centerBoom));
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

        // 将服务注册进redis
        jedis.set(key, JSONUtil.toJsonStr(serviceMetaInfo));

        // 开启一个订阅频道
        if (!localServiceNodeSet.contains(key)) {
            // 获取新jedis实例
            Jedis tempJedis = getJedis();

            tempJedis.publish(key, "hello");
        }
        jedis.expire(key, 30);

        // 将结点名称加入本地集合
        localServiceNodeSet.add(key);
    }

    /**
     * 服务下线
     * @param serviceMetaInfo   服务信息
     */
    @Override
    public void unRegistry(ServiceMetaInfo serviceMetaInfo) {
        log.info("注册中心注销服务--{}", serviceMetaInfo.getServiceName());

        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        String key = RpcConstants.DEFAULT_SERVICE_PREFIX + serviceNodeKey;

        if (jedis.exists(key)) {
            jedis.del(key);
            localServiceNodeSet.remove(key);

            // 向频道发布下线指令
            Jedis tempJedis = getJedis();
            tempJedis.publish(key, "delete");
        }
    }

    /**
     * 获取一个jedis实例
     * @return  jedis
     */
    private static Jedis getJedis() {
        // 获取新jedis实例
        RegistryConfig registryConfig = ConfigUtil.loadConfig(RpcConfig.class).getRegistryConfig();
        return JedisFactory.getInstance(registryConfig.getCenterAddress(), registryConfig.getCenterPort(), Math.toIntExact(registryConfig.getTimeout()));
    }

    /**
     * 获取提供者服务对应结点列表
     * @param serviceKey    注册中心中存储的key
     * @return  列表
     */
    @Override
    public List<ServiceMetaInfo> getService(String serviceKey) {
        // 如果有缓存则直接取出
        List<ServiceMetaInfo> cacheList = registryCache.getCache(serviceKey);
        if (cacheList != null) {
            return cacheList;
        }

        // 得到服务对应前缀 ex. rpc/service:v1.0/
        String prefix = RpcConstants.DEFAULT_SERVICE_PREFIX + serviceKey + ":";
        Set<String> serviceNodeKeys = jedis.keys(prefix + "*");

        List<ServiceMetaInfo> serviceMetaInfoList = serviceNodeKeys.stream().map(nodeKey -> {
            String serviceJson = jedis.get(nodeKey);
            // 消费端开启对提供端结点的监听
            watch(nodeKey);
            return JSONUtil.toBean(serviceJson, ServiceMetaInfo.class);
        }).collect(Collectors.toList());

        // 将服务加入缓存
        registryCache.setCache(serviceKey, serviceMetaInfoList);
        return serviceMetaInfoList;
    }

    /**
     * 断开注册中心连接
     */
    @Override
    public void centerBoom() {
        log.info("断开与注册中心连接--");

        if (jedis != null) {
            // 清除当前服务所有结点
            for (String keyNode : localServiceNodeSet) {
                jedis.del(keyNode);
                log.info("注册中心注销服务:{}", keyNode);
            }
            jedis.close();
        }
        localServiceNodeSet.clear();
        registryCache.clearCache();
    }

    /**
     * 心跳检测
     */
    @Override
    public void heartBeat() {
        // 每十秒执行一次
        // 如果服务提供者一直运行 则会保持注册状态
        CronUtil.schedule("*/10 * * * * *", (Task) () -> {
            for (String keyNode : localServiceNodeSet) {
                String jsonString = jedis.get(keyNode);
                // 该结点已过期
                if (StrUtil.isEmpty(jsonString)) {
                    // 向频道发布下线指令
                    Jedis tempJedis = getJedis();
                    tempJedis.publish(keyNode, "delete");
                    continue;
                }
                ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(jsonString, ServiceMetaInfo.class);

                // 重新注册
                registry(serviceMetaInfo);
                log.info("注册中心续签服务:{}", serviceMetaInfo.getServiceName());
            }
        });

        // 开启秒级别任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 服务监听(消费)
     *
     * @param serviceNodeKey redis服务键值
     */
    @Override
    public void watch(String serviceNodeKey) {
        boolean isNew = watchedNodeKey.add(serviceNodeKey);
        // 第一次调用时加入监听
        if (isNew) {
            new Thread(() -> {
                // 获取新jedis实例
                Jedis tempJedis = getJedis();

                tempJedis.subscribe(new ServiceJedisSub(), serviceNodeKey);
            }).start();
        }
    }
    

    /**
     * 订阅发布内部类
     */
    class ServiceJedisSub extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {
            System.out.println("Received message: " + message + " on channel: " + channel);
            // 结点下线 删除本地缓存
            if (message.equals("delete")) {
                registryCache.removeCache(channel);
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            System.out.println("Subscribed to channel: " + channel + ", currently subscribed channels: " + subscribedChannels);
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            System.out.println("Unsubscribed from channel: " + channel + ", currently subscribed channels: " + subscribedChannels);
        }
    }
}