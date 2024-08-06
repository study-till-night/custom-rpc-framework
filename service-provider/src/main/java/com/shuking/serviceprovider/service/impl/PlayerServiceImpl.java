package com.shuking.serviceprovider.service.impl;

import com.shuking.common.model.Player;
import com.shuking.common.services.PlayerService;
import com.shuking.rpcspringbootstarter.annotation.RpcService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RpcService(interfaceClass = PlayerService.class)
@Log4j2
public class PlayerServiceImpl implements PlayerService {

    @Override
    public List<Player> searchPlayerList(String content) {
        log.info("search players that contain '{}' ", content);
        return List.of(new Player());
    }
}

