package com.nekonade.network.param.game.message;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 2, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class HeartbeatMsgRequest extends AbstractJsonGameMessage<Void> {

    @Override
    protected Class<Void> getBodyObjClass() {
        return null;
    }

}
