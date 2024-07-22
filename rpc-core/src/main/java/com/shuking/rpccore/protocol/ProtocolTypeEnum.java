package com.shuking.rpccore.protocol;

import lombok.Getter;

/**
 * 协议请求类型
 */
@Getter
public enum ProtocolTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int type;

    ProtocolTypeEnum(int type) {
        this.type = type;
    }

    public static ProtocolTypeEnum getEnumByType(int type) {
        for (ProtocolTypeEnum value : values()) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }
}
