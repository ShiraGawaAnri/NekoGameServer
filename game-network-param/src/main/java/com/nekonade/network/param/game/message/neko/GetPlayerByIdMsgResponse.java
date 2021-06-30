package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@GameMessageMetadata(messageId = 302, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetPlayerByIdMsgResponse extends AbstractJsonGameMessage<GetPlayerByIdMsgResponse.ResponseBody> {

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody {

        private long playerId;

        private String nickName;

        private Map<String, Character> characterMap;


    }
}
