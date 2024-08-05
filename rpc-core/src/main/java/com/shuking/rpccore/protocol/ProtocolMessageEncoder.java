package com.shuking.rpccore.protocol;

import com.shuking.rpccore.constant.ProtocolConstants;
import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.serializer.Serializer;
import com.shuking.rpccore.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 消息编解码
 */
public class ProtocolMessageEncoder {

    /**
     * 对协议数据结构进行编码
     *
     * @param protocolMessage 协议消息
     * @return Buffer
     * @throws IOException
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }

        ProtocolMessage.Header header = protocolMessage.getHeader();

        // 向缓冲区写入字节 按照设计好的顺序写入
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        ProtocolSerializerEnum serializerEnum = ProtocolSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化器不存在");
        }

        // 对消息体进行序列化
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        Object body = protocolMessage.getBody();
        byte[] bodyBytes = serializer.serialize(body);
        // 写入数据体长度和数据体
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);

        return buffer;
    }

    /**
     * 对协议数据结构进行解码
     *
     * @param buffer buffer
     * @return 协议消息
     */
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        if (buffer == null) {
            return null;
        }

        // 优先检验magic合法性
        byte magic = buffer.getByte(0);
        if (magic != ProtocolConstants.MAGIC_NUMBER) {
            throw new RuntimeException("消息magic非法");
        }

        ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();

        // 设置请求头参数
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));

        // 设置请求体
        protocolMessage.setHeader(header);
        // 读取请求体 顺便解决粘包问题 超出长度的部分将留给下次处理
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());

        ProtocolSerializerEnum serializerEnum = ProtocolSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化器不存在");
        }
        // 对消息体进行反序列化
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolTypeEnum protocolTypeEnum = ProtocolTypeEnum.getEnumByType(header.getType());
        if (protocolTypeEnum == null) {
            throw new RuntimeException("消息类型不存在");
        }
        switch (protocolTypeEnum) {
            // 类型为请求
            case REQUEST -> {
                RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                protocolMessage.setBody(rpcRequest);
            }
            // 类型为响应
            case RESPONSE -> {
                RpcResponse rpcResponse = serializer.deserialize(bodyBytes, RpcResponse.class);
                protocolMessage.setBody(rpcResponse);
            }
            // 类型为心跳检测
            case HEART_BEAT -> {
                // todo    待实现
            }
            // 其它类型
            case OTHERS -> {
                // todo    待实现
            }
            default -> throw new RuntimeException("暂不支持该消息类型");
        }

        return protocolMessage;
    }
}