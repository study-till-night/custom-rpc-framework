package com.shuking.rpcspringbootstarter.annotation;

import com.shuking.rpccore.constant.RpcConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcService {

    Class<?> interfaceClass() default void.class;

    String version() default RpcConstants.DEFAULT_SERVICE_VERSION;
}
