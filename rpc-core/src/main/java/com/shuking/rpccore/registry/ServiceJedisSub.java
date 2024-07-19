package com.shuking.rpccore.registry;

import com.shuking.rpccore.registry.registryImpl.RedisRegistry;
import redis.clients.jedis.JedisPubSub;


public class ServiceJedisSub extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
        System.out.println("Received message: " + message + " on channel: " + channel);

    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        // 当使用模式匹配时会调用这个方法
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

