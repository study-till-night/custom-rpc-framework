package com.shuking.rpccore.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisFactory {
    private static JedisPool pool = null;

    // 池基本配置
    private static final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

    static {
        //最大连接
        jedisPoolConfig.setMaxTotal(8);
        //最大空闲连接
        jedisPoolConfig.setMaxIdle(8);
        //最小空闲连接
        jedisPoolConfig.setMinIdle(0);
        //最长等待时间,ms
        jedisPoolConfig.setMaxWaitMillis(200);
    }

    // 获取jedis实例
    public static Jedis getInstance(String address,int port,int timeout) {
        if (pool == null) {
            pool = new JedisPool(jedisPoolConfig, address, port,timeout);
        }
        return pool.getResource();
    }
}


