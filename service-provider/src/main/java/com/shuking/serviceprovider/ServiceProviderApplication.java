package com.shuking.serviceprovider;

import com.shuking.common.services.PlayerService;
import com.shuking.rpccore.bootstrap.ProviderBootStrap;
import com.shuking.rpccore.registry.ServiceRegisterInfo;
import com.shuking.rpcspringbootstarter.annotation.EnableRpc;
import com.shuking.serviceprovider.service.impl.PlayerServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableRpc
public class ServiceProviderApplication {

    // private static final List<ServiceRegisterInfo<?>> serviceList = Arrays.asList(new ServiceRegisterInfo<>(PlayerService.class.getName(), PlayerServiceImpl.class));

    public static void main(String[] args) {
        // ProviderBootStrap.init(serviceList);
        SpringApplication.run(ServiceProviderApplication.class, args);
    }

}