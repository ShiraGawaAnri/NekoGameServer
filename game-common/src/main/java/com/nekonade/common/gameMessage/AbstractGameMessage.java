package com.nekonade.common.gameMessage;

import org.springframework.data.annotation.Transient;

public abstract class AbstractGameMessage implements IGameMessage {

    private GameMessageHeader header;

    @Transient
    private byte[] body;


    public AbstractGameMessage() {
        GameMessageMetadata gameMessageMetaData = this.getClass().getAnnotation(GameMessageMetadata.class);
        if (gameMessageMetaData == null) {
            throw new IllegalArgumentException("消息没有添加元数据注解：" + this.getClass().getName());
        }
        header = new GameMessageHeader();
        header.setMessageId(gameMessageMetaData.messageId());
        header.setServiceId(gameMessageMetaData.serviceId());
        header.setMessageType(gameMessageMetaData.messageType());
    }

    public <T extends IGameMessage> void wrapResponse(T another) {
        GameMessageMetadata gameMessageMetaData = this.getClass().getAnnotation(GameMessageMetadata.class);
        if (gameMessageMetaData == null) {
            throw new IllegalArgumentException("消息没有添加元数据注解：" + this.getClass().getName());
        }
        header.setClientSeqId(another.getHeader().getClientSeqId());
        header.setPlayerId(another.getHeader().getPlayerId());
        header.setClientSendTime(another.getHeader().getClientSendTime());
        header.setVersion(another.getHeader().getVersion());
        header.setAttribute(another.getHeader().getAttribute());
        header.setToServerId(another.getHeader().getFromServerId());
        header.setFromServerId(another.getHeader().getToServerId());
    }

    public int getMessageId(){
        GameMessageMetadata gameMessageMetaData = this.getClass().getAnnotation(GameMessageMetadata.class);
        if(gameMessageMetaData == null){
            return -255;
        }
        return gameMessageMetaData.messageId();
    }

    public EnumMessageType getEnumMessageType(){
        GameMessageMetadata gameMessageMetaData = this.getClass().getAnnotation(GameMessageMetadata.class);
        if(gameMessageMetaData == null){
            return null;
        }
        return gameMessageMetaData.messageType();
    }

    public Boolean sameMessageMeta(int messageId,EnumMessageType enumMessageType){
        GameMessageMetadata gameMessageMetaData = this.getClass().getAnnotation(GameMessageMetadata.class);
        if(gameMessageMetaData == null || enumMessageType == null){
            return false;
        }
        return messageId == gameMessageMetaData.messageId() && enumMessageType.equals(gameMessageMetaData.messageType());
    }


    @Override
    public GameMessageHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(GameMessageHeader header) {
        this.header = header;
    }

    @Override
    public void read(byte[] body) {
        this.body = body;
        if (body != null) {// 如果不为null，才反序列化，这样不用考虑为null的情况，防止忘记判断。
            this.decode(body);
        }
    }


    @Override
    public byte[] body() {
        if (body == null) {
            if (!this.isBodyMsgNull()) {// 如果内容不为null，再去序列化，这样子类实现的时候，不需要考虑null的问题了。
                body = this.encode();
                if (body == null) {// 检测是否返回的空，防止开发者默认返回null
                    throw new IllegalArgumentException("消息序列化之后的值为null:" + this.getClass().getName());
                }
            }
        }
        return body;
    }

    protected abstract byte[] encode();// 这些方法由子类自己实现，因为每个子类的这些行为是不一样的。

    protected abstract void decode(byte[] body);

    protected abstract boolean isBodyMsgNull();

}
