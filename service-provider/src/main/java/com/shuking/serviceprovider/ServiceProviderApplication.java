package com.shuking.serviceprovider;

import com.shuking.common.services.PlayerService;
import com.shuking.rpcsimple.registry.LocalRegistry;
import com.shuking.rpcsimple.server.VertxHttpServer;
import com.shuking.serviceprovider.service.impl.PlayerServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceProviderApplication {

    public static void main(String[] args) {
        // SpringApplication.run(ServiceProviderApplication.class, args);
        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(8080);

        // 进行服务注册   注意值为实现类而不是接口
        LocalRegistry.register(PlayerService.class.getName(), PlayerServiceImpl.class);
    }

}