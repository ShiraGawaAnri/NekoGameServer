package com.nekonade.network.param.game.message;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 1, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class DoConfirmMsgResponse extends AbstractJsonGameMessage<DoConfirmMsgResponse.ConfirmResponseBody> {


    @Override
    protected Class<ConfirmResponseBody> getBodyObjClass() {
        return ConfirmResponseBody.class;
    }

    public static class ConfirmResponseBody {

        private String secretKey; //对称加密密钥，客户端需要使用非对称加密私钥解密才能获得。

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

    }
}
