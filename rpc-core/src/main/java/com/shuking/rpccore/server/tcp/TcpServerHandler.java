package com.shuking.rpccore.server.tcp;


import com.shuking.rpccore.constant.ProtocolConstant;
import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.protocol.ProtocolMessage;
import com.shuking.rpccore.protocol.ProtocolMessageEncoder;
import com.shuking.rpccore.protocol.ProtocolTypeEnum;
import com.shuking.rpccore.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 请求处理器    直接接触生产者方法的部位
 */
@Log4j2
public class TcpServerHandler implements Handler<NetSocket> {

    /**
     * 处理tcp请求
     *
     * @param netSocket tcp连接
     */
    @Override
    public void handle(NetSocket netSocket) {
        netSocket.handler(buffer -> {
            // 对半包和粘包进行判断
            if (buffer == null || buffer.length() == 0) {
                log.error("buffer为空!");
                throw new RuntimeException("buffer为空!");
            }

            // 获取消息体body长度
            int bodyLength = buffer.getInt(13);
            // 判断半包问题
            if (buffer.getBytes().length < ProtocolConstant.MESSAGE_HEADER_LENGTH + bodyLength) {
                log.error("发生半包问题--length={}", buffer.getBytes().length);
                throw new RuntimeException("发生半包问题!");
            }
            // 判断粘包问题
            if (buffer.getBytes().length > ProtocolConstant.MESSAGE_HEADER_LENGTH + bodyLength) {
                log.error("粘包问题--length={}", buffer.getBytes().length);
                throw new RuntimeException("发生粘包问题!");
            }

            log.info("接收到请求消息,length={}", buffer.getBytes().length);

            ProtocolMessage<RpcRequest> protocolMessage;

            try {
                // 通过解码得到协议消息体
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageEncoder.decode(buffer);
            } catch (IOException e) {
                log.error("消息体解码出错--{}", e.getMessage());
                throw new RuntimeException(e);
            }

            // 通过反射执行方法 流程与http实现一致
            RpcResponse rpcResponse = new RpcResponse();
            try {
                RpcRequest rpcRequest = protocolMessage.getBody();
                // 利用反射得到要执行的方法
                Class<?> serviceClass = LocalRegistry.get(rpcRequest.getServiceName());

                Method method = serviceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
                // 得到方法执行结果
                Object result = method.invoke(serviceClass.getDeclaredConstructor().newInstance(), rpcRequest.getParams());

                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage(String.format("成功调用:%s", method.getName()));
                rpcResponse.setException(null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     InstantiationException e) {
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
                log.error("对方法进行反射时报错:{}", e.getMessage());
            }

            // 将rpcResponse进行编码 返回给客户端
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolTypeEnum.RESPONSE.getType());
            // 用于返回的协议消息
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            log.info("返回给客户端的响应--{}", responseProtocolMessage.toString());
            try {
                // 将buffer写回
                Buffer encodedBuffer = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encodedBuffer);
            } catch (IOException e) {
                log.error("消息体编码出错--{}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
