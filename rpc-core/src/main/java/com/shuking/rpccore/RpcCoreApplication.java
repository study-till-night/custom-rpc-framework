package com.shuking.rpccore;

import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.utils.ConfigUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配置读取操作类
 */
@Log4j2
@Component
public class RpcCoreApplication {

    // 单例模式 维护全局唯一配置
    @Autowired
    public static volatile RpcConfig rpcConfig;

    /**
     * 自定义配置初始化
     * @param customRpcConfig 自定义配置
     */
    public static void init(RpcConfig customRpcConfig) {
        rpcConfig = customRpcConfig;
        log.info("配置初始化:{}", rpcConfig.toString());
    }

    /**
     * 默认配置初始化
     */
    public static void init() {
        try {
            rpcConfig = ConfigUtil.loadConfig(RpcConfig.class);
        } catch (Exception e) {
            // 读取配置出错 使用默认配置
            rpcConfig = new RpcConfig();
        }
        log.info("配置初始化:{}", rpcConfig.toString());
    }

    /**
     * 获取当前配置
     * @return
     */
    public static RpcConfig getRpcConfig() {
        // 若为空 则进行初始化
        if (rpcConfig == null) {
            // 添加synchronized关键字 解决线程安全问题
            synchronized (RpcConfig.class) {
                // 双重检验 防止多次初始化
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
