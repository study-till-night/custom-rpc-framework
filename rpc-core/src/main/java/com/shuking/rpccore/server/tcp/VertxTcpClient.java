package com.shuking.rpccore.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.constant.ProtocolConstant;
import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.model.ServiceMetaInfo;
import com.shuking.rpccore.protocol.ProtocolMessage;
import com.shuking.rpccore.protocol.ProtocolMessageEncoder;
import com.shuking.rpccore.protocol.ProtocolSerializerEnum;
import com.shuking.rpccore.protocol.ProtocolTypeEnum;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 只是用来做测试用的
 */
@Log4j2
public class VertxTcpClient {

    public void start() {
        Vertx vertx = Vertx.vertx();

        RpcConfig rpcConfig = RpcCoreApplication.getRpcConfig();

        // 创建客户端
        Integer port = rpcConfig.getPort();
        String serverHost = rpcConfig.getServerHost();

        vertx.createNetClient().connect(8080, "127.0.0.1", netSocketAsyncResult -> {
            if (netSocketAsyncResult.succeeded()) {
                log.info("connect to tcp-server on address:{}", serverHost + ":" + port);

                NetSocket socket = netSocketAsyncResult.result();
                // 向服务器发送消息
                for (int i = 0; i < 1000; i++) {
                    socket.write("duck u!duck u!duck u!duck u!");
                }
                // 处理响应结果
                socket.handler(buffer -> {
                    log.info("received info from server:{}", buffer.toString());
                });
            } else {
                log.error("connect to tcp-server failed:{}", netSocketAsyncResult.cause().toString());
            }
        });
    }

    /**
     * 发送实际请求 利用RecordParser避免半包粘包问题
     * @param rpcRequest    rpc请求体
     * @param serviceMetaInfo   服务信息
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) {
        // 发送tcp请求
        Vertx vertx = Vertx.vertx();
        // 创建tcp客户端
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        // 连接到TCP服务端
        netClient.connect(Integer.parseInt(serviceMetaInfo.getServicePort()), serviceMetaInfo.getServiceHost(), result -> {
            if (result.succeeded()) {
                log.info("客户端成功连接至TCP服务器");
                NetSocket socket = result.result();

                // 用于发送给TCP服务端的消息
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                ProtocolMessage.Header header = new ProtocolMessage.Header();

                // 设置请求头
                header.setMagic(ProtocolConstant.MAGIC_NUMBER);
                header.setVersion(ProtocolConstant.VERSION_NUMBER);
                header.setSerializer((byte) ProtocolSerializerEnum.getEnumByValue(RpcCoreApplication.getRpcConfig().getSerializer()).getKey());
                header.setType((byte) ProtocolTypeEnum.REQUEST.getType());
                header.setRequestId(IdUtil.getSnowflakeNextId());

                protocolMessage.setHeader(header);
                protocolMessage.setBody(rpcRequest);

                // 发送消息
                try {
                    log.info("准备发送消息--");
                    socket.write(ProtocolMessageEncoder.encode(protocolMessage));
                    log.info("发送消息完毕--");
                } catch (IOException e) {
                    log.error("消息体编码出错--{}", e.getMessage());
                    throw new RuntimeException(e);
                }

                // 接收消息 使用RecordParser包装器
                TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                    try {
                        ProtocolMessage<RpcResponse> responseMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageEncoder.decode(buffer);
                        log.info("接收到响应消息--{}", responseMessage.getBody());
                        responseFuture.complete(responseMessage.getBody());
                    } catch (IOException e) {
                        log.error("消息体解码出错--{}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });

                socket.handler(tcpBufferHandlerWrapper);
            } else {
                log.error("客户端连接至TCP服务器失败");
            }
        });

        // 进行阻塞 等待获取数据
        try {
            RpcResponse rpcResponse = responseFuture.get();
            netClient.close();
            return rpcResponse;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
