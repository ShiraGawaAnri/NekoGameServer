package com.nekonade.network.param.game.message.neko.rpc;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 210, messageType = EnumMessageType.RPC_RESPONSE, serviceId = 102)
public class ConsumeDiamondMsgResponse extends AbstractJsonGameMessage<ConsumeDiamondMsgResponse.ResponseBody> {

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    public static class ResponseBody {

    }
}
