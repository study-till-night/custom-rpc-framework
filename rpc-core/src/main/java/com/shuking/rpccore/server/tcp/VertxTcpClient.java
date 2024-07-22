package com.shuking.rpccore.server.tcp;

import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.config.RpcConfig;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import lombok.extern.log4j.Log4j2;

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
                socket.write("duck u!");
                // 处理响应结果
                socket.handler(buffer -> {
                    log.info("received info from server:{}", buffer.toString());
                });
            } else {
                log.error("connect to tcp-server failed:{}", netSocketAsyncResult.cause().toString());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
