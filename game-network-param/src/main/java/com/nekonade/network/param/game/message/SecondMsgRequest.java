package com.nekonade.network.param.game.message;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 10002, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class SecondMsgRequest extends AbstractJsonGameMessage<SecondMsgRequest.SecondRequestBody> {
    @Override
    protected Class<SecondRequestBody> getBodyObjClass() {
        return SecondRequestBody.class;
    }

    public static class SecondRequestBody {
        private String value1;
        private long value2;
        private String value3;

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public long getValue2() {
            return value2;
        }

        public void setValue2(long value2) {
            this.value2 = value2;
        }

        public String getValue3() {
            return value3;
        }

        public void setValue3(String value3) {
            this.value3 = value3;
        }


    }


}
