package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 401, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class DoCreateBattleMsgRequest extends AbstractJsonGameMessage<DoCreateBattleMsgRequest.RequestBody> {


    @Override
    protected Class<DoCreateBattleMsgRequest.RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private int area;

        private int episode;

        private int chapter;

        private int stage;

        private int difficulty;

    }
}
