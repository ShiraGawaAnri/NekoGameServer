package com.nekonade.network.message.event.function;

import com.nekonade.common.dto.ItemDTO;
import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class TriggerSystemSendMailEvent extends ApplicationEvent {

    private final PlayerManager playerManager;

    private String senderName;

    private String content;

    private String title;

    private final List<ItemDTO> gifts;

    public TriggerSystemSendMailEvent(Object source,PlayerManager playerManager,List<ItemDTO> gifts) {
        super(source);
        this.playerManager = playerManager;
        this.gifts = gifts;
    }
}
