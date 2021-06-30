package com.nekonade.network.message.event.function;

import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EnterGameEvent extends ApplicationEvent {

    private final PlayerManager playerManager;

    private final GameMessageHeader header;

    public EnterGameEvent(Object source, PlayerManager playerManager,GameMessageHeader header) {
        super(source);
        this.playerManager = playerManager;
        this.header = header;
    }

}
