package com.shuking.serviceprovider;

import com.shuking.common.services.PlayerService;
import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.registry.LocalRegistry;
import com.shuking.rpccore.server.VertxHttpServer;
import com.shuking.rpccore.utils.ConfigUtil;
import com.shuking.serviceprovider.service.impl.PlayerServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceProviderApplication {

    public static void main(String[] args) {

        // RPC框架初始化
        // RpcConfig rpcConfig = ConfigUtil.loadConfig(RpcConfig.class);
        // RpcCoreApplication.init(rpcConfig);
        RpcCoreApplication.init();

        // 进行服务注册   注意值为实现类而不是接口
        LocalRegistry.register(PlayerService.class.getName(), PlayerServiceImpl.class);

        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(RpcCoreApplication.getRpcConfig().getPort());

    }

}