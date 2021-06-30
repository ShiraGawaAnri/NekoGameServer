package com.nekonade.raidbattle.event.user;

import com.nekonade.common.dto.PlayerVo;
import com.nekonade.raidbattle.manager.RaidBattleManager;
import lombok.Getter;

@Getter
public class JoinedRaidBattlePlayerInitCharacterEventUser extends BasicEventUser{

    private final PlayerVo playerDTO;

    private final RaidBattleManager raidBattleManager;

    public JoinedRaidBattlePlayerInitCharacterEventUser(PlayerVo playerDTO, RaidBattleManager raidBattleManager) {
        this.playerDTO = playerDTO;
        this.raidBattleManager = raidBattleManager;
    }
}
