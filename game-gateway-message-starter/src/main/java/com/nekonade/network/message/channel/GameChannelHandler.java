package com.nekonade.network.message.channel;

public interface GameChannelHandler {

    void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception;
}
