package com.nekonade.network.param.game.message.im;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 311, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class SendIMMsgeResponse extends AbstractJsonGameMessage<SendIMMsgeResponse.IMMsgBody> {
    @Override
    protected Class<IMMsgBody> getBodyObjClass() {
        return IMMsgBody.class;
    }


    @Getter
    @Setter
    public static class IMMsgBody {

        private long seqId;

        private String text;

        private String sender;//消息发送者，这里测试，使用昵称，也可以添加一些其它的信息，比如头像，等级等。

    }

}
