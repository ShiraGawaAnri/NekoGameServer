package com.nekonade.raidbattle.event.user;

import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.raidbattle.manager.RaidBattleManager;
import com.nekonade.raidbattle.message.context.RaidBattleMessageContext;
import lombok.Getter;

@Getter
public class PushRaidBattleToSinglePlayerEventUser extends BasicEventUser{

    private final IGameMessage request;

    public PushRaidBattleToSinglePlayerEventUser(RaidBattleMessageContext<RaidBattleManager> ctx,IGameMessage request) {
        super();
        this.request = request;
    }
}
