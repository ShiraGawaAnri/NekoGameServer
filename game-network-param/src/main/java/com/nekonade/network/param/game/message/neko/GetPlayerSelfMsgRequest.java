package com.nekonade.network.param.game.message.neko;

import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 202, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class GetPlayerSelfMsgRequest extends AbstractJsonGameMessage<GetPlayerSelfMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

    }

}
