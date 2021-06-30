package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.dto.raidbattle.vo.RaidBattleVo;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 401, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class DoCreateBattleMsgResponse extends AbstractJsonGameMessage<DoCreateBattleMsgResponse.ResponseBody> {

    @Override
    protected Class<DoCreateBattleMsgResponse.ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody extends RaidBattleVo {

    }
}
