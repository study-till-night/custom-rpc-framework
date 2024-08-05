package com.shuking.rpccore.config;

import com.shuking.rpccore.constant.LoadBalancerConstants;
import com.shuking.rpccore.constant.RetryStrategyConstants;
import com.shuking.rpccore.constant.TolerantStrategyConstants;
import com.shuking.rpccore.serializer.SerializerEnum;
import lombok.Data;

/**
 * 服务自身配置
 */
@Data
public class RpcConfig {

    private RegistryConfig registryConfig = new RegistryConfig();

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
    private String serializer = SerializerEnum.JDK.getSerializerName();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerConstants.ROUND_ROBIN;

    /**
     * 重试机制
     */
    private String retry = RetryStrategyConstants.FIXED_INTERVAL;

    /**
     * 容错机制
     */
    private String tolerant = TolerantStrategyConstants.FAIL_FAST;
}
