package com.nekonade.network.message.event.function;


import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AchievementAccEvent extends ApplicationEvent {

    private final PlayerManager playerManager;

    public AchievementAccEvent(Object source, PlayerManager playerManager) {
        super(source);
        this.playerManager = playerManager;
    }
}