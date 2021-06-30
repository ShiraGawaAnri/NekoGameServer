package com.nekonade.raidbattle.message.channel;

import io.netty.util.concurrent.EventExecutor;

public class DefaultRaidBattleChannelHandlerContext extends AbstractRaidBattleChannelHandlerContext {
    private final RaidBattleChannelHandler handler;

    public DefaultRaidBattleChannelHandlerContext(RaidBattleChannelPipeline pipeline, EventExecutor executor, String name, RaidBattleChannelHandler channelHandler) {
        super(pipeline, executor, name, isInbound(channelHandler), isOutbound(channelHandler));//判断一下这个channelHandler是处理接收消息的Handler还是处理发出消息的Handler
        this.handler = channelHandler;
    }

    private static boolean isInbound(RaidBattleChannelHandler handler) {
        return handler instanceof RaidBattleChannelInboundHandler;
    }

    private static boolean isOutbound(RaidBattleChannelHandler handler) {
        return handler instanceof RaidBattleChannelOutboundHandler;
    }

    @Override
    public RaidBattleChannelHandler handler() {
        return this.handler;
    }

}
