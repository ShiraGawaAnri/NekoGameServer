package com.nekonade.network.param.game.message.neko;

import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@GameMessageMetadata(messageId = 306, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class DoReceiveMailMsgResponse extends AbstractJsonGameMessage<DoReceiveMailMsgResponse.PageResult> {

    @Override
    protected Class<PageResult> getBodyObjClass() {
        return PageResult.class;
    }

    @Getter
    @Setter
    public static class PageResult<T> extends com.nekonade.common.model.PageResult<T> {

        private List<T> list;

    }
}
