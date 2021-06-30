package com.nekonade.common.cloud;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class RaidBattleChannelCloseEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    @Getter
    private final String raidId;

    @Getter
    private final int serviceId;

    public RaidBattleChannelCloseEvent(Object source, String raidId,int serviceId) {
        super(source);
        this.raidId = raidId;
        this.serviceId = serviceId;
    }

}
