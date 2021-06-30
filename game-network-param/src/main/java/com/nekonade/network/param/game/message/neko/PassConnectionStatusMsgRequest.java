package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 3, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class PassConnectionStatusMsgRequest extends AbstractJsonGameMessage<PassConnectionStatusMsgRequest.MessageBody> {

    @Override
    protected Class<MessageBody> getBodyObjClass() {
        return MessageBody.class;
    }

    public static class MessageBody {

        private boolean connect;//true是连接成功，false是连接断开

        public boolean isConnect() {
            return connect;
        }

        public void setConnect(boolean connect) {
            this.connect = connect;
        }

    }


}
