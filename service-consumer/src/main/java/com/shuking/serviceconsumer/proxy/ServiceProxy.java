package com.shuking.serviceconsumer.proxy;

import cn.hutool.core.util.IdUtil;
import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.constant.ProtocolConstant;
import com.shuking.rpccore.constant.RpcConstants;
import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.protocol.ProtocolMessage;
import com.shuking.rpccore.protocol.ProtocolMessageEncoder;
import com.shuking.rpccore.protocol.ProtocolSerializerEnum;
import com.shuking.rpccore.protocol.ProtocolTypeEnum;
import com.shuking.rpccore.registry.RegistryFactory;
import com.shuking.rpccore.registry.RemoteRegistry;
import com.shuking.rpccore.serializer.Serializer;
import com.shuking.rpccore.serializer.SerializerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
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
        // 获取配置文件中的序列化器
        String serializerKey = RpcCoreApplication.getRpcConfig().getSerializer();
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
        RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();
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

        // 随机选取一个结点进行调用
        ServiceMetaInfo serviceMetaInfo = services.get(new Random().nextInt(services.size()));
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

        // 发送tcp请求
        Vertx vertx = Vertx.vertx();
        // 创建tcp客户端
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        // 连接到TCP服务端
        netClient.connect(Integer.parseInt(serviceMetaInfo.getServicePort()), serviceMetaInfo.getServiceHost(), result -> {
            if (result.succeeded()) {
                log.info("客户端成功连接至TCP服务器");
                NetSocket socket = result.result();

                // 用于发送给TCP服务端的消息
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                ProtocolMessage.Header header = new ProtocolMessage.Header();

                // 设置请求头
                header.setMagic(ProtocolConstant.MAGIC_NUMBER);
                header.setVersion(ProtocolConstant.VERSION_NUMBER);
                header.setSerializer((byte) ProtocolSerializerEnum.getEnumByValue(RpcCoreApplication.getRpcConfig().getSerializer()).getKey());
                header.setType((byte) ProtocolTypeEnum.REQUEST.getType());
                header.setRequestId(IdUtil.getSnowflakeNextId());

                protocolMessage.setHeader(header);
                protocolMessage.setBody(rpcRequest);

                // 发送消息
                try {
                    log.info("准备发送消息--");
                    socket.write(ProtocolMessageEncoder.encode(protocolMessage));
                    log.info("发送消息完毕--");
                } catch (IOException e) {
                    log.error("消息体编码出错--{}", e.getMessage());
                    throw new RuntimeException(e);
                }

                // 接收消息
                socket.handler(buffer -> {
                    try {
                        ProtocolMessage<RpcResponse> responseMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageEncoder.decode(buffer);
                        log.info("接收到响应消息--{}",responseMessage.getBody());
                        responseFuture.complete(responseMessage.getBody());
                    } catch (IOException e) {
                        log.error("消息体解码出错--{}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            } else {
                log.error("客户端连接至TCP服务器失败");
            }
        });

        // 进行阻塞 等待获取数据
        try {
            RpcResponse rpcResponse = responseFuture.get();
            netClient.close();
            return rpcResponse.getData();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
