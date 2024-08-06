package com.shuking.rpcspringbootstarter.bootstrap;

import com.shuking.rpccore.proxy.ServiceProxyFactory;
import com.shuking.rpcspringbootstarter.annotation.RpcReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

@Log4j2
public class RpcConsumerBootStrap implements BeanPostProcessor {

    /**
     * 监听类加载
     *
     * @param bean     the new bean instance
     * @param beanName the name of the bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取标记的Reference注解 注解位于实现类上
        Class<?> beanClass = bean.getClass();
        log.info("bean class-{}", beanClass);
        // 获取调用到的所有服务
        Field[] declaredFields = beanClass.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 生成代理对象
                declaredField.setAccessible(true);
                Object proxy = ServiceProxyFactory.getProxy(declaredField.getType());
                try {
                    declaredField.set(bean, proxy);
                    declaredField.setAccessible(false);
                } catch (IllegalAccessException e) {
                    log.error("生成代理对象失败--{}", e.getMessage());
                    throw new RuntimeException("生成代理对象失败", e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
