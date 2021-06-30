package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 208, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class GetRaidBattleRewardListMsgRequest extends AbstractJsonGameMessage<GetRaidBattleRewardListMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private int claimed = 0;

        private int page = 1;

        private int limit = 10;

        private int sort = 1;

    }

}
