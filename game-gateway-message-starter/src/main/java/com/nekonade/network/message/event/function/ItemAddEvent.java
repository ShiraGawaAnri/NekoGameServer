package com.nekonade.network.message.event.function;

import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ItemAddEvent extends ApplicationEvent {

    private final PlayerManager playerManager;

    private final String itemId;

    private final int amount;

    public ItemAddEvent(Object source, PlayerManager playerManager, String itemId, int count) {
        super(source);
        this.playerManager = playerManager;
        this.itemId = itemId;
        this.amount = count;
    }
}
