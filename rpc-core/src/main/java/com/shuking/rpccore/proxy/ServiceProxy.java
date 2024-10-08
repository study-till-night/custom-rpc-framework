package com.shuking.rpccore.proxy;

import cn.hutool.core.util.ObjectUtil;
import com.github.rholder.retry.RetryException;
import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.constant.RpcConstants;
import com.shuking.rpccore.fault.retry.RetryStrategy;
import com.shuking.rpccore.fault.retry.RetryStrategyFactory;
import com.shuking.rpccore.fault.tolerant.TolerantStrategy;
import com.shuking.rpccore.fault.tolerant.TolerantStrategyFactory;
import com.shuking.rpccore.loadBalancer.LoadBalancer;
import com.shuking.rpccore.loadBalancer.LoadBalancerFactory;
import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.registry.RegistryFactory;
import com.shuking.rpccore.registry.RemoteRegistry;
import com.shuking.rpccore.serializer.Serializer;
import com.shuking.rpccore.serializer.SerializerFactory;
import com.shuking.rpccore.server.tcp.VertxTcpClient;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();

        // 获取配置文件中的序列化器
        String serializerKey = rpcConfig.getSerializer();
        /*
         使用枚举类实现动态获取
        Serializer serializer = SerializerEnum.getSerializerByKey(serializerKey);
         */

        // 使用自定义spi实现动态获取
        Serializer serializer = SerializerFactory.getInstance(serializerKey);

        RpcRequest rpcRequest = RpcRequest.builder().serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args).build();

        // 使用远程注册中心
        RemoteRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());
        ServiceMetaInfo tempServiceInfo = ServiceMetaInfo.builder().serviceName(method.getDeclaringClass().getName())
                .serviceVersion(RpcConstants.DEFAULT_SERVICE_VERSION)
                .build();

        // 获取目标服务所有可用结点
        List<ServiceMetaInfo> services = registry.getService(tempServiceInfo.getServiceKey());
        if (services.isEmpty()) {
            log.error("服务{}暂未有线上结点！", tempServiceInfo.getServiceName());
            throw new Exception(String.format("服务%s暂未有线上结点！", tempServiceInfo.getServiceName()));
        }
        log.info("服务{}共有结点--{}", tempServiceInfo.getServiceName(), services);
        // 随机选取一个结点进行调用
        // ServiceMetaInfo serviceMetaInfo = services.get(new Random().nextInt(services.size()));

        // 使用负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("RequestTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        ServiceMetaInfo serviceMetaInfo = loadBalancer.select(requestParams, services);
        String serviceAddress = serviceMetaInfo.getServiceAddress();
        log.info("向服务{}发送请求,服务信息:{}, 目标地址:{}", tempServiceInfo.getServiceName(), serviceMetaInfo, serviceAddress);

        // 发送http请求
            /*
            HttpResponse response = HttpRequest.post(serviceAddress).body(bytes).execute();
            byte[] responseBytes = response.bodyBytes();
            RpcResponse rpcResponse = serializer.deserialize(responseBytes, RpcResponse.class);
            log.info("收到服务{}回复,数据:{}", tempServiceInfo.getServiceName(),rpcResponse.getData());
            return rpcResponse.getData();
             */

        // 发送请求得到响应
        // 使用重试机制
        RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetry());
        RpcResponse rpcResponse;
        try {
            rpcResponse = retryStrategy.doRemoteCall(() -> VertxTcpClient.doRequest(rpcRequest, serviceMetaInfo));
        } catch (ExecutionException | RetryException e) {
            // 重试过程发生异常 调用服务容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerant());
            rpcResponse = tolerantStrategy.doTolerant(new HashMap<>(), e);
        }

        if (ObjectUtil.isNull(rpcResponse)) {
            throw new RuntimeException("响应为空");
        }
        if (rpcResponse.getException() != null) {
            throw new RuntimeException("服务调用发生异常--" + rpcResponse.getMessage() + "--错误原因--" + rpcResponse.getException());
        }

        log.info("得到服务{}响应数据--{}", serviceMetaInfo.getServiceName(), rpcResponse.getData());
        return rpcResponse.getData();
    }
}
