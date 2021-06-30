package com.nekonade.network.param.game.message;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 10002, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class SecondMsgResponse extends AbstractJsonGameMessage<SecondMsgResponse.SecondMsgResponseBody> {

    @Override
    protected Class<SecondMsgResponseBody> getBodyObjClass() {
        return SecondMsgResponseBody.class;
    }

    public static class SecondMsgResponseBody {
        private long result1;
        private String result2;

        public long getResult1() {
            return result1;
        }

        public void setResult1(long result1) {
            this.result1 = result1;
        }

        public String getResult2() {
            return result2;
        }

        public void setResult2(String result2) {
            this.result2 = result2;
        }
    }
}


