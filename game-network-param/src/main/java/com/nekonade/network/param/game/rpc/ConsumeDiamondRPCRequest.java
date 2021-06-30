package com.nekonade.network.param.game.rpc;

import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 304, messageType = EnumMessageType.RPC_REQUEST, serviceId = 101)
public class ConsumeDiamondRPCRequest extends AbstractJsonGameMessage<ConsumeDiamondRPCRequest.RequestBody> {
    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    public static class RequestBody {
        private int consumeCount;

        public int getConsumeCount() {
            return consumeCount;
        }

        public void setConsumeCount(int consumeCount) {
            this.consumeCount = consumeCount;
        }
    }


}
