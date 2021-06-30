package com.nekonade.raidbattle.event.function;

import com.nekonade.raidbattle.manager.RaidBattleManager;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RaidBattleShouldBeFinishEvent extends ApplicationEvent {

    private final RaidBattleManager raidBattleManager;

    private final boolean idleCheck;

    public RaidBattleShouldBeFinishEvent(Object source,RaidBattleManager raidBattleManager) {
        super(source);
        this.raidBattleManager = raidBattleManager;
        this.idleCheck = false;
    }

    public RaidBattleShouldBeFinishEvent(Object source,RaidBattleManager raidBattleManager,boolean idleCheck) {
        super(source);
        this.raidBattleManager = raidBattleManager;
        this.idleCheck = idleCheck;
    }
}
