package com.nekonade.network.message.event.function;

import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExperienceAddEvent extends ApplicationEvent {

    private final PlayerManager playerManager;

    private final Long exp;

    public ExperienceAddEvent(Object source, PlayerManager playerManager,long exp) {
        super(source);
        this.playerManager = playerManager;
        this.exp = exp;
    }
}
