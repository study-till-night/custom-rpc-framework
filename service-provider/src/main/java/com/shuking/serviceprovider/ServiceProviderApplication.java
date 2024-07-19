package com.shuking.serviceprovider;

import com.shuking.common.services.PlayerService;
import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.registry.LocalRegistry;
import com.shuking.rpccore.registry.RegistryFactory;
import com.shuking.rpccore.registry.RemoteRegistry;
import com.shuking.rpccore.server.VertxHttpServer;
import com.shuking.serviceprovider.service.impl.PlayerServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceProviderApplication {


    public static void main(String[] args) {

        // RPC框架初始化
        RpcCoreApplication.init();

        // 进行本地服务注册   注意值为实现类而不是接口
        LocalRegistry.register(PlayerService.class.getName(), PlayerServiceImpl.class);

        // 获取服务中心
        RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();
        RemoteRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());

        ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder().serviceName(PlayerService.class.getName())
                .serviceVersion(rpcConfig.getVersion())
                .servicePort(String.valueOf(rpcConfig.getPort()))
                .serviceHost(rpcConfig.getServerHost()).build();
        // 进行远程服务注册
        registry.registry(serviceMetaInfo);

        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(rpcConfig.getPort());

    }

}