package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 306, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class DoReceiveMailMsgRequest extends AbstractJsonGameMessage<DoReceiveMailMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }


    @Getter
    @Setter
    public static class RequestBody {

        private String mailId;

        private int page = 1;

        private boolean isAllPages = false;

        private int type = -1;

        private String lastId;
    }
}
