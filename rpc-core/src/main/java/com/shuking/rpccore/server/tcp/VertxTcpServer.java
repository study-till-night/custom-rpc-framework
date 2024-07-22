package com.shuking.rpccore.server.tcp;

import com.shuking.rpccore.RpcCoreApplication;
import com.shuking.rpccore.serializer.Serializer;
import com.shuking.rpccore.serializer.SerializerFactory;
import com.shuking.rpccore.server.RpcServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class VertxTcpServer implements RpcServer {
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();

        // 创建tcp服务器
        NetServer netServer = vertx.createNetServer();

        //  处理连接    一个连接包含多个请求
        // netServer.connectHandler(netSocket -> {
        //     // 处理请求
        //     netSocket.handler(buffer -> {
        //         log.info("received info from client:{}",buffer.toString());
        //         // 获取请求数据
        //         byte[] requestData = buffer.getBytes();
        //         // 获取配置文件中的序列化器
        //         String serializerKey = RpcCoreApplication.getRpcConfig().getSerializer();
        //         Serializer serializer = SerializerFactory.getInstance(serializerKey);
        //
        //         try {
        //             byte[] response = getResponse(requestData, serializer);
        //             // 向客户端发送数据
        //             netSocket.write(Buffer.buffer(response));
        //         } catch (IOException e) {
        //             log.error("tcp处理响应时报错--{}", e.getMessage());
        //         }
        //     });
        // });

        netServer.connectHandler(new TcpServerHandler());

        // 启动服务监听端口
        netServer.listen(port, result -> {
            if (result.succeeded()) {
                log.info("tcp-server is now listening on port:{}", port);
            } else {
                log.error("tcp-server start failed:{}", result.cause().toString());
            }
        });
    }

    /**
     * 获取响应字节数组
     *
     * @param requestData 发送的请求数据
     * @param serializer  序列化器
     * @return
     */
    private byte[] getResponse(byte[] requestData, Serializer serializer) throws IOException {
        // todo 构造响应数据体
        // todo 并执行业务逻辑
        return serializer.serialize("hello");
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8080);
    }
}