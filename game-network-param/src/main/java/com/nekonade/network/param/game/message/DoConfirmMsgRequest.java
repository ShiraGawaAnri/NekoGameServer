package com.nekonade.network.param.game.message;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 1, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class DoConfirmMsgRequest extends AbstractJsonGameMessage<DoConfirmMsgRequest.ConfirmBody> {

    @Override
    protected Class<ConfirmBody> getBodyObjClass() {
        return ConfirmBody.class;
    }

    public static class ConfirmBody {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

}
