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
        Object serviceProxy = Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass}, new ServiceProxy());
        if (serviceProxy != null) {
            return (T) serviceProxy;
        }
        return null;
    }
}
