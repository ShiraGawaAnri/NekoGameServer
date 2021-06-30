package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@GameMessageMetadata(messageId = 308, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class DoClaimRaidBattleRewardMsgRequest extends AbstractJsonGameMessage<DoClaimRaidBattleRewardMsgRequest.RequestBody> {

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }


    @Getter
    @Setter
    public static class RequestBody {

        @NonNull
        private String raidId;
    }
}
