package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 201, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class DoEnterGameMsgRequest extends AbstractJsonGameMessage<DoEnterGameMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return null;
    }

    public static class RequestBody {

    }
}
