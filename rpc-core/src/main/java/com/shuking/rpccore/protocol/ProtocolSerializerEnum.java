package com.shuking.rpccore.protocol;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 协议序列化器
 */
@Getter
public enum ProtocolSerializerEnum {

    JDK(0, "jdk"),
    JSON(1, "json"),
    KRYO(2, "kryo"),
    HESSIAN(3, "hessian");

    private final int key;
    private final String value;

    ProtocolSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value value
     * @return enum
     */
    public static ProtocolSerializerEnum getEnumByValue(String value) {
        for (ProtocolSerializerEnum item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取序列化器列表
     */
    public static List<String> getSerializerList() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据key获取枚举
     *
     * @param key key
     * @return enum
     */
    public static ProtocolSerializerEnum getEnumByKey(int key) {
        for (ProtocolSerializerEnum item : values()) {
            if (item.key == key) {
                return item;
            }
        }
        return null;
    }
}
