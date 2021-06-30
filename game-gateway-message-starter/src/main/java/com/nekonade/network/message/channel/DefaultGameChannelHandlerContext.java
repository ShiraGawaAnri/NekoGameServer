package com.nekonade.network.message.channel;

import io.netty.util.concurrent.EventExecutor;

public class DefaultGameChannelHandlerContext extends AbstractGameChannelHandlerContext {
    private final GameChannelHandler handler;

    public DefaultGameChannelHandlerContext(GameChannelPipeline pipeline, EventExecutor executor, String name, GameChannelHandler channelHandler) {
        super(pipeline, executor, name, isInbound(channelHandler), isOutbound(channelHandler));//判断一下这个channelHandler是处理接收消息的Handler还是处理发出消息的Handler
        this.handler = channelHandler;
    }

    private static boolean isInbound(GameChannelHandler handler) {
        return handler instanceof GameChannelInboundHandler;
    }

    private static boolean isOutbound(GameChannelHandler handler) {
        return handler instanceof GameChannelOutboundHandler;
    }

    @Override
    public GameChannelHandler handler() {
        return this.handler;
    }

}
