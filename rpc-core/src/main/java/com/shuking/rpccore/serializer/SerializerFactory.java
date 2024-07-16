package com.shuking.rpccore.serializer;


import cn.hutool.core.util.StrUtil;
import com.shuking.rpccore.serializer.serializerImpl.JdkSerializer;
import com.shuking.rpccore.utils.SpiUtil;

/**
 * 序列化器工厂（工厂模式，用于获取序列化器对象）
 *
 */
public class SerializerFactory {

    // 在类初始化时执行序列化器的spi加载
    static {
        SpiUtil.loadSingle(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        if (StrUtil.isBlank(key)) {
            return DEFAULT_SERIALIZER;
        }
        return SpiUtil.getInstance(Serializer.class, key);
    }

}
