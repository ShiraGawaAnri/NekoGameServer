package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 207, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class GetRaidBattleListMsgRequest extends AbstractJsonGameMessage<GetRaidBattleListMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private boolean finish = false;

        private int page = 1;

        private int limit = 10;

        private int sort = 1;

    }

}
