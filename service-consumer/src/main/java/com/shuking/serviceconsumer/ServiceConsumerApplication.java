package com.shuking.serviceconsumer;

import com.shuking.common.model.Player;
import com.shuking.common.services.PlayerService;
import com.shuking.serviceconsumer.proxy.ServiceProxyFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@Log4j2
public class ServiceConsumerApplication {

    public static void main(String[] args) {
        // 获取代理增强过的服务类
        PlayerService playerServiceWithProxy = ServiceProxyFactory.getProxy(PlayerService.class);

        if (playerServiceWithProxy != null) {
            List<Player> playerList = playerServiceWithProxy.searchPlayerList("shuking");
            if (playerList != null) {
                log.info("call success");
            } else {
                log.error("call failed");
            }
        }
    }
}