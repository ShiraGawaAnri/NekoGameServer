package com.nekonade.network.message.event.user;

import com.nekonade.network.message.manager.PlayerManager;
import lombok.Getter;

@Getter
public class DoDiamondGachaEventUser extends BasicEventUser {

    private final PlayerManager playerManager;

    private final String gachaPoolsId;

    private final int gachaType;

    public DoDiamondGachaEventUser(PlayerManager playerManager, String gachaPoolsId, int type) {
        this.playerManager = playerManager;
        this.gachaPoolsId = gachaPoolsId;
        this.gachaType = type;
    }
}
