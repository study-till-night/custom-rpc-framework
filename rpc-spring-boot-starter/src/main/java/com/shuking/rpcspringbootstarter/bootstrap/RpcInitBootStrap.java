package com.shuking.rpcspringbootstarter.bootstrap;

import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.server.tcp.VertxTcpServer;
import com.shuking.rpcspringbootstarter.annotation.EnableRpc;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RpcInitBootStrap implements ImportBeanDefinitionRegistrar {

    // 是否已经扫描
    private static boolean hasScanned = false;

    /**
     * 在spring初始化时检测注解
     *
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (hasScanned) {
            return;
        }
        Map<String, Object> enableRpcAttrs = Optional.ofNullable(importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())).orElse(new HashMap<>());

        boolean needServer = (boolean) enableRpcAttrs.get("needServer");

        RpcCoreApplication.init();
        RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();

        // 开启服务端
        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getPort());
        }
        hasScanned = true;
    }
}
