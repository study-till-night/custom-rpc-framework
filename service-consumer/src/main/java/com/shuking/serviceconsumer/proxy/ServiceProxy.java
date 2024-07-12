package com.shuking.serviceconsumer.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;


import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.serializer.JdkSerializer;
import com.shuking.rpccore.serializer.Serializer;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理 rpc请求发送的第一步
 */
@Log4j2
public class ServiceProxy implements InvocationHandler {

    /**
     * 实现InvocationHandler接口对方法进行增强
     * 执行service层方法 会转而经过下方流程 使用post请求访问服务提供者的web接口
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Serializer jdkSerializer = new JdkSerializer();

        RpcRequest rpcRequest = RpcRequest.builder().serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args).build();

        // 向服务提供者发送请求
        try {
            byte[] bytes = jdkSerializer.serialize(rpcRequest);
            // todo 使用注册中心动态传递服务地址
            HttpResponse response = HttpRequest.post("http://localhost:8080").body(bytes).execute();
            byte[] responseBytes = response.bodyBytes();
            RpcResponse rpcResponse = jdkSerializer.deserialize(responseBytes, RpcResponse.class);
            return rpcResponse.getData();
        } catch (Exception e) {
            log.error("动态代理发送请求失败:{}", e.getMessage());
        }
        return null;
    }
}
