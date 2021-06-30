package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 204, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetStaminaMsgResponse extends AbstractJsonGameMessage<GetStaminaMsgResponse.Stamina> {

    @Override
    protected Class<Stamina> getBodyObjClass() {
        return Stamina.class;
    }

    @Getter
    @Setter
    public static class Stamina {

        private Integer value = 20;

        private Long preQueryTime = 0L;

        private Long nextRecoverTime = 0L;

        private Long nextRecoverTimestamp = 0L;

        private Integer cutTime = 0;

        private double cutPercent = 0;
    }
}
