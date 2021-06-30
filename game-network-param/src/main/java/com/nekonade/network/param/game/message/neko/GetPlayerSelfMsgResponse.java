package com.nekonade.network.param.game.message.neko;

import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 202, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetPlayerSelfMsgResponse extends AbstractJsonGameMessage<GetPlayerSelfMsgResponse.ResponseBody> {

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody {

        private long playerId;

        private String zoneId;

        private String nickname;

        private int level;

        private long lastLoginTime;

        private long createTime;


    }
}
