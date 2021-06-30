package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@GameMessageMetadata(messageId = 501, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class DoDiamondGachaMsgRequest extends AbstractJsonGameMessage<DoDiamondGachaMsgRequest.RequestBody> {

    @Override
    protected Class<DoDiamondGachaMsgRequest.RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        @NonNull
        private String gachaPoolsId;

        private int type = 10;
    }
}
