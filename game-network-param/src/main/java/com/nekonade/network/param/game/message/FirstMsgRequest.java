package com.nekonade.network.param.game.message;

import com.nekonade.common.gameMessage.AbstractGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;

@GameMessageMetadata(messageId = 10001, serviceId = 1, messageType = EnumMessageType.REQUEST) // 添加元数据信息
public class FirstMsgRequest extends AbstractGameMessage {
    private String value;

    @Override
    protected void decode(byte[] body) {
        value = new String(body);// 反序列化消息，这里不用判断null，父类上面已判断过
    }

    @Override
    protected byte[] encode() {
        return value.getBytes();// 序列化消息,这里不用判断null，父类上面已判断过
    }

    @Override
    protected boolean isBodyMsgNull() {// 返回要序列化的消息体是否为null
        return this.value == null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
