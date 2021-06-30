package com.nekonade.network.param.game.message.battle;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@GameMessageMetadata(messageId = 1002, messageType = EnumMessageType.REQUEST, serviceId = 102,groupId = ConstMessageGroup.RAIDBATTLE)
public class RaidBattleCardAttackMsgRequest extends AbstractJsonGameMessage<RaidBattleCardAttackMsgRequest.RequestBody> {

    @Override
    protected Class<RaidBattleCardAttackMsgRequest.RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private int charaPos;

        private String charaId;

        private String cardId;

        private int targetPos;

        private List<Integer> selectCharaPos;

        private long turn;

        private long timestamp;
    }
}



