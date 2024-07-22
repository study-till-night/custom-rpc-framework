package com.shuking.rpccore.server.http;

import com.shuking.rpccore.server.RpcServer;
import io.vertx.core.Vertx;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VertxHttpServer implements RpcServer {
    @Override
    public void doStart(int port) {
        // 获取http服务器
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();

        // 设置请求处理器
        httpServer.requestHandler(new HttpServerHandler());

        // 开启服务请求处理
        // httpServer.requestHandler(request -> {
        //     log.info("received request: {},{}", request.method().toString(), request.uri());
        //
        //     request.response().putHeader("content-type", "text/plain").end("welcome to genshin impact!");
        //
        // });

        // 监听端口
        httpServer.listen(port, result -> {
            if (result.succeeded()) {
                log.info("server is now listening on port:{}", port);
            } else {
                log.error("server start failed:{}", result.cause().toString());
            }
        });
    }
}