package com.nekonade.game.client.common;

import com.nekonade.common.dto.PlayerVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class PlayerInfo extends PlayerVo {

    private boolean connected;

    private boolean entered;
}
