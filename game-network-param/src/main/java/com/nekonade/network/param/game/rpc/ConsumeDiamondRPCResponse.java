package com.nekonade.network.param.game.rpc;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 304, messageType = EnumMessageType.RPC_RESPONSE, serviceId = 102) // 返回的服务id是102服务
public class ConsumeDiamondRPCResponse extends AbstractJsonGameMessage<ConsumeDiamondRPCResponse.ResponseBody> {
    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return null;
    }

    public static class ResponseBody {

    }
}
