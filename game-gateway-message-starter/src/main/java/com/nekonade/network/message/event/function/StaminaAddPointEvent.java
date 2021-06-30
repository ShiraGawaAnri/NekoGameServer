package com.nekonade.network.message.event.function;

import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

public class StaminaAddPointEvent extends ApplicationEvent {

    @Getter
    private final PlayerManager playerManager;

    @Getter
    @Setter
    private int point;

    public StaminaAddPointEvent(Object source, PlayerManager playerManager) {
        super(source);
        this.playerManager = playerManager;
    }
}
