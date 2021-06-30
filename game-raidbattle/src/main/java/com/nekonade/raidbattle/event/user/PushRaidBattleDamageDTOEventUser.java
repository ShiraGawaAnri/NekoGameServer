package com.nekonade.raidbattle.event.user;

import com.nekonade.common.dto.raidbattle.vo.RaidBattleDamageVo;
import com.nekonade.common.gameMessage.IGameMessage;
import lombok.Getter;

@Getter
public class PushRaidBattleDamageDTOEventUser extends BasicEventUser {

    private final IGameMessage request;

    private final RaidBattleDamageVo damageDTO;

    public PushRaidBattleDamageDTOEventUser(IGameMessage request, RaidBattleDamageVo damageDTO) {
        this.request = request;
        this.damageDTO = damageDTO;
    }
}
