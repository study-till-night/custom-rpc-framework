package com.shuking.rpccore.config;

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
}
