package com.nekonade.network.param.game.message.battle;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 1000, messageType = EnumMessageType.REQUEST, serviceId = 102,groupId = ConstMessageGroup.RAIDBATTLE)
public class JoinRaidBattleMsgRequest extends AbstractJsonGameMessage<JoinRaidBattleMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private String raidId;

        private long playerId;

        private long timestamp;
    }
}



