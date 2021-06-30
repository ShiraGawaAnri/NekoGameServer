package com.nekonade.network.param.game.message.battle.rpc;


import com.nekonade.common.dto.PlayerVo;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.ConstMessageGroup;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 1000, messageType = EnumMessageType.RPC_RESPONSE, serviceId = 102,groupId = ConstMessageGroup.RAIDBATTLE)
public class JoinRaidBattleRPCResponse extends AbstractJsonGameMessage<JoinRaidBattleRPCResponse.ResponseBody> {

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }

    @Getter
    @Setter
    public static class ResponseBody {

        private String raidId;

        private long timestamp;

        private PlayerVo player;
    }

}



