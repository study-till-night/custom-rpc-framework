package com.shuking.common.services;

import com.shuking.common.model.Player;
import lombok.NoArgsConstructor;

import java.util.List;

public interface PlayerService {

    List<Player> searchPlayerList(String content);

    default int getNumber() {
        return 1;
    }
}
