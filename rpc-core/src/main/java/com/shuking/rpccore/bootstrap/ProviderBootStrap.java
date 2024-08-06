package com.shuking.rpccore.bootstrap;

import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.registry.LocalRegistry;
import com.shuking.rpccore.registry.RegistryFactory;
import com.shuking.rpccore.registry.RemoteRegistry;
import com.shuking.rpccore.registry.ServiceRegisterInfo;
import com.shuking.rpccore.server.tcp.VertxTcpServer;

import java.util.List;

public class ProviderBootStrap {

    /**
     * 进行服务端初始化
     *
     * @param serviceList 服务列表
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceList) {
        // RPC框架初始化
        RpcCoreApplication.init();
        RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();

        // 将所有服务进行注册
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceList) {
            // 进行本地服务注册   注意值为实现类而不是接口
            LocalRegistry.register(serviceRegisterInfo.getServiceName(), serviceRegisterInfo.getImplClass());

            // 获取服务中心
            RemoteRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());

            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder().serviceName(serviceRegisterInfo.getServiceName())
                    .serviceVersion(rpcConfig.getVersion())
                    .servicePort(String.valueOf(rpcConfig.getPort()))
                    .serviceHost(rpcConfig.getServerHost()).build();
            // 进行远程服务注册
            registry.registry(serviceMetaInfo);
        }

        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getPort());
    }
}
