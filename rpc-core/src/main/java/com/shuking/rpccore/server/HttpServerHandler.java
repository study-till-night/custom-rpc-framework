package com.shuking.rpccore.server;


import com.shuking.rpccore.model.RpcRequest;
import com.shuking.rpccore.model.RpcResponse;
import com.shuking.rpccore.registry.LocalRegistry;
import com.shuking.rpccore.serializer.JdkSerializer;
import com.shuking.rpccore.serializer.Serializer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 请求处理器    直接接触生产者方法的部位
 */
@Log4j2
public class HttpServerHandler implements Handler<HttpServerRequest> {

    /**
     * 1、反序列化
     * 2、根据服务名称获取服务类
     * 3、利用反射执行传入的方法，得到数据
     * 4、序列化，传回消费者
     *
     * @param httpServerRequest
     */
    @Override
    public void handle(HttpServerRequest httpServerRequest) {

        // 序列化器
        final Serializer jdkSerializer = new JdkSerializer();

        log.info("received request: {},{}", httpServerRequest.method().toString(), httpServerRequest.uri());

        httpServerRequest.bodyHandler(body -> {
            // 进行反序列化
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = jdkSerializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                log.error("反序列化时出错:{}", e.getMessage());
            }

            // 构建响应对象
            RpcResponse rpcResponse = new RpcResponse();
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpc请求体为空");
                // 执行响应
                doResponse(httpServerRequest, rpcResponse, jdkSerializer);
                return;
            }

            try {
                // 利用反射得到要执行的方法
                Class<?> serviceClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = serviceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
                // 得到方法执行结果
                Object result = method.invoke(serviceClass.newInstance(), rpcRequest.getParams());

                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage(String.format("成功调用:%s,%s", httpServerRequest.method().toString(), httpServerRequest.uri()));
                rpcResponse.setException(null);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     InstantiationException e) {
                log.error("对方法进行反射时报错:{}", e.getMessage());
            }
            // 执行响应
            doResponse(httpServerRequest, rpcResponse, jdkSerializer);
        });

    }

    /**
     * 执行响应 返回序列化数据
     *
     * @param request     vertx请求
     * @param rpcResponse 自定义响应包装体
     * @param serializer  序列化器
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response().putHeader("content-type", "application/json");

        // 将序列化后数据返回
        try {
            byte[] responseBytes = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(responseBytes));
        } catch (IOException e) {
            log.error("响应时序列化报错:{}", e.getMessage());
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
