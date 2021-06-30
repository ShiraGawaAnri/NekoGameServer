package com.nekonade.network.param.game.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nekonade.common.gameMessage.AbstractGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import com.nekonade.network.param.game.message.body.ThirdMsgBody;

@GameMessageMetadata(messageId = 10003, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class ThirdMsgResponse extends AbstractGameMessage {
    private ThirdMsgBody.ThirdMsgResponseBody responseBody;//声明消息体


    public ThirdMsgBody.ThirdMsgResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ThirdMsgBody.ThirdMsgResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    protected byte[] encode() {
        return this.responseBody.toByteArray();//序列化消息体
    }

    @Override
    protected void decode(byte[] body) {
        try {
            this.responseBody = ThirdMsgBody.ThirdMsgResponseBody.parseFrom(body);//反序列化消息体
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isBodyMsgNull() {
        return this.responseBody == null;//判断消息体是否为空
    }

}
