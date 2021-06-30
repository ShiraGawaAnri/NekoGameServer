package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.dto.CharacterVo;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@GameMessageMetadata(messageId = 209, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetPlayerCharacterListMsgResponse extends AbstractJsonGameMessage<GetPlayerCharacterListMsgResponse.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private Map<String, CharacterVo> characterMap = new HashMap<>();
    }

}
