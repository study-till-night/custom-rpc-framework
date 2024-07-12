package com.shuking.serviceconsumer.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * MOCK代理增强
 */
public class MockServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法返回类型 输出默认值
        Class<?> returnType = method.getReturnType();
        return getDefaultResult(returnType);
    }


    // 获取默认值
    private Object getDefaultResult(Class<?> classType) {
        // 是否是基础类型
        if (classType.isPrimitive()) {
            if (classType.equals(boolean.class)) {
                return false;
            } else if (classType.equals(int.class)) {
                return 0;
            } else if (classType.equals(short.class)) {
                return (short) 0;
            } else if (classType.equals(long.class)) {
                return 0L;
            } else if (classType.equals(float.class) || classType.equals(double.class)) {
                return 0.0;
            }
        }
        if (classType.equals(String.class)) {
            return "";
        } else if (classType.equals(Date.class)) {
            return new Date();
        }
        // 若为自定义对象 则返回null
        return null;
    }
}
