package com.nekonade.network.message.event.user;

import com.nekonade.network.param.game.message.neko.DoCreateBattleMsgRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoCreateBattleEventUser extends BasicEventUser<DoCreateBattleMsgRequest.RequestBody> {

    private long playerId;

    public DoCreateBattleEventUser(long playerId, DoCreateBattleMsgRequest request) {
        this.playerId = playerId;
        this.request = request;
    }
}
