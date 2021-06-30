package com.nekonade.network.message.event.user;

import lombok.Getter;

@Getter
public class DoClaimRaidBattleRewardEventUser extends BasicEventUser {

    private final String raidId;

    public DoClaimRaidBattleRewardEventUser(String raidId) {
        this.raidId = raidId;
    }
}
