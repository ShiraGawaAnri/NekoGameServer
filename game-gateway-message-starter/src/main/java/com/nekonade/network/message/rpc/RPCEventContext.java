package com.nekonade.network.message.rpc;


import com.nekonade.common.gameMessage.DataManager;
import com.nekonade.network.message.channel.AbstractGameChannelHandlerContext;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.IGameMessage;

public class RPCEventContext<T extends DataManager> {

    private final IGameMessage request;
    private final T data;//这个用于存储缓存的数据，因为不同的服务的数据结构是不同的，所以这里使用泛型
    private final AbstractGameChannelHandlerContext ctx;

    public RPCEventContext(T data, IGameMessage request, AbstractGameChannelHandlerContext ctx) {
        super();
        this.request = request;
        this.ctx = ctx;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void sendResponse(IGameMessage response) {
        GameMessageHeader responseHeader = response.getHeader();
        EnumMessageType messageType = responseHeader.getMessageType();
        if (messageType != EnumMessageType.RPC_RESPONSE) {
            throw new IllegalArgumentException(response.getClass().getName() + " 参数类型不对，不是RPC的响应数据对象");
        }
        GameMessageHeader requestHeander = request.getHeader();
        responseHeader.setToServerId(requestHeander.getFromServerId());
        responseHeader.setFromServerId(requestHeander.getToServerId());
        responseHeader.setClientSeqId(requestHeander.getClientSeqId());
        responseHeader.setClientSendTime(requestHeander.getClientSendTime());
        responseHeader.setPlayerId(requestHeander.getPlayerId());
        responseHeader.setServerSendTime(System.currentTimeMillis());
        ctx.writeRPCMessage(response, null);
    }
}
