package com.nekonade.network.message.event.function;

import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StagePassEvent extends ApplicationEvent {

    private final String stageId;

    private final int time;

    private final PlayerManager playerManager;

    public StagePassEvent(Object source, String stageId, int time, PlayerManager playerManager) {
        super(source);
        this.stageId = stageId;
        this.time = time;
        this.playerManager = playerManager;
    }

    public StagePassEvent(Object source, String stageId, PlayerManager playerManager) {
        super(source);
        this.stageId = stageId;
        this.time = 1;
        this.playerManager = playerManager;
    }

}
