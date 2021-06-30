package com.nekonade.network.param.game.message.battle;


import com.nekonade.common.dto.raidbattle.vo.RaidBattleVo;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 1000, messageType = EnumMessageType.RESPONSE, serviceId = 102,groupId = ConstMessageGroup.RAIDBATTLE)
public class JoinRaidBattleMsgResponse extends AbstractJsonGameMessage<JoinRaidBattleMsgResponse.ResponseBody> {

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody extends RaidBattleVo {

    }
}



