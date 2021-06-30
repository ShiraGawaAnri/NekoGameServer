package com.nekonade.network.param.game.message.im;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 310, messageType = EnumMessageType.REQUEST, serviceId = 103)
public class EnterIMMsgRequest extends AbstractJsonGameMessage<EnterIMMsgRequest.EnterIMMsgBody> {
    @Override
    protected Class<EnterIMMsgBody> getBodyObjClass() {
        return null;
    }

    public static class EnterIMMsgBody {

    }

}
