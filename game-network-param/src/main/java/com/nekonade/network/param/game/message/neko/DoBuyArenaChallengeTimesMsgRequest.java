package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 210, messageType = EnumMessageType.REQUEST, serviceId = 102)
public class DoBuyArenaChallengeTimesMsgRequest extends AbstractJsonGameMessage<DoBuyArenaChallengeTimesMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return null;
    }

    public static class RequestBody {

    }
}
