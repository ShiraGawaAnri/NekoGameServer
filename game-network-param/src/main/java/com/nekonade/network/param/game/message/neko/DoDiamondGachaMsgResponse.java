package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.dto.CharacterVo;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@GameMessageMetadata(messageId = 501, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class DoDiamondGachaMsgResponse extends AbstractJsonGameMessage<DoDiamondGachaMsgResponse.ResponseBody> {

    @Override
    protected Class<DoDiamondGachaMsgResponse.ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody {

        private List<CharacterVo> result;
    }
}
