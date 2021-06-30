package com.nekonade.raidbattle.message.channel;

public interface RaidBattleChannelHandler {

    void exceptionCaught(AbstractRaidBattleChannelHandlerContext ctx, Throwable cause) throws Exception;
}
