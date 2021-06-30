package com.nekonade.network.message.event.function;

import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;



@Getter
public class ConsumeGoldEvent extends ApplicationEvent {


    private final int gold;

    private final PlayerManager playerManager;

    public ConsumeGoldEvent(Object source, int gold, PlayerManager playerManager) {
        super(source);
        this.gold = gold;
        this.playerManager = playerManager;
    }
}
