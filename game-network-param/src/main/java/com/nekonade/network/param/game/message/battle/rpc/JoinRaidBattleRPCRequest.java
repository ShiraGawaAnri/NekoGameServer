package com.nekonade.network.param.game.message.battle.rpc;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 1000, messageType = EnumMessageType.RPC_REQUEST, serviceId = 101)
public class JoinRaidBattleRPCRequest extends AbstractJsonGameMessage<JoinRaidBattleRPCRequest.RequestBody> {

    @Override
    protected Class<JoinRaidBattleRPCRequest.RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }

    @Getter
    @Setter
    public static class RequestBody {

        private String raidId;

        private long playerId;

        private long timestamp;
    }
}



