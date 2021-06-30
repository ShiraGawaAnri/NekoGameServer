package com.nekonade.network.param.game.message.battle;


import com.google.protobuf.InvalidProtocolBufferException;
import com.nekonade.common.gameMessage.*;
import com.nekonade.common.proto.RaidBattleAttackMsgBody;
import lombok.Getter;
import lombok.Setter;

@GameMessageMetadata(messageId = 10001, messageType = EnumMessageType.RESPONSE, serviceId = 102,groupId = ConstMessageGroup.RAIDBATTLE )
public class RaidBattleAttackMsgResponseProtobuf extends AbstractGameMessage {

    @Getter
    @Setter
    private RaidBattleAttackMsgBody.RaidBattleAttackMsgResponseBody responseBody;

    @Override
    protected byte[] encode() {
        return this.responseBody.toByteArray();
    }

    @Override
    protected void decode(byte[] body) {
        try {
            this.responseBody = RaidBattleAttackMsgBody.RaidBattleAttackMsgResponseBody.parseFrom(body);//反序列化消息体
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isBodyMsgNull() {
        return this.responseBody == null;
    }
}



