package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 206, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetMailBoxMsgResponse extends AbstractJsonGameMessage<GetMailBoxMsgResponse.PageResult> {

    @Override
    protected Class<PageResult> getBodyObjClass() {
        return PageResult.class;
    }

    @Getter
    @Setter
    public static class PageResult<T> extends com.nekonade.common.model.PageResult<T>{


    }
}
