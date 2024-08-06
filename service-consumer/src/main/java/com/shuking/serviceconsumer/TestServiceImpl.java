package com.shuking.serviceconsumer;

import com.shuking.common.model.Player;
import com.shuking.common.services.PlayerService;
import com.shuking.rpcspringbootstarter.annotation.RpcReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class TestServiceImpl {

    @RpcReference
    private PlayerService playerService;

    public void test() {
        List<Player> playerList = playerService.searchPlayerList("shuking");
        log.info("调用结果--{}", playerList.toString());
    }
}
