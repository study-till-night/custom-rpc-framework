package com.shuking.rpccore.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.shuking.rpccore.constant.RpcConstants;

public class ConfigUtil {

    public static <T> T loadConfig(Class<T> configClass) {
        return loadConfig(configClass, RpcConstants.DEFAULT_CONFIG_PREFIX, "");
    }

    public static <T> T loadConfig(Class<T> configClass, String prefix) {
        return loadConfig(configClass, prefix, "");
    }

    public static <T> T loadConfig(Class<T> configClass, String prefix, String env) {
        // 对环境进行拼接
        StringBuilder envFileBuilder = new StringBuilder("config-rpc");

        if (StrUtil.isNotBlank(env)) {
            envFileBuilder.append("-" + env);
        }
        // 只支持读取properties格式
        envFileBuilder.append(".properties");
        // 获取配置文件属性
        Props props = new Props(envFileBuilder.toString());
        props.autoLoad(true);

        return props.toBean(configClass, prefix);
    }
}

