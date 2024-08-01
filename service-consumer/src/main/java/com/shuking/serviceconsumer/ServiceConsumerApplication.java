package com.shuking.serviceconsumer;

import com.shuking.common.model.Player;
import com.shuking.common.services.PlayerService;
import com.shuking.rpccore.utils.SpiUtil;
import com.shuking.serviceconsumer.proxy.ServiceProxyFactory;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class ServiceConsumerApplication {

    public static void main(String[] args) {
        // 获取代理增强过的服务类
        PlayerService playerServiceWithProxy = ServiceProxyFactory.getProxy(PlayerService.class);
        SpiUtil.loadAllInterface();

        if (playerServiceWithProxy != null) {
            for (int i = 0; i < 2; i++) {
                List<Player> playerList = playerServiceWithProxy.searchPlayerList("shuking");

                if (playerList != null) {
                    log.info("call success");
                } else {
                    log.error("call failed");
                }
            }
        }
    }
}
