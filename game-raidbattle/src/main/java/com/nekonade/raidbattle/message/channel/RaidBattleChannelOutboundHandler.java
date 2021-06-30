package com.nekonade.raidbattle.message.channel;

import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.util.concurrent.Promise;

public interface RaidBattleChannelOutboundHandler extends RaidBattleChannelHandler {

    void writeAndFlush(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg, RaidBattleChannelPromise promise) throws Exception;

    void writeRPCMessage(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage gameMessage, Promise<IGameMessage> callback);

    void close(AbstractRaidBattleChannelHandlerContext ctx, RaidBattleChannelPromise promise);

}
