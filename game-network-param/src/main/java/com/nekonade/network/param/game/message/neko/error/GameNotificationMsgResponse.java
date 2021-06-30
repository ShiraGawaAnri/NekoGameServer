package com.nekonade.network.param.game.message.neko.error;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 6, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class GameNotificationMsgResponse extends AbstractJsonGameMessage<GameNotificationMsgResponse.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private Object error;
    }
}
