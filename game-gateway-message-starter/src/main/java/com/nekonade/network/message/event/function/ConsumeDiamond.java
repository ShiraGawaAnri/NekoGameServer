package com.nekonade.network.message.event.function;

import com.nekonade.network.message.manager.PlayerManager;
import org.springframework.context.ApplicationEvent;

public class ConsumeDiamond extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private final int diamond;
    private final PlayerManager playerManager;

    public ConsumeDiamond(Object source, int diamond, PlayerManager playerManager) {
        super(source);
        this.diamond = diamond;
        this.playerManager = playerManager;
    }

    public int getDiamond() {
        return diamond;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }


}
