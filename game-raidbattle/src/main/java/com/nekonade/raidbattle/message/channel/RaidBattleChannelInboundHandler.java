package com.nekonade.raidbattle.message.channel;

import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.util.concurrent.Promise;

public interface RaidBattleChannelInboundHandler extends RaidBattleChannelHandler {
    //GameChannel第一次注册的时候调用

    /**
     * @param ctx
     * @param raidId 标记ID
     * @param promise
     */
    void channelRegister(AbstractRaidBattleChannelHandlerContext ctx, String raidId, RaidBattleChannelPromise promise);

    //GameChannel被移除的时候调用
    void channelInactive(AbstractRaidBattleChannelHandlerContext ctx) throws Exception;

    //读取并处理客户端发送的消息
    void channelRead(AbstractRaidBattleChannelHandlerContext ctx, Object msg) throws Exception;

    //读取并处理RPC的请求消息
    void channelReadRPCRequest(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg) throws Exception;

    //触发一些内部事件
    void userEventTriggered(AbstractRaidBattleChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception;
}
