package com.shuking.rpcspringbootstarter.bootstrap;

import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.registry.LocalRegistry;
import com.shuking.rpccore.registry.RegistryFactory;
import com.shuking.rpccore.registry.RemoteRegistry;
import com.shuking.rpcspringbootstarter.annotation.EnableRpc;
import com.shuking.rpcspringbootstarter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class RpcProviderBootStrap implements BeanPostProcessor {

    /**
     * 监听类加载
     *
     * @param bean     the new bean instance
     * @param beanName the name of the bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取标记的service注解 注解位于实现类上
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);

        if (rpcService != null) {
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }

            String serviceName = interfaceClass.getName();
            // 进行本地服务注册   注意值为实现类而不是接口
            LocalRegistry.register(serviceName, beanClass);
            RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();
            // 获取服务中心
            RemoteRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());

            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder().serviceName(serviceName)
                    .serviceVersion(rpcConfig.getVersion())
                    .servicePort(String.valueOf(rpcConfig.getPort()))
                    .serviceHost(rpcConfig.getServerHost()).build();
            // 进行远程服务注册
            registry.registry(serviceMetaInfo);
        }
        // 依旧照常返回
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
