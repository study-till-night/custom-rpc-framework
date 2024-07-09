package com.shuking.serviceconsumer.proxy;

import java.lang.reflect.Proxy;

@SuppressWarnings("all")
public class ServiceProxyFactory {

    /**
     * 根据传入的service名称得到代理增强后的服务类
     *
     * @param serviceClass
     * @param <T>
     * @return 代理增强后的服务类
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        if (Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new ServiceProxy()) != null) {
            return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new ServiceProxy());
        }
        return null;
    }
}
