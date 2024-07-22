package com.shuking.rpccore.protocol;

import lombok.Getter;

@Getter
public enum ProtocolMsgStatusEnum {

    SUCCESS("suceess", 200),
    BAD_REQUEST("bad_request", 401),
    INTERNAL_ERROR("internal_error", 500);


    private final String msg;
    private final int status;

    ProtocolMsgStatusEnum(String msg, int status) {
        this.msg = msg;
        this.status = status;
    }

    /**
     * 根据status获取枚举
     * @param status    状态
     * @return enum
     */
    public static ProtocolMsgStatusEnum getEnumByStatus(int status) {
        for (ProtocolMsgStatusEnum value : values()) {
            if (value.status == status) {
                return value;
            }
        }
        return null;
    }
}
