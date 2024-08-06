package com.shuking.rpcspringbootstarter.annotation;

import com.shuking.rpcspringbootstarter.bootstrap.RpcConsumerBootStrap;
import com.shuking.rpcspringbootstarter.bootstrap.RpcInitBootStrap;
import com.shuking.rpcspringbootstarter.bootstrap.RpcProviderBootStrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RpcInitBootStrap.class, RpcConsumerBootStrap.class, RpcProviderBootStrap.class})
public @interface EnableRpc {

    /**
     * 是否需要开启tcp服务器
     */
    boolean needServer() default true;

}
