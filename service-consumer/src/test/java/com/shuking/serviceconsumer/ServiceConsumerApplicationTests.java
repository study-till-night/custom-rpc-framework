package com.shuking.serviceconsumer;

import com.shuking.rpccore.config.RpcConfig;
import com.shuking.rpccore.utils.ConfigUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServiceConsumerApplicationTests {

    @Test
    void contextLoads() {
        RpcConfig rpcConfig = ConfigUtil.loadConfig(RpcConfig.class);
        System.out.println(rpcConfig);
    }

}
