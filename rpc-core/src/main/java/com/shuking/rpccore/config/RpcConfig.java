package com.shuking.rpccore.config;

import com.shuking.rpccore.serializer.Serializer;
import com.shuking.rpccore.serializer.SerializerEnum;
import lombok.Data;

@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "common-rpc";

    /**
     * 版本号
     */
    private String version = "1.0.0";

    private String serverHost = "127.0.0.1";

    private Integer port = 8080;

    /**
     * 是否开启mock
     */
    private Boolean mock = false;

    /**
     * 选择的序列化器
     */
    private String  serializer= SerializerEnum.JDK.serializerName;
}
