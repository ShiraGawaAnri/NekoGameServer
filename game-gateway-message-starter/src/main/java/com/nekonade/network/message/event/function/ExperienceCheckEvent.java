package com.nekonade.network.message.event.function;

import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExperienceCheckEvent extends ApplicationEvent {

    private final PlayerManager playerManager;

    public ExperienceCheckEvent(Object source, PlayerManager playerManager) {
        super(source);
        this.playerManager = playerManager;
    }
}
