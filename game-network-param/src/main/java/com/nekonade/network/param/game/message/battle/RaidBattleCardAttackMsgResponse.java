package com.nekonade.network.param.game.message.battle;


import com.nekonade.common.dto.raidbattle.vo.RaidBattleDamageVo;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 1002, messageType = EnumMessageType.RESPONSE, serviceId = 102,groupId = ConstMessageGroup.RAIDBATTLE)
public class RaidBattleCardAttackMsgResponse extends AbstractJsonGameMessage<RaidBattleCardAttackMsgResponse.ResponseBody> {

    @Override
    protected Class<RaidBattleCardAttackMsgResponse.ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody extends RaidBattleDamageVo {

    }
}



