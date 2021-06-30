package com.nekonade.network.message.event.function;

import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class StaminaSubPointEvent extends ApplicationEvent {

    @Getter
    private final PlayerManager playerManager;

    @Getter
    private final int point;

    public StaminaSubPointEvent(Object source, PlayerManager playerManager, int point) {
        super(source);
        this.playerManager = playerManager;
        this.point = point;
    }
}
