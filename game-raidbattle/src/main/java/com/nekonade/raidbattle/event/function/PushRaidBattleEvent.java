package com.nekonade.raidbattle.event.function;

import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.network.param.game.message.battle.RaidBattleCardAttackMsgRequest;
import com.nekonade.raidbattle.manager.RaidBattleManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PushRaidBattleEvent extends ApplicationEvent {

    private final RaidBattleManager raidBattleManager;

    private final List<Long> boardIds;

    private final IGameMessage gameMessage;

    public PushRaidBattleEvent(Object source, RaidBattleManager raidBattleManager, RaidBattleCardAttackMsgRequest gameMessage) {
        super(source);
        this.raidBattleManager = raidBattleManager;
        this.boardIds = new ArrayList<>();
        this.gameMessage = gameMessage;
    }
}
