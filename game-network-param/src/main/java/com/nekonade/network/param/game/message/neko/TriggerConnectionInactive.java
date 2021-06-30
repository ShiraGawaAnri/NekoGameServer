package com.nekonade.network.param.game.message.neko;

import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 9, messageType = EnumMessageType.REQUEST, serviceId = 101,groupId = ConstMessageGroup.Trigger)
public class TriggerConnectionInactive extends AbstractJsonGameMessage<TriggerConnectionInactive.RequestBody> {

    @Override
    protected Class<TriggerConnectionInactive.RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private long playerId;

        private int serverId;
    }
}
