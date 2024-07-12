package com.shuking.serviceconsumer.proxy;

import com.shuking.rpccore.RpcCoreApplication;

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
        // 若开启 mock 则获取对应的mockproxy
        Boolean isMock = RpcCoreApplication.getRpcConfig().getMock();
        if (isMock) {
            return getMockProxy(serviceClass);
        }

        Object serviceProxy = Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass}, new ServiceProxy());
        if (serviceProxy != null) {
            return (T) serviceProxy;
        }
        return null;
    }

    /**
     * 开启MOCK后的的代理类
     *
     * @param serviceClass
     * @param <T>
     * @return 代理增强后的服务类
     */
    public static <T> T getMockProxy(Class<T> serviceClass) {
        Object serviceProxy = Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass}, new MockServiceProxy());
        if (serviceProxy != null) {
            return (T) serviceProxy;
        }
        return null;
    }
}
