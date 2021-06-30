package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 210, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetTasksMsgResponse extends AbstractJsonGameMessage<GetTasksMsgResponse.ResponseBody> {

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody {

    }
}
