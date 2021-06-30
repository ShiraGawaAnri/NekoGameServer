package com.nekonade.network.param.game.message.neko.rpc;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 210, messageType = EnumMessageType.RPC_REQUEST, serviceId = 101)
public class ConsumeDiamondMsgRequest extends AbstractJsonGameMessage<ConsumeDiamondMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return null;
    }

    public static class RequestBody {

        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }
}
