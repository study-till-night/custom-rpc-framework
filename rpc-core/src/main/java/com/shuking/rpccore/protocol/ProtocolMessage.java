package com.shuking.rpccore.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议消息结构
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {

    private Header header;

    /**
     * 数据包
     */
    private T body;

    /**
     * 协议头
     */
    @Data
    public static class Header{

        private byte magic;

        private byte version;

        private byte serializer;

        /**
         * 消息类型
         */
        private byte type;

        private byte status;

        /**
         * 请求id 8字节
         */
        private long requestId;

        /**
         * 数据体长度 4字节
         */
        private int bodyLength;
    }
}
